package ir.values.instructions;

import backend.Mc;
import backend.parts.MipsBlock;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

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
    }
}
