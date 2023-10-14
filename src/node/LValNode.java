package node;

import symbol.*;
import token.Token;
import token.TokenType;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description 左值表达式 LVal → Ident {'[' Exp ']'}
 * @Author
 * @Date 2023/9/20
 **/
public class LValNode extends Node{
    public Token getIdentToken() {
        return identToken;
    }

    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> rbrackTokens;

    public LValNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ExpNode> expNodes, ArrayList<Token> rbrackTokens) {
        super(NodeType.LVal);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.expNodes = expNodes;
        this.rbrackTokens = rbrackTokens;
    }

    @Override
    public void print(){
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            for(int i=0; i<lbrackTokens.size(); i++){
                lbrackTokens.get(i).print();
                expNodes.get(i).print();
                rbrackTokens.get(i).print();
            }
        }
        printNodeType();
    }

    // 左值表达式   LVal → Ident {'[' Exp ']'} // c k
    @Override
    public void check() {
        // 检查未定义的名称
        ErrorCheckTool.judgeAndHandleUndefinedError(identToken);
        for(ExpNode expNode : expNodes){
            expNode.check();
        }
    }

    public int getDim(){
        // 先检查名称是否存在
        Symbol symbol = SymbolTableStack.getSymbol(identToken.str, null);
        // 是函数类型或不存在 不予报错
        if( symbol == null || (symbol instanceof FuncSymbol)){
            return -2;
        }
        // 非函数类型且存在 那么一定可以计算返回值维数
        NumSymbol symbol1 = (NumSymbol) symbol;
        int dim = expNodes != null ? symbol1.getDim() - expNodes.size() : symbol1.getDim();
        return Math.max(dim, -1);
    }
}
