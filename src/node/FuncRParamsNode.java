package node;

import token.Token;

import java.util.ArrayList;

/**
 * @Description 函数实参表 FuncRParams → Exp { ',' Exp }
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

    @Override
    public void print(){
        expNodes.get(0).print();
        for(int i=0; i<commaTokens.size(); i++){
            commaTokens.get(i).print();
            expNodes.get(i+1).print();
        }
        printNodeType();
    }
}
