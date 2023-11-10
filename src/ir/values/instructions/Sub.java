package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

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
}
