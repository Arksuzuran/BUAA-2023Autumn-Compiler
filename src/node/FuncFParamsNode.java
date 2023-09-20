package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class FuncFParamsNode extends Node{
    private ArrayList<FuncFParamNode> funcFParamNodes;
    private ArrayList<Token> commaTokens;

    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commaTokens) {
        super(NodeType.FuncFParams);
        this.funcFParamNodes = funcFParamNodes;
        this.commaTokens = commaTokens;
    }
}
