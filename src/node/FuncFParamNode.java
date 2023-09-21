package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
 * @Author
 * @Date 2023/9/20
 **/
public class FuncFParamNode extends Node{
    private BTypeNode bTypeNode;
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<Token> rbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;

    public FuncFParamNode(BTypeNode bTypeNode, Token identToken, ArrayList<Token> lbrackTokens, ArrayList<Token> rbrackTokens, ArrayList<ConstExpNode> constExpNodes) {
        super(NodeType.FuncFParam);
        this.bTypeNode = bTypeNode;
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.rbrackTokens = rbrackTokens;
        this.constExpNodes = constExpNodes;
    }

    @Override
    public void print() {
        bTypeNode.print();
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            lbrackTokens.get(0).print();
            rbrackTokens.get(0).print();
            if(!constExpNodes.isEmpty()){
                for(int i=0; i<constExpNodes.size(); i++){
                    lbrackTokens.get(i+1).print();
                    constExpNodes.get(i).print();
                    rbrackTokens.get(i+1).print();
                }
            }
        }
        printNodeType();
    }
}
