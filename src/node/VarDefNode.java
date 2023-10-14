package node;

import symbol.NumSymbol;
import symbol.SymbolTableStack;
import symbol.SymbolType;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description VarDef → Ident { '[' ConstExp ']' }
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
}
