package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description TODO
 * <result> = mul <ty> <op1>, <op2>
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class Mul extends Instruction{

    /**
     *  类型只能是i32
     */
    public Mul(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, new IntType(32), parent, op1, op2);
    }
}
