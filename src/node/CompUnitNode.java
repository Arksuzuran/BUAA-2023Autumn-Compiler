package node;

import utils.IO;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class CompUnitNode extends Node{
    private ArrayList<DeclNode> declNodes;
    private ArrayList<FuncDefNode> funcDefNodes;
    private MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(ArrayList<DeclNode> declNodes, ArrayList<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        super(NodeType.CompUnit);
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }


    @Override
    public void print() {
        if(!declNodes.isEmpty()){
            for(DeclNode declNode : declNodes){
                declNode.print();
            }
        }
        if(!funcDefNodes.isEmpty()){
            for (FuncDefNode funcDefNode : funcDefNodes){
                funcDefNode.print();
            }
        }
        mainFuncDefNode.print();
        printNodeType();
    }

    // 最后输出时不加回车
    @Override
    public void printNodeType(){
        IO.write(type.getOutputString(), true, false);
    }
}
