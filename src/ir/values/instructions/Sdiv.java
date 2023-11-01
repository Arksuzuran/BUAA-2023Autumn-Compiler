package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description 有符号除法
 * <result> = sdiv <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Sdiv extends Instruction{

    public Sdiv(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, new IntType(32), parent, op1, op2);
    }
}
