package node;

import token.Token;
import utils.IO;

import java.util.ArrayList;

/**
 * @Description 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
 * @Author
 * @Date 2023/9/19
 **/
public class ConstDeclNode extends Node{
    private Token constToken;
    private BTypeNode bTypeNode;
    private ConstDefNode constDefNode;
    private ArrayList<Token> commaTokens;
    private ArrayList<ConstDefNode> constDefNodes;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ConstDefNode constDefNode, ArrayList<Token> commaTokens, ArrayList<ConstDefNode> constDefNodes, Token semicnToken) {
        super(NodeType.ConstDecl);
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNode = constDefNode;
        this.commaTokens = commaTokens;
        this.constDefNodes = constDefNodes;
        this.semicnToken = semicnToken;
    }

    @Override
    public void print() {
        constToken.print();
        bTypeNode.print();
        constDefNode.print();
        for(int i=0; i<commaTokens.size(); i++){
            commaTokens.get(i).print();
            constDefNodes.get(i).print();
        }
        semicnToken.print();
        printNodeType();
    }
}
