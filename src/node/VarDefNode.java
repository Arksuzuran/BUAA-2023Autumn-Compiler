package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description VarDef → Ident { '[' ConstExp ']' }
 * @Author HIKARI
 * @Date 2023/9/19
 **/
public class VarDefNode extends Node{
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rbrackTokens;
    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rbrackTokens, Token assignToken, InitValNode initValNode) {
        super(NodeType.VarDef);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.constExpNodes = constExpNodes;
        this.rbrackTokens = rbrackTokens;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    @Override
    public void print() {
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            for (int i=0; i<lbrackTokens.size(); i++){
                lbrackTokens.get(i).print();
                constExpNodes.get(i).print();
                rbrackTokens.get(i).print();
            }
        }
        if(assignToken != null){
            assignToken.print();
            initValNode.print();
        }
        printNodeType();
    }
}
