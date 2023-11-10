package ir.values.instructions;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstArray;

import java.util.ArrayList;

/**
 * @Description 内存申请指令  该指令对应的Value应当是指针类型
 * <result> = alloca <type>
 * @Author
 * @Date 2023/10/30
 **/
public class Alloca extends Instruction{
    public ConstArray getInitValue() {
        return initArray;
    }

    /**
     * 处理局部常量数组的场合
     */
    private ConstArray initArray = null;
    private ValueType allocaedType = null;

    /**
     * alloca 指令没有操作数
     * @param name     指令Value的名称
     * @param pointingType     要指向的类型
     * @param parent   parent一定是BasicBlock
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent) {
        super(name, new PointerType(pointingType), parent);
        allocaedType = pointingType;
    }

    /**
     * 用于处理带初值的数组常量
     * 该常量的内容被写入了alloca的地址
     * 因此该初值也由alloca对象来保存
     * @param initArray   常量初值
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent, ConstArray initArray) {
        super(name, new PointerType(pointingType), parent);
        this.initArray = initArray;
        allocaedType = pointingType;
    }

    @Override
    public String toString(){
        return getName() + " = alloca " + allocaedType;
    }
}
