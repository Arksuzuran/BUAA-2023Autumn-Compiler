package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;

/**
 * @Description TODO
 * <result> = sub <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Sub extends AresInstruction{

    public Sub(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    @Override
    public String toString(){
        return getAresIrString("sub");
    }

    /**
     * subu
     * subiu ：使用加立即数 addiu -op2 来实现
     */
    @Override
    public void buildMips(){
        MipsBlock block = Mc.b(getParent());
        Value op1 = getOp(1), op2 = getOp(2);

        MipsOperand src1, src2;
        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());

        // op1 op2均为常数 则直接move dst Imm(op1+op2)
        if (op1 instanceof ConstInt && op2 instanceof ConstInt) {
            int imm1 = IrTool.getValueOfConstInt(op1);
            int imm2 = IrTool.getValueOfConstInt(op2);
            MipsBuilder.buildMove(dst, new MipsImm(imm1 - imm2), getParent());
        }
        // op2 是常数，op1不是常数
        // 借用addiu 代替：op1 + (-op2)
        // 需要手动调用buildOperand内的一个分支，将-imm2送进去，以构造-op2
        else if (op1 instanceof ConstInt) {
            int imm2 = IrTool.getValueOfConstInt(op2);
            src1 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
            src2 = MipsBuilder.buildImmOperand(-imm2, true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
        }
        // op2不为常数的场合 要求op1不为常数即可
        // 若op1为常数，buildOperand会处理转化为虚拟寄存器
        else {
            src1 = MipsBuilder.buildOperand(op1, false, Mc.curIrFunction, getParent());
            src2 = MipsBuilder.buildOperand(op2, true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, dst, src1, src2, getParent());
        }
    }
}
