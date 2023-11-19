package node;

import ir.Irc;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import symbol.NumSymbol;
import symbol.SymbolTableStack;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
 * @Author
 * @Date 2023/9/20
 **/
public class FuncFParamNode extends Node{
    private BTypeNode bTypeNode;
    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<Token> rbrackTokens;
    private ArrayList<ConstExpNode> constExpNodes;

    public FuncFParamNode(BTypeNode bTypeNode, Token identToken, ArrayList<Token> lbrackTokens, ArrayList<Token> rbrackTokens, ArrayList<ConstExpNode> constExpNodes) {
        super(NodeType.FuncFParam);
        this.bTypeNode = bTypeNode;
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.rbrackTokens = rbrackTokens;
        this.constExpNodes = constExpNodes;
    }
    // 获取参数的维数 理应等于左右中括号的组数
    public int getParamDim(){
        return lbrackTokens.size();
    }
    // 获取标识符的token
    public Token getIdentToken() {
        return identToken;
    }
    @Override
    public void print() {
        bTypeNode.print();
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            lbrackTokens.get(0).print();
            rbrackTokens.get(0).print();
            if(!constExpNodes.isEmpty()){
                for(int i=0; i<constExpNodes.size(); i++){
                    lbrackTokens.get(i+1).print();
                    constExpNodes.get(i).print();
                    rbrackTokens.get(i+1).print();
                }
            }
        }
        printNodeType();
    }

    // 函数形参    FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]  //   b k
    @Override
    public void check() {
        // 检测重定义即可
        if(ErrorCheckTool.judgeAndHandleDuplicateError(identToken)){
            NumSymbol numSymbol = ErrorCheckTool.transFuncFParam2Symbol(this);
            SymbolTableStack.addSymbolToPeek(numSymbol);
        }
    }

    /**
     * 解析参数，并以ValueType的形式传入function，以记录参数
     */
    @Override
    public void buildIr() {
        // int类型
        ValueType type = new IntType(32);
        // int a[] 或者 int a[][?], 那么还要将这个int包装成为指针
        if(!lbrackTokens.isEmpty()){
            // 如果只是一维，那么这里直接指向type即可
            // 如果还有第二维，那么要迭代包装type
            if(!constExpNodes.isEmpty()){
                for(ConstExpNode constExpNode : constExpNodes){
                    constExpNode.buildIr();
                    // 透过综合属性传递上来的应该是这一维的长度，例如int a[][2]则此处传递上来的是2
                    // 迭代更新Type
                    type = new ArrayType(type, Irc.synInt);
                }
            }
            // 最终得到的应该是指针
            // 需注意，得到的并非“指向数组整体”的指针，而是“指向数组下一级元素”的指针，这是为了store方便考虑
            type = new PointerType(type);
        }
        // 将解析完成的参数类型传给curFunction，在curFunction内部构建参数的value
        Irc.curFunction.addArgByValueType(type, Irc.inInt);
    }
}
