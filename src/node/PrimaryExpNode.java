package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class PrimaryExpNode extends Node{
    private Token lparentToken;
    private ExpNode expNode;
    private Token rparentToken;
    private LValNode lValNode;
    private NumberNode numberNode;

    public PrimaryExpNode(Token lparentToken, ExpNode expNode, Token rparentToken, LValNode lValNode, NumberNode numberNode) {
        super(NodeType.PrimaryExp);
        this.lparentToken = lparentToken;
        this.expNode = expNode;
        this.rparentToken = rparentToken;
        this.lValNode = lValNode;
        this.numberNode = numberNode;
    }
}
