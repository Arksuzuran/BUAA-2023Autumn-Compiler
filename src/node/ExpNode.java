package node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class ExpNode extends Node{
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        super(NodeType.Exp);
        this.addExpNode = addExpNode;
    }
}
