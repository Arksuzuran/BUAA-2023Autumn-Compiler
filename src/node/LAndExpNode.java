package node;

import ir.IrBuilder;
import ir.Irc;
import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Icmp;
import token.Token;

/**
 * @Description 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
 * @Author
 * @Date 2023/9/21
 **/
public class LAndExpNode extends Node{
    private EqExpNode eqExpNode;
    private Token opToken;
    private LAndExpNode lAndExpNode;
    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;

    public LAndExpNode(EqExpNode eqExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LAndExp);
        this.eqExpNode = eqExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }

    // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    @Override
    public void print(){
        if(opToken == null){
            eqExpNode.print();
        } else {
            lAndExpNode.print();
            opToken.print();
            eqExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            eqExpNode.check();
        } else {
            lAndExpNode.check();
            eqExpNode.check();
        }
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp
    /**
     * 短路求值：or
     * 在短路求值中，所有条件判断都被拆分，并被转化为基本块间的跳转关系。
     * 因此LOrExp的作用是为下层的LAnd构建跳转块。
     *
     * 对于LAndExp '&&' EqExp来说：
     * 如果LAndExp为假，那么直接跳转到falseBranch即可。
     * 如果LAndExp为真，那么应该创建并跳转到一个新的块(Short-circuit evaluation)，在该基本块内退化为处理单EqExp的情形
     *
     * 已经到达求值的最高基本单元EqExp，需要根据EqExp来构造Br指令
     */
    @Override
    public void buildIr() {
        // LAndExp → EqExp
        if(opToken == null){
            handleSingleEqExp();
        }
        // LAndExp → LAndExp '&&' EqExp
        else {
            BasicBlock scBranch = IrBuilder.buildBasicBlock(Irc.curFunction);
            lAndExpNode.setTrueBranch(scBranch);
            lAndExpNode.setFalseBranch(falseBranch);
            lAndExpNode.buildIr();

            // 切换到新建的scBranch块内，在这个块内构建EqExp
            Irc.curBlock = scBranch;
            handleSingleEqExp();
        }
    }

    /**
     * LAndExp → EqExp
     * @synValue 无
     */
    public void handleSingleEqExp(){
        eqExpNode.buildIr();
        // Br指令要求condition的类型为i1
        // 而下层可以直接到达AddExp，其结果可能是i32
        // 对于i32，判断其是否非0，以转化为i1
        IntType valueType= (IntType) Irc.synValue.getType();
        Value condition = Irc.synValue;
        if(!valueType.isI1()){
            condition = IrBuilder.buildIcmpInstruction(condition, new ConstInt(valueType.getBits(), 0), Icmp.CondType.NEQ, Irc.curBlock);
        }
        IrBuilder.buildBrInstruction(condition, trueBranch, falseBranch, Irc.curBlock);
        // 而我们从来没有new过ConstInt(1, value)，这意味着一切ConstInt在此处都被转化为了Icmp
        // 这意味着在生产目标代码时，带条件Br语句的condition永远是Icmp类型
    }
}
