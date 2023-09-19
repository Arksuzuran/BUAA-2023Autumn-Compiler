package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description  VarDecl → BType VarDef { ',' VarDef } ';'
 * @Author
 * @Date 2023/9/19
 **/
public class VarDeclNode {
    private BTypeNode bTypeNode;
    private ArrayList<VarDefNode> varDefNodes;
    private ArrayList<Token> commas;
    private Token semicnToken;

    public VarDeclNode(BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commas, Token semicnToken) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semicnToken = semicnToken;
    }
}
