package node;

import token.Token;
import utils.IO;

import java.util.ArrayList;

/**
 * @Description ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
 * @Author H1KARI
 * @Date 2023/9/19
 **/
public class ConstInitValNode extends Node{
    private ConstExpNode constExpNode;
    private Token lbraceToken;
    // 需注意，constInitValNodes比 commaTokens长1
    private ArrayList<ConstInitValNode> constInitValNodes;
    private ArrayList<Token> commaTokens;
    private Token rbraceToken;

    public ConstInitValNode(ConstExpNode constExpNode, Token lbraceToken, ArrayList<ConstInitValNode> constInitValNodes, ArrayList<Token> commaTokens, Token rbraceToken) {
        super(NodeType.ConstInitVal);
        this.constExpNode = constExpNode;
        this.lbraceToken = lbraceToken;
        this.constInitValNodes = constInitValNodes;
        this.commaTokens = commaTokens;
        this.rbraceToken = rbraceToken;
    }

    @Override
    public void print() {
        if(lbraceToken != null){
            lbraceToken.print();
            if(!constInitValNodes.isEmpty()){
                constInitValNodes.get(0).print();
                if(!commaTokens.isEmpty()){
                    for(int i=0; i<commaTokens.size(); i++){
                        commaTokens.get(i).print();
                        constInitValNodes.get(i+1).print();
                    }
                }
            }
            rbraceToken.print();
        }
        else if(constExpNode != null){
            constExpNode.print();
        }
        printNodeType();
    }
}
