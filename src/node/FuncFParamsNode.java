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
    public ArrayList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }
    @Override
    public void print() {
        funcFParamNodes.get(0).print();
        if(!commaTokens.isEmpty()){
            for(int i=0; i<commaTokens.size(); i++){
                commaTokens.get(i).print();
                funcFParamNodes.get(i+1).print();
            }
        }
        printNodeType();
    }

    // 函数形参表   FuncFParams → FuncFParam { ',' FuncFParam }
    @Override
    public void check() {
        for(FuncFParamNode funcFParamNode : funcFParamNodes){
            funcFParamNode.check();
        }
    }
}
