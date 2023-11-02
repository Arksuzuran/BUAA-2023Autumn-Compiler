package ir.values.instructions;

import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description 跳转指令
 *
 * 有条件跳转：
 * br i1 <cond>, label <iftrue>, label <iffalse>
 *
 * 无条件跳转：
 * br label <dest>
 *
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class Br extends Instruction{
    public boolean isConditional() {
        return conditional;
    }

    private boolean conditional;

    /**
     * 无条件跳转
     * 类型void
     * @param parent   parent一定是BasicBlock
     * @param target  要跳转到的基本块
     */
    public Br(String name, BasicBlock parent, BasicBlock target) {
        super(name, new VoidType(), parent, target);
        conditional = false;
    }

    /**
     * 有条件跳转
     * @param parent        parent一定是BasicBlock
     * @param condition     跳转条件
     * @param trueTarget    条件成立时跳转到的基本块
     * @param falseTarget   条件不成立时跳转到的基本块
     */
    public Br(String name, BasicBlock parent, Value condition, BasicBlock trueTarget, BasicBlock falseTarget) {
        super(name, new VoidType(), parent, condition, trueTarget, falseTarget);
        conditional = true;
    }

}
