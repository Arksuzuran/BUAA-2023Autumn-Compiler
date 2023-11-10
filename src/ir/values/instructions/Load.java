package ir.values.instructions;

import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description 读取内存
 * <result> = load <ty>, <ty>* <pointer>
 *  其中<ty>就存储在该对象的type里
 * @Author
 * @Date 2023/10/30
 **/
public class Load extends Instruction{
    /**
     * ValueType为指针指向的类型
     * @param pointer   操作数，为指针
     */
    public Load(String name, BasicBlock parent, Value pointer) {
        super(name, IrTool.getPointingTypeOfPointer(pointer), parent, pointer);
    }

    // %2 = load i32, i32* @c
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = load ");  // "%2 = load "
        stringBuilder.append(getType()).append(", ");          // "i32, "
        stringBuilder.append(IrTool.tnstr(getOperands().get(0)));   //"i32* @c"

        return stringBuilder.toString();
    }
}
