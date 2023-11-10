package node;

import ir.values.BasicBlock;

/**
 * @Description 条件表达式 Cond → LOrExp
 * @Author
 * @Date 2023/9/20
 **/
public class CondNode extends Node{
    private LOrExpNode lOrExpNode;
    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }
    private BasicBlock trueBranch;
    private BasicBlock falseBranch;

    public CondNode(LOrExpNode lOrExpNode) {
        super(NodeType.Cond);
        this.lOrExpNode = lOrExpNode;
    }

    @Override
    public void print(){
        lOrExpNode.print();
        printNodeType();
    }

    @Override
    public void check() {
        lOrExpNode.check();
    }

    @Override
    public void buildIr() {
        lOrExpNode.setTrueBranch(trueBranch);
        lOrExpNode.setFalseBranch(falseBranch);
        lOrExpNode.buildIr();
    }
}
