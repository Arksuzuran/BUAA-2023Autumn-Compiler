package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/21
 **/
public class RelExpNode extends Node{
    private AddExpNode addExpNode;
    private Token opToken;
    private RelExpNode relExpNode;

    public RelExpNode(AddExpNode addExpNode, Token opToken, RelExpNode relExpNode) {
        super(NodeType.RelExp);
        this.addExpNode = addExpNode;
        this.opToken = opToken;
        this.relExpNode = relExpNode;
    }
}
