package node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class ConstExpNode extends Node{
    private AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        super(NodeType.ConstExp);
        this.addExpNode = addExpNode;
    }
}
