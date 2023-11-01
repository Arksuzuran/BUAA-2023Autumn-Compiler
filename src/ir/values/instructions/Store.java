package ir.values.instructions;

import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description 写内存
 * store <ty> <value>, <ty>* <pointer>
 * @Author
 * @Date 2023/10/30
 **/
public class Store extends Instruction{

    /**
     * @param value     要存储的值
     * @param pointer   存储位置指针
     */
    public Store(String name, BasicBlock parent, Value value, Value pointer) {
        super(name, new VoidType(), parent, value, pointer);
    }

    /**
     * 获取指令中的地址操作数
     * @return  地址操作数的value对象
     */
    public Value getPointer(){
        return getOperands().get(1);
    }
}
