package node;

/**
 * @Description 表达式 Exp → AddExp
 * @Author
 * @Date 2023/9/20
 **/
public class ExpNode extends Node{
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        super(NodeType.Exp);
        this.addExpNode = addExpNode;
    }

    @Override
    public void print(){
        addExpNode.print();
        printNodeType();
    }

    @Override
    public void check() {
        addExpNode.check();
    }

    public int getDim() {
        return addExpNode.getDim();
    }
}
