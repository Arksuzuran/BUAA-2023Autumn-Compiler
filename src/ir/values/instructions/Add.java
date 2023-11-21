package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.instructions.MipsMove;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description Add指令
 * <result> = add <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Add extends AresInstruction{
    /**
     * @param name     指令Value的名称
     * @param parent   parent一定是BasicBlock
     * @param op1 所属操作数1
     * @param op2 所属操作数2
     */
    public Add(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    @Override
    public String toString(){
        return getAresIrString("add");
    }

    /**
     * addu addiu
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
            MipsBuilder.buildMove(dst, new MipsImm(imm1 + imm2), getParent());
        }
        // op1 是常数，op2不是常数
        //  则op2应当作为rs, op1的值作为imm，二者需要互换位置
        else if (op1 instanceof ConstInt) {
            src1 = MipsBuilder.buildOperand(op2, false, Mc.curIrFunction, getParent());
            src2 = MipsBuilder.buildOperand(op1, true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
        }
        // op1不为常数的场合 在buildOperand内要求op1不为常数即可
        else {
            src1 = MipsBuilder.buildOperand(op1, false, Mc.curIrFunction, getParent());
            src2 = MipsBuilder.buildOperand(op2, true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, src1, src2, getParent());
        }
    }
}
