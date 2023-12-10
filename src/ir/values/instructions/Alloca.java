package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.operands.RegType;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.User;
import ir.values.Value;
import ir.values.constants.ConstArray;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 内存申请指令  该指令对应的Value应当是指针类型
 * <result> = alloca <type>
 * @Author
 * @Date 2023/10/30
 **/
public class Alloca extends Instruction {
    public ConstArray getInitValue() {
        return initArray;
    }
    public ValueType getAllocaedType() {
        return allocaedType;
    }
    /**
     * 处理局部常量数组的场合
     */
    private ConstArray initArray = null;
    private ValueType allocaedType = null;

    /**
     * alloca 指令没有操作数
     *
     * @param name         指令Value的名称
     * @param pointingType 要指向的类型
     * @param parent       parent一定是BasicBlock
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent) {
        super(name, new PointerType(pointingType), parent);
        allocaedType = pointingType;
    }

    /**
     * 用于处理带初值的数组常量
     * 该常量的内容被写入了alloca的地址
     * 因此该初值也由alloca对象来保存
     *
     * @param initArray 常量初值
     */
    public Alloca(String name, ValueType pointingType, BasicBlock parent, ConstArray initArray) {
        super(name, new PointerType(pointingType), parent);
        this.initArray = initArray;
        allocaedType = pointingType;
    }

    /**
     * 只要是没有使用 gep 的，都可以在 mem2reg 中被提升
     */
    public boolean canPromotable() {
        if (getUsers().isEmpty()) {
            return true;
        }
        for (User user : getUsers()) {
            if (user instanceof GetElementPtr getElementPtr) {
                if (getElementPtr.getOp(1) == this) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return getName() + " = alloca " + allocaedType;
    }

    @Override
    public void buildMips() {
        MipsFunction curFunction = Mc.f(Mc.curIrFunction);
        // 在栈上已经分配出的空间
        int allocaedSize = curFunction.getAllocaSize();
        MipsOperand allocaedSizeOperand = MipsBuilder.buildImmOperand(allocaedSize, true, Mc.curIrFunction, getParent());
        // 记录 分配出指向类型那么多的空间
        int newSize = allocaedType.getSize();
        curFunction.addAllocaSize(newSize);
//        System.out.println("在函数分配空间" + curFunction.getName() + ", newSize:" + newSize+ ", name:" + this);

        // 向当前Alloca指令对应的Mips对象内，存入分配好的空间的首地址，即一开始的allocaedSize
        // 栈在一开始就已经分配好了空间，这里只需要向上生长即可
        MipsOperand dst = MipsBuilder.buildOperand(this, true, Mc.curIrFunction, getParent());
        MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, MipsRealReg.SP, allocaedSizeOperand, getParent());
    }
}
