package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.operands.MipsOperand;
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

    /**
     * 获取指令中的地址操作数
     * @return  地址操作数的value对象
     */
    public Value getPointer(){
        return getOperands().get(0);
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

    @Override
    public void buildMips() {
        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
        MipsOperand base = MipsBuilder.buildOperand(getOp(1), false, Mc.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, Mc.curIrFunction, getParent());
        MipsBuilder.buildLoad(dst, base, offset, getParent());
    }
}
