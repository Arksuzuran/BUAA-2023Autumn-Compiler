package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description TODO
 * <result> = mul <ty> <op1>, <op2>
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class Mul extends AresInstruction{

    /**
     *  类型只能是i32
     */
    public Mul(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    @Override
    public String toString(){
        return getAresIrString("mul");
    }
}
