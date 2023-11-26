package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.instructions.MipsMoveHI;
import backend.instructions.MipsShift;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;
import utils.MipsMath;

/**
 * @Description 有符号除法
 * <result> = sdiv <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Sdiv extends AresInstruction {

    public Sdiv(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    // %6 = div i32 5, %5
    @Override
    public String toString() {
        return getAresIrString("sdiv");
    }

    @Override
    public void buildMips() {
        MipsOperand src1 = MipsBuilder.buildOperand(getOp(1), false, Mc.curIrFunction, getParent());
        MipsBlock mipsBlock = Mc.b(getParent());

        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
        Value op2 = getOp(2);

        // 除数是常数，可以进行常数优化
        if (op2 instanceof ConstInt) {
            // 获得除数常量
            int imm = IrTool.getValueOfConstInt(op2);
            // 除数为 1 ，无需生成中间代码，,将 ir 映射成被除数，直接记录即可
            if (imm == 1) {
                Mc.addOperandMapping(this, src1);
            }
            // 除数不为1
            else {
                MipsOperand result = Mc.div(mipsBlock, src1, new MipsImm(imm));
                // 如果先前已有计算结果，无需生成中间代码，直接记录映射即可
                if (result != null) {
                    Mc.addOperandMapping(this, result);
                }
                // 先前没有计算结果，需要手动进行计算
                else {
                    doDivConstOpt(dst, src1, imm);
                }
            }
        }
        // 无法常数优化, 直接进行除法
        else {
            MipsOperand src2 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.DIV, dst, src1, src2, getParent());
        }
    }

    /**
     * 常数优化乘法： dst = src / imm
     */
    private void doDivConstOpt(MipsOperand dst, MipsOperand src, int imm) {
//        System.out.println("imm: " + imm);
        // 这里之所以取 abs，是在之后如果是负数，那么会有一个取相反数的操作
        int abs = Math.abs(imm);
        // 除数为-1，取相反数dst = 0 - src, 生成结束
        if (imm == -1) {
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, src,getParent());
            return;
        }
        // 除数为1，直接进行move
        else if (imm == 1) {
            MipsBuilder.buildMove(dst, src, getParent());
        }
        // 如果是 2 的幂次
        else if (MipsMath.isPow2(abs)) {
            // 末尾0的个数
            int l = MipsMath.countTailZeroNumber(abs);
            // 产生新的被除数
            MipsOperand newSrc = buildNegativeSrcCeil(src, abs);
            // 将新的被除数右移
            MipsBuilder.buildShift(MipsShift.ShiftType.SRA, dst, newSrc, l, getParent());
        }
        // 转换公式dst = src / abs
        // dst = (src * n) >> shift
        else {
            long nc = ((long) 1 << 31) - (((long) 1 << 31) % abs) - 1;
            long p = 32;
            while (((long) 1 << p) <= nc * (abs - ((long) 1 << p) % abs)) {
                p++;
            }
            long m = ((((long) 1 << p) + (long) abs - ((long) 1 << p) % abs) / (long) abs);
            int n = (int) ((m << 32) >>> 32);
            int shift = (int) (p - 32);

            // tmp0 = n
            MipsOperand tmp0 = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
            MipsBuilder.buildMove(tmp0, new MipsImm(n), getParent());

            MipsOperand tmp1 = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
            // tmp1 = src + (src * n)[63:32]
            if (m >= 0x80000000L) {
                // HI = src
                MipsBuilder.buildMoveHI(MipsMoveHI.MoveHIType.MTHI, src, getParent());
                // tmp1 += src * tmp0 + HI （有符号乘法）
                MipsBuilder.buildBinary(MipsBinary.BinaryType.SMMADD, tmp1, src, tmp0, getParent());
            }
            // tmp1 = (src * n)[63:32] 有符号的
            else {
                MipsBuilder.buildBinary(MipsBinary.BinaryType.SMMUL, tmp1, src, tmp0, getParent());
            }

            MipsOperand tmp2 = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
            // tmp2 = tmp1 >> shift
            MipsBuilder.buildShift(MipsShift.ShiftType.SRA, tmp2, tmp1, shift, getParent());
            // AT = src >> 31
            MipsBuilder.buildShift(MipsShift.ShiftType.SRL, MipsRealReg.AT, src, 31, getParent());
            // dst = tmp2 + AT
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, MipsRealReg.AT, tmp2, getParent());
        }

        // 先前都是使用的除数绝对值abs
        // 如果除数为负值，需要变为相反数
        if (imm < 0) {
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, dst, getParent());
        }
        // 记录，以便后续使用
        Mc.addDivMapping(Mc.b(getParent()), src, new MipsImm(imm), dst);
    }
    
    /**
     * 针对负被除数的除法向上取整，产生新的被除数
     * 若只采用移位操作，除法向下取整 -3 / 4 = -1，与除法的含义不符
     * 新的被除数：newDividend = oldDividend + divisor - 1
     * @param oldSrc        旧的被除数
     * @param absImm         除数的绝对值，为2的幂次
     * @return 新的被除数
     */
    private MipsOperand buildNegativeSrcCeil(MipsOperand oldSrc, int absImm) {
        MipsOperand newSrc = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
        int l = MipsMath.countTailZeroNumber(absImm);

        // tmp1 = (oldDividend >> 31)
        // 这是为了保留负数的最高位1，正数在下面的过程中不受影响
        MipsOperand tmp1 = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
        MipsBuilder.buildShift(MipsShift.ShiftType.SRA, tmp1, oldSrc, 31, getParent());
        // tmp1 = tmp1 << 32-l
        // 如果被除数是负数，那么[l-1 : 0] 位全为1，这就是abs - 1
        MipsBuilder.buildShift(MipsShift.ShiftType.SRL, tmp1, tmp1, 32 - l, getParent());
        // newSrc = oldSrc + divisor - 1
        MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, newSrc, oldSrc, tmp1, getParent());
        return newSrc;
    }
}
