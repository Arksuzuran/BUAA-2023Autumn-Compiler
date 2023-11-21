package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsCondType;
import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
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

    @Override
    public void buildMips() {
        // 将关于跳转的块 转换为mips块
        MipsBlock curBlock = Mc.b(getParent()); // 当前块
        // 有条件跳转
        if(conditional){
            if(getOp(1) instanceof ConstInt){
                System.out.println("[Br] 错误：条件为ConstInt");
            }
            // 将关于跳转的块 转换为mips块
            MipsBlock trueBlock = Mc.b((BasicBlock) getOp(2));      // 真跳转块
            MipsBlock falseBlock = Mc.b((BasicBlock) getOp(3));     // 假跳转块

            Icmp condition = (Icmp) getOp(1);   // ir跳转条件类
            // 获得具体的跳转条件
            MipsCondType type = MipsCondType.IrCondType2MipsCondType(condition.getCondType());
            // 获得两个比较数
            MipsOperand src1 = MipsBuilder.buildOperand(condition.getOp(1), false, Mc.curIrFunction, getParent());
            MipsOperand src2 = MipsBuilder.buildOperand(condition.getOp(2), true, Mc.curIrFunction, getParent());
            // 将trueBlock设置为跳转地址
            MipsBuilder.buildBranch(type, src1, src2, trueBlock, getParent());
            // 登记后续块
            curBlock.setTrueSucBlock(trueBlock);
            curBlock.setFalseSucBlock(falseBlock);
        }
        // 无条件跳转指令
        else{
            MipsBlock targetBlock = Mc.b((BasicBlock) getOp(1));
            MipsBuilder.buildBranch(targetBlock, getParent());
            // 登记后继块
            curBlock.setTrueSucBlock(targetBlock);
        }
    }
}
