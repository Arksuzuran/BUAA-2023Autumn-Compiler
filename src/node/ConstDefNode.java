package node;

import symbol.NumSymbol;
import symbol.SymbolTableStack;
import symbol.SymbolType;
import token.Token;
import utils.ErrorCheckTool;

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
}
