package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description Add指令
 * <result> = add <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Add extends Instruction{
    /**
     * @param name     指令Value的名称
     * @param parent   parent一定是BasicBlock
     * @param operand1 所属操作数1
     * @param operand2 所属操作数2
     */
    public Add(String name, BasicBlock parent, Value operand1, Value operand2) {
        super(name, new IntType(32), parent, operand1, operand2);
    }
}
