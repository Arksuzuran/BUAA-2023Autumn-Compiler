package node;

/**
 * @Description 常量表达式 ConstExp → AddExp
 * @Author
 * @Date 2023/9/19
 **/
public class ConstExpNode extends Node{
    private AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        super(NodeType.ConstExp);
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
}
