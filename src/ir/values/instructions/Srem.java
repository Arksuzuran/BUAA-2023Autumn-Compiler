package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;

/**
 * @Description 取余运算
 * <result> = srem <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Srem extends AresInstruction{

    public Srem(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    @Override
    public String toString(){
        return getAresIrString("srem");
    }

    /**
     * 能进入后端的取余运算只有一种：x % 1，其结果为0，直接进行move即可
     */
    // TODO
    @Override
    public void buildMips() {
        if(IrTool.getValueOfConstInt(getOp(2)) != 1){
            System.out.println("[Srem.buildMips] 取余时y不等于1，计算错误");
        }
        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
        MipsBuilder.buildMove(dst, MipsRealReg.ZERO, getParent());
    }
}
