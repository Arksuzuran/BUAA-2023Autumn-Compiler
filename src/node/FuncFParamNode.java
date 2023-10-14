package node;

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
}
