package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.instructions.MipsShift;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;
import utils.MipsMath;
import utils.Pair;

import java.util.ArrayList;

/**
 * @Description TODO
 * <result> = mul <ty> <op1>, <op2>
 * @Author H1KARI
 * @Date 2023/10/30
 **/
public class Mul extends AresInstruction {

    /**
     * 类型只能是i32
     */
    public Mul(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    @Override
    public String toString() {
        return getAresIrString("mul");
    }

    /**
     * 将乘常数分解成多个 (+-shift) 的项
     */
    @Override
    public void buildMips() {
        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
        Value op1 = getOp(1);
        Value op2 = getOp(2);
        MipsBuilder.buildOptMul(dst, op1, op2, Mc.curIrFunction, getParent());
    }
//    /**
//     * 将乘常数分解成多个 (+-shift) 的项
//     */
//    @Override
//    public void buildMips() {
//        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
//
//        // 这里去除了注释
//        Value op1 = getOp(1);
//        Value op2 = getOp(2);
//        Boolean isOp1ConstInt = op1 instanceof ConstInt;
//        Boolean isOp2ConstInt = op2 instanceof ConstInt;
//
//        MipsOperand src1, src2;
//        // 如果有常数，那么可以尝试进行优化
//        if ( isOp1ConstInt || isOp2ConstInt) {
//            int imm;
//            // 取出常数，送入imm
//            if (isOp1ConstInt) {
//                src1 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
//                imm = ((ConstInt) op1).getValue();
//            }
//            // op1非ConstInt
//            else {
//                src1 = MipsBuilder.buildOperand(op1, false, Mc.curIrFunction, getParent());
//                imm = ((ConstInt) op2).getValue();
//            }
//            // 根据常数imm获取优化操作序列
//            ArrayList<Pair<Boolean, Integer>> mulOptItems = MipsMath.getMulOptItems(imm);
//            // 无法优化
//            if (mulOptItems == null) {
//                // 此时src2应该是已经确定为常数的op1或op2
//                if (isOp1ConstInt) {
//                    src2 = MipsBuilder.buildOperand(op1, false, Mc.curIrFunction, getParent());
//                } else {
//                    src2 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
//                }
//            }
//            // 可以优化
//            else {
//                if (mulOptItems.size() == 1) {
//                    doOptStep1(mulOptItems.get(0), dst, src1);
//                }
//                else {
//                    MipsOperand at = MipsRealReg.AT;
//                    doOptStep1(mulOptItems.get(0), at, src1);
//                    // 中间运算的结果存储在 at 中
//                    for (int i = 1; i < mulOptItems.size() - 1; i++) {
//                        doOptStep(mulOptItems.get(i), at, at, src1);
//                    }
//                    doOptStep(mulOptItems.get(mulOptItems.size() - 1), dst, at, src1);
//                }
//                // 至此优化已经完成，dst内存入了结果，不必再向下进行
//                return;
//            }
//        }
//        // 没有常数，无法进行优化
//        else {
//            src1 = MipsBuilder.buildOperand(op1, false, Mc.curIrFunction, getParent());
//            src2 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
//        }
//        // 对于优化失败的场合，直接构造乘法即可
//        MipsBuilder.buildBinary(MipsBinary.BinaryType.MUL, dst, src1, src2, getParent());
//    }
//
//    /**
//     * 进行乘法累加位移优化的中间步骤
//     * 具体来说，tmp = src1 << mulOptItems.get(0).getSecond()
//     *          dst = tmpDst + tmp(optItem.getFirst())
//     *              = tmpDst - tmp(!optItem.getFirst())
//     * @param optItem   累加优化的信息
//     * @param dst       要累加到的寄存器
//     * @param tmpDst    上一步结果暂存到的寄存器
//     * @param src1      乘法的第一操作数
//     */
//    private void doOptStep(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand tmpDst, MipsOperand src1){
//        if (optItem.getSecond() == 0) {
//            // at
//            if(optItem.getFirst()){
//                MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, src1, getParent());
//            } else{
//                MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, src1, getParent());
//            }
//        }
//        // 需要位移
//        else {
//            // 生成周转用的虚拟寄存器 tmp = src1 << mulOptItems.get(0).getSecond()
//            MipsOperand tmp = MipsBuilder.allocVirtualReg(Mc.curIrFunction);
//            MipsBuilder.buildShift(MipsShift.ShiftType.SLL, tmp, src1, optItem.getSecond(), getParent());
//            if(optItem.getFirst()){
//                MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, tmp, getParent());
//            } else{
//                MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, tmp, getParent());
//            }
//        }
//    }
//
//    /**
//     * 进行累加位移优化的第一步
//     * 具体来说    dst =   src1 << mulOptItems.get(0).getSecond()(optItem.getFirst())
//     *               = - src1 << mulOptItems.get(0).getSecond() (!optItem.getFirst())
//     */
//    private void doOptStep1(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand src1){
//        // dst = src1 << mulOptItems.get(0).getSecond()
//        MipsBuilder.buildShift(MipsShift.ShiftType.SLL, dst, src1, optItem.getSecond(), getParent());
//        // dst = -dst
//        if (!optItem.getFirst()) {
//            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, dst, getParent());
//        }
//    }
}
