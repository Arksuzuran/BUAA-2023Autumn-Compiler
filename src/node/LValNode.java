package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class LValNode extends Node{
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> rbrackTokens;

    public LValNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ExpNode> expNodes, ArrayList<Token> rbrackTokens) {
        super(NodeType.LVal);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.expNodes = expNodes;
        this.rbrackTokens = rbrackTokens;
    }
}
