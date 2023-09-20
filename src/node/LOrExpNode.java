package node;

import token.Token;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class LOrExpNode extends Node{
   private LAndExpNode lAndExpNode;
   private Token opToken;
   private LOrExpNode lOrExpNode;

    public LOrExpNode(LAndExpNode lAndExpNode, Token opToken, LOrExpNode lOrExpNode) {
        super(NodeType.LOrExp);
        this.lAndExpNode = lAndExpNode;
        this.opToken = opToken;
        this.lOrExpNode = lOrExpNode;
    }
}
