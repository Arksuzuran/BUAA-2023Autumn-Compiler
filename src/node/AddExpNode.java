package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class AddExpNode extends Node{
    private MulExpNode mulExpNode;
    private Token opToken;
    private AddExpNode addExpNode;

    public AddExpNode(MulExpNode mulExpNode, Token opToken, AddExpNode addExpNode) {
        super(NodeType.AddExp);
        this.mulExpNode = mulExpNode;
        this.opToken = opToken;
        this.addExpNode = addExpNode;
    }
}
