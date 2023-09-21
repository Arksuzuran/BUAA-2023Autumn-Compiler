package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description 左值表达式 LVal → Ident {'[' Exp ']'}
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

    @Override
    public void print(){
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            for(int i=0; i<lbrackTokens.size(); i++){
                lbrackTokens.get(i).print();
                expNodes.get(i).print();
                rbrackTokens.get(i).print();
            }
        }
        printNodeType();
    }
}
