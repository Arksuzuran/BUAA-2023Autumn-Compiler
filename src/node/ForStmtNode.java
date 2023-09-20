package node;

import token.Token;
import token.TokenType;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class ForStmtNode extends Node{
   private LValNode lValNode;
   private Token assignToken;
   private ExpNode expNode;

    public ForStmtNode(LValNode lValNode, Token assignToken, ExpNode expNode) {
        super(NodeType.ForStmt);
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
    }
}
