package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class ConstDefNode extends Node{
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rbrackTokens;
    private Token assignToken;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rbrackTokens, Token assignToken, ConstInitValNode constInitValNode) {
        super(NodeType.ConstDef);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.constExpNodes = constExpNodes;
        this.rbrackTokens = rbrackTokens;
        this.assignToken = assignToken;
        this.constInitValNode = constInitValNode;
    }
}
