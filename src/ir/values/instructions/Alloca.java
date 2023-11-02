package ir.values.instructions;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @Description 内存申请指令  该指令对应的Value应当是指针类型
 * <result> = alloca <type>
 * @Author
 * @Date 2023/10/30
 **/
public class Alloca extends Instruction{
    /**
     * alloca指令没有操作数
     * @param name     指令Value的名称
     * @param pointingType     要指向的类型
     * @param parent   parent一定是BasicBlock
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent) {
        super(name, new PointerType(pointingType), parent);
    }
}
