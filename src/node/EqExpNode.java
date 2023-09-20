package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/21
 **/
public class EqExpNode extends Node{
    private RelExpNode relExpNode;
    private Token opToken;
    private EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode, Token opToken, EqExpNode eqExpNode) {
        super(NodeType.EqExp);
        this.relExpNode = relExpNode;
        this.opToken = opToken;
        this.eqExpNode = eqExpNode;
    }
}
