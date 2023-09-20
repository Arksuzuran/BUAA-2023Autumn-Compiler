package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class UnaryOpNode extends Node{
    private Token opToken;

    public UnaryOpNode(Token opToken) {
        super(NodeType.UnaryOp);
        this.opToken = opToken;
    }
}
