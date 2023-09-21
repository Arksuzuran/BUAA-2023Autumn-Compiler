package node;

/**
 * @Description 条件表达式 Cond → LOrExp
 * @Author
 * @Date 2023/9/20
 **/
public class CondNode extends Node{
    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        super(NodeType.Cond);
        this.lOrExpNode = lOrExpNode;
    }

    @Override
    public void print(){
        lOrExpNode.print();
        printNodeType();
    }
}
