package node;

import symbol.SymbolTable;
import symbol.SymbolTableStack;
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
    public void check() {
        // 要新建根符号表 采取SymbolTable的单例
        SymbolTable root = SymbolTable.getRootSymbolTable();
        SymbolTableStack.push(root);

        if(!declNodes.isEmpty()){
            for(DeclNode declNode : declNodes){
                declNode.check();
            }
        }
        if(!funcDefNodes.isEmpty()){
            for (FuncDefNode funcDefNode : funcDefNodes){
                funcDefNode.check();
            }
        }
        mainFuncDefNode.check();
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
}
