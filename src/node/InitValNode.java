package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class InitValNode extends Node{
    private ExpNode expNode;
    private Token lbraceToken;
    private ArrayList<InitValNode> initValNodes;
    private ArrayList<Token> commaTokens;
    private Token rbraceToken;

    public InitValNode(ExpNode expNode, Token lbraceToken, ArrayList<InitValNode> initValNodes, ArrayList<Token> commaTokens, Token rbraceToken) {
        super(NodeType.InitVal);
        this.expNode = expNode;
        this.lbraceToken = lbraceToken;
        this.initValNodes = initValNodes;
        this.commaTokens = commaTokens;
        this.rbraceToken = rbraceToken;
    }
}
