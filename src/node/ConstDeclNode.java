package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class ConstDeclNode {
    private Token constToken;
    private BTypeNode bTypeNode;
    private ConstDefNode constDefNode;
    private ArrayList<Token> commaTokens;
    private ArrayList<ConstDefNode> constDefNodes;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ConstDefNode constDefNode, ArrayList<Token> commaTokens, ArrayList<ConstDefNode> constDefNodes, Token semicnToken) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNode = constDefNode;
        this.commaTokens = commaTokens;
        this.constDefNodes = constDefNodes;
        this.semicnToken = semicnToken;
    }
}
