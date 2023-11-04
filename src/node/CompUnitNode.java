package node;

import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.VoidType;
import ir.values.Function;
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
    public void buildIr() {
        // 在顶部先声明库函数
        Function.getint = IrBuilder.buildFunction("getint", new IntType(32), new ArrayList<>(), true);
        Function.putstr = IrBuilder.buildFunction("putstr", new VoidType(), new ArrayList<>(){{
            add(new PointerType(new IntType(8)));
        }}, true);
        Function.putint = IrBuilder.buildFunction("putint", new VoidType(), new ArrayList<>(){{
            add(new IntType(32));
        }}, true);
        Function.putch = IrBuilder.buildFunction("putch", new VoidType(), new ArrayList<>(){{
            add(new IntType(32));
        }}, true);

        // 设置全局符号表
        IrSymbolTableStack.push();

        // 访问子节点
        if(!declNodes.isEmpty()){
            for(DeclNode declNode : declNodes){
                declNode.buildIr();
            }
        }
        if(!funcDefNodes.isEmpty()){
            for (FuncDefNode funcDefNode : funcDefNodes){
                funcDefNode.buildIr();
            }
        }
        mainFuncDefNode.buildIr();
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
