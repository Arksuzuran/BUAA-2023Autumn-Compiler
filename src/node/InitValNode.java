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

    @Override
    public void print() {
        if(expNode != null){
            expNode.print();
        }
        else{
            lbraceToken.print();
            if(!initValNodes.isEmpty()){
                initValNodes.get(0).print();
                if(!commaTokens.isEmpty()){
                    for(int i=0; i<commaTokens.size(); i++){
                        commaTokens.get(i).print();
                        initValNodes.get(i+1).print();
                    }
                }
            }
            rbraceToken.print();
        }
        printNodeType();
    }

    // 变量初值    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    @Override
    public void check() {
        if(expNode != null){
            expNode.check();
        } else {
            for(InitValNode initValNode : initValNodes){
                initValNode.check();
            }
        }
    }
}
