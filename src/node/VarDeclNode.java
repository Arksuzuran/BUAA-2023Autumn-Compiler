package node;

import token.Token;
import token.TokenType;
import utils.IO;

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

    @Override
    public void print() {
        bTypeNode.print();
        varDefNodes.get(0).print();
        for(int i=0; i<commaTokens.size(); i++){
            commaTokens.get(i).print();
            varDefNodes.get(i+1).print();
        }
        semicnToken.print();
        printNodeType();
    }

    // 变量声明    VarDecl → BType VarDef { ',' VarDef } ';' // i
    @Override
    public void check() {
        for(VarDefNode varDefNode : varDefNodes){
            varDefNode.check();
        }
    }
}
