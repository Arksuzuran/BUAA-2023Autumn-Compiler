package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class FuncRParamsNode extends Node{
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> commaTokens;

    public FuncRParamsNode(ArrayList<ExpNode> expNodes, ArrayList<Token> commaTokens) {
        super(NodeType.FuncRParams);
        this.expNodes = expNodes;
        this.commaTokens = commaTokens;
    }
}
