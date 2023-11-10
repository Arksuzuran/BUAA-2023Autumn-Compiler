package ir.values.instructions;

import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

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
    public Br(BasicBlock parent, BasicBlock target) {
        super("", new VoidType(), parent, target);
        conditional = false;
    }

    /**
     * 有条件跳转
     * @param parent        parent一定是BasicBlock
     * @param condition     跳转条件
     * @param trueBranch    条件成立时跳转到的基本块
     * @param falseBranch   条件不成立时跳转到的基本块
     */
    public Br(BasicBlock parent, Value condition, BasicBlock trueBranch, BasicBlock falseBranch) {
        super("", new VoidType(), parent, condition, trueBranch, falseBranch);
        conditional = true;
    }

    @Override
    public String toString(){
        if(conditional){
            return "br "
                    + IrTool.tnstr(getOperands().get(0)) + ", "
                    + IrTool.tnstr(getOperands().get(1)) + ", "
                    + IrTool.tnstr(getOperands().get(2));
        } else {
            return "br " + IrTool.tnstr(getOperands().get(0));
        }
    }
}
