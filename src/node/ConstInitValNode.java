package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
 * @Author H1KARI
 * @Date 2023/9/19
 **/
public class ConstInitValNode {
    private ConstExpNode constExpNode;
    private Token lbraceToken;
    // 需注意，constInitValNodes比 commas长1
    private ArrayList<ConstInitValNode> constInitValNodes;
    private ArrayList<Token> commas;
    private Token rbraceToken;

    public ConstInitValNode(ConstExpNode constExpNode, Token lbraceToken, ArrayList<ConstInitValNode> constInitValNodes, ArrayList<Token> commas, Token rbraceToken) {
        this.constExpNode = constExpNode;
        this.lbraceToken = lbraceToken;
        this.constInitValNodes = constInitValNodes;
        this.commas = commas;
        this.rbraceToken = rbraceToken;
    }
}
