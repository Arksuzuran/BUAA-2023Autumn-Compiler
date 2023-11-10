package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description 具有<result> = <arith type> <ty> <op1>, <op2>形式的指令 即四则运算
 * @Author
 * @Date 2023/11/9
 **/
public class AresInstruction extends Instruction{
    public AresInstruction(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, new IntType(32), parent, op1, op2);
    }

    public String getAresIrString(String aresTypeName){
        return getName() + " = " + aresTypeName + " " +
                IrTool.tnstr(getOperands().get(0)) + ", " +
                getOperands().get(1).getName();
    }
}
