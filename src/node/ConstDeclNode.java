package node;

import ir.IrSymbolTableStack;
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
    private ArrayList<Token> commaTokens;
    private ArrayList<ConstDefNode> constDefNodes;
    private Token semicnToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ArrayList<Token> commaTokens, ArrayList<ConstDefNode> constDefNodes, Token semicnToken) {
        super(NodeType.ConstDecl);
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.commaTokens = commaTokens;
        this.constDefNodes = constDefNodes;
        this.semicnToken = semicnToken;
    }

    @Override
    public void print() {
        constToken.print();
        bTypeNode.print();
        constDefNodes.get(0).print();
        for(int i=0; i<commaTokens.size(); i++){
            commaTokens.get(i).print();
            constDefNodes.get(i+1).print();
        }
        semicnToken.print();
        printNodeType();
    }

    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
    @Override
    public void check() {
        for(ConstDefNode constDefNode : constDefNodes){
            constDefNode.check();
        }
    }

    /**
     * 构建常量变量声明
     */
    @Override
    public void buildIr() {
        for(ConstDefNode constDefNode : constDefNodes){
            constDefNode.buildIr();
        }
    }
}
