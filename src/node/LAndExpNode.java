package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/21
 **/
public class LAndExpNode extends Node{
    private EqExpNode eqExpNode;
    private Token opToken;
    private LAndExpNode lAndExpNode;

    public LAndExpNode(EqExpNode eqExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LAndExp);
        this.eqExpNode = eqExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }
}
