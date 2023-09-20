package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class NumberNode extends Node{
    private Token intConstToken;

    public NumberNode(Token intConstToken) {
        super(NodeType.Number);
        this.intConstToken = intConstToken;
    }
}
