package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description  VarDecl → BType VarDef { ',' VarDef } ';'
 * @Author
 * @Date 2023/9/19
 **/
public class VarDeclNode extends Node{
    private BTypeNode bTypeNode;
    private ArrayList<VarDefNode> varDefNodes;
    private ArrayList<Token> commaTokens;
    private Token semicnToken;

    public VarDeclNode(BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commaTokens, Token semicnToken) {
        super(NodeType.VarDecl);
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commaTokens = commaTokens;
        this.semicnToken = semicnToken;
    }
}
