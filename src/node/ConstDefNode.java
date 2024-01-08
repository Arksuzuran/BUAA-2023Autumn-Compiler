package node;

import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.Irc;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.constants.ConstArray;
import ir.values.instructions.Alloca;
import symbol.NumSymbol;
import symbol.SymbolTableStack;
import symbol.SymbolType;
import token.Token;
import error.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
 * @Author
 * @Date 2023/9/19
 **/
public class ConstDefNode extends Node{
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rbrackTokens;
    private Token assignToken;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rbrackTokens, Token assignToken, ConstInitValNode constInitValNode) {
        super(NodeType.ConstDef);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.constExpNodes = constExpNodes;
        this.rbrackTokens = rbrackTokens;
        this.assignToken = assignToken;
        this.constInitValNode = constInitValNode;
    }

    @Override
    public void print() {
        identToken.print();
        for(int i=0; i<lbrackTokens.size(); i++){
            lbrackTokens.get(i).print();
            constExpNodes.get(i).print();
            rbrackTokens.get(i).print();
        }
        assignToken.print();
        constInitValNode.print();
        printNodeType();
    }

    // 常数定义    ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal  // b k
    @Override
    public void check() {
        // 先检测是否存在重复定义
        // 不存在重复定义 那么加入栈顶符号表中
        if(ErrorCheckTool.judgeAndHandleDuplicateError(identToken)){
            NumSymbol numSymbol = new NumSymbol(identToken.str, SymbolType.Const, identToken.lineNum, this, constExpNodes.size());
            SymbolTableStack.addSymbolToPeek(numSymbol);
            // 继续向下检查
            for(ConstExpNode constExpNode : constExpNodes){
                constExpNode.check();
            }
            constInitValNode.check();
        }
    }

    /**
     * 常量的定义
     * 完成常量的声明，初始化
     * 非数组常量、全局常量的初始化都记录在对象中，而不付诸中间代码
     * 局部数组常量的初始化则要生成store与GEP的中间代码
     * @synValue        无
     * @synValueArray   无
     */
    @Override
    public void buildIr() {
        // 1.非数组常量
        // Ident '=' ConstInitVal
        // 仅记录在符号表中，不在中间代码进行声明
        if(constExpNodes.isEmpty()){
            constInitValNode.buildIr(); // 生成对应Value对象
            IrSymbolTableStack.addSymbolToPeek(identToken.str, Irc.synValue);
        }
        // 2.数组常量
        // Ident { '[' ConstExp ']' } '=' ConstInitVal
        else{
            // 解析维数信息
            ArrayList<Integer> dims = new ArrayList<>();
            for(ConstExpNode constExpNode : constExpNodes){
                constExpNode.buildIr();
                dims.add(Irc.synInt);
            }
            // 向下传递维数信息
            constInitValNode.setDims(dims);
            // 获取常量初始化值
            // 对于全局常量数组，仅在synValue内存储一个ConstArray
            // 对于局部变量数组，在synValue内存储一个ConstArray，在synValueArray内存储展平后的基本元素
            constInitValNode.buildIr();

            // 根据当前是否是全局变量，进行不同处理

            // 2.1 全局常量数组
            // 初值只需要存储在GlobalVariable对象中即可
            if(IrSymbolTableStack.isBuildingGlobalSymbolTable()){
                IrBuilder.buildGlobalVariable(identToken.str, true, (ConstArray) Irc.synValue);
            }
            // 2.2 局部常量数组
            // 需要手动进行store, gep和store
            // 下部会传入构造好的常量数组synValue，以及展平后的synArray
            else{
                // 首先分配数组空间
                // 数组类型 维数信息存储在其中
                ArrayType arrayType = new ArrayType(new IntType(32), dims);
                // 分配空间 同时传入初值
                Alloca arrayPointer = IrBuilder.buildAllocaInstruction(arrayType, Irc.curBlock, (ConstArray) Irc.synValue);
                // 将该符号与其对应指针存入符号表
                IrSymbolTableStack.addSymbolToPeek(identToken.str, arrayPointer);
                // 使用store和gep将展平后的内容数组存入数组
                IrBuilder.buildStoreWithValuesIntoArray(arrayPointer, dims, Irc.synValueArray, Irc.curBlock);
            }
        }
    }
}
