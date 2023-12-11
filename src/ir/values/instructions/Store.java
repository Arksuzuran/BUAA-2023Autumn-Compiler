package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.operands.MipsOperand;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

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
    public Value getValue(){
        return getOperands().get(0);
    }
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("store ");
        IrTool.appendSBParamList(stringBuilder, getOperands());
//        System.out.println("store指令：" + getOperands());
        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        MipsOperand src = MipsBuilder.buildOperand(getValue(), false, Mc.curIrFunction, getParent());
        MipsOperand base = MipsBuilder.buildOperand(getPointer(), false, Mc.curIrFunction, getParent());
        MipsOperand offset = MipsBuilder.buildImmOperand(0, true, Mc.curIrFunction, getParent());
        MipsBuilder.buildStore(src, base, offset, getParent());
    }
}
