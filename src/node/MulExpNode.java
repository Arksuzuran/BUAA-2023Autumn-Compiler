package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/21
 **/
public class MulExpNode extends Node{
    private UnaryExpNode unaryExpNode;
    private Token opToken;
    private MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.MulExp);
        this.unaryExpNode = unaryExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }
}
