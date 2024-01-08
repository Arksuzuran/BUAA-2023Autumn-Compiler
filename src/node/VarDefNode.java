package node;

import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.Irc;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.ZeroInitializer;
import ir.values.instructions.Alloca;
import symbol.NumSymbol;
import symbol.SymbolTableStack;
import symbol.SymbolType;
import token.Token;
import error.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
 * @Author HIKARI
 * @Date 2023/9/19
 **/
public class VarDefNode extends Node{
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rbrackTokens;
    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rbrackTokens, Token assignToken, InitValNode initValNode) {
        super(NodeType.VarDef);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.constExpNodes = constExpNodes;
        this.rbrackTokens = rbrackTokens;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    @Override
    public void print() {
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            for (int i=0; i<lbrackTokens.size(); i++){
                lbrackTokens.get(i).print();
                constExpNodes.get(i).print();
                rbrackTokens.get(i).print();
            }
        }
        if(assignToken != null){
            assignToken.print();
            initValNode.print();
        }
        printNodeType();
    }

    // 变量定义    VarDef → Ident { '[' ConstExp ']' } // b
    //                  | Ident { '[' ConstExp ']' } '=' InitVal // k
    @Override
    public void check() {
        // 检查重名 由于一行内最多只有一个错误 因此如果有重名错误 那么就不必再进行任何其他检查
        if(ErrorCheckTool.judgeAndHandleDuplicateError(identToken)){
            // 无重名 加入符号表
            NumSymbol numSymbol = new NumSymbol(identToken.str, SymbolType.Var, identToken.lineNum, this, constExpNodes.size());
            SymbolTableStack.addSymbolToPeek(numSymbol);
            // 继续向下检查
            for(ConstExpNode constExpNode : constExpNodes){
                constExpNode.check();
            }
            if(initValNode != null){
                initValNode.check();
            }
        }

    }

    /**
     * 完成变量定义的声明、赋初值工作
     * @synValue        无
     * @synValueArray   无
     */
    @Override
    public void buildIr() {
        // 1.非数组变量
        if(constExpNodes.isEmpty()){
            // 1.1 全局非数组变量
            if(IrSymbolTableStack.isBuildingGlobalSymbolTable()){
                // 文法约束：
                // 1.全局变量的初值一定是constExp
                // 2.未初始化的全局变量初值是0
                ConstInt initVal;
                if(initValNode != null){
                    Irc.isBuildingConstExp = true;
                    initValNode.buildIr();
                    Irc.isBuildingConstExp = false;
                    initVal = (ConstInt) Irc.synValue;
//                    System.out.println("构建全局非数组变量 " + identToken.str + Irc.synValue);
                } else {
                    initVal = new ConstInt(32, 0);
                }
                IrBuilder.buildGlobalVariable(identToken.str, false, initVal);
            }
            // 1.2 局部非数组变量
            else{
                // 分配空间
                Alloca alloca = IrBuilder.buildAllocaInstruction(new IntType(32), Irc.curBlock);
                // <name, pointer> 加入符号表
                IrSymbolTableStack.addSymbolToPeek(identToken.str, alloca);
                // 有初值 那么进行store
                if(initValNode != null){
                    initValNode.buildIr();
                    IrBuilder.buildStoreInstruction(Irc.synValue ,alloca, Irc.curBlock);
                }
                // 无初值也不必分配 其值未知
            }
        }
        // 2.数组变量
        else{
            // 解析维数信息
            ArrayList<Integer> dims = new ArrayList<>();
            for(ConstExpNode constExpNode : constExpNodes){
                constExpNode.buildIr();
                dims.add(Irc.synInt);
            }
            ArrayType arrayType = new ArrayType(new IntType(32), dims);

            // 2.1 全局数组变量
            if(IrSymbolTableStack.isBuildingGlobalSymbolTable()){
                // 有初始值
                // 处理类似于全局数组常量，直接使用ConstArray进行初始化
                // ConstArray通过synValue传递上来
                if(initValNode != null){
                    initValNode.setDims(dims);
                    // 全局变量初始化一定为constExp
                    Irc.isBuildingConstExp = true;
                    initValNode.buildIr();
                    Irc.isBuildingConstExp = false;
                    IrBuilder.buildGlobalVariable(identToken.str, false, (ConstArray) Irc.synValue);
                }
                // 无初始值
                // 使用ZeroInitializer
                else{
                    ZeroInitializer zeroInitializer = new ZeroInitializer(arrayType);
                    IrBuilder.buildGlobalVariable(identToken.str, false, zeroInitializer);
                }
            }
            // 2.2 局部数组变量
            else{
                // 分配空间
                Alloca arrayPointer = IrBuilder.buildAllocaInstruction(arrayType, Irc.curBlock);
                // 加入符号表
                IrSymbolTableStack.addSymbolToPeek(identToken.str, arrayPointer);

                // 有初始值
                // 处理类似于局部常量数组
                // FlattenArray通过syvValueArray传递上来
                if(initValNode != null){
                    initValNode.setDims(dims);
                    initValNode.buildIr();
                    // 使用store和gep将展平后的内容数组存入数组
                    IrBuilder.buildStoreWithValuesIntoArray(arrayPointer, dims, Irc.synValueArray, Irc.curBlock);
                }
                // 无初始值，不予处理
            }
        }
    }
}
