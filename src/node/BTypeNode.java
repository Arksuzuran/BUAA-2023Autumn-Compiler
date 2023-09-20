package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class BTypeNode extends Node{
    private Token BTypeToken;

    public BTypeNode(Token BTypeToken) {
        super(NodeType.BType);
        this.BTypeToken = BTypeToken;
    }
}
