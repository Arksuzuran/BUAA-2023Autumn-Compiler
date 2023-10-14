package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import symbol.*;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 * @Author
 * @Date 2023/9/20
 **/
public class UnaryExpNode extends Node{
    private PrimaryExpNode primaryExpNode;
    private Token identToken;
    private Token lparentToken;
    private FuncRParamsNode funcRParamsNode;
    private Token rparentToken;
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;

    public UnaryExpNode(PrimaryExpNode primaryExpNode, Token identToken, Token lparentToken, FuncRParamsNode funcRParamsNode, Token rparentToken, UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        super(NodeType.UnaryExp);
        this.primaryExpNode = primaryExpNode;
        this.identToken = identToken;
        this.lparentToken = lparentToken;
        this.funcRParamsNode = funcRParamsNode;
        this.rparentToken = rparentToken;
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    @Override
    public void print(){
        if(primaryExpNode != null){
            primaryExpNode.print();
        } else if(identToken != null) {
            identToken.print();
            lparentToken.print();
            if(funcRParamsNode != null){
                funcRParamsNode.print();
            }
            rparentToken.print();
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        printNodeType();
    }

    // 一元表达式   UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // c d e j
    //        | UnaryOp UnaryExp
    @Override
    public void check() {
        // PrimaryExp
        if(primaryExpNode != null){
            primaryExpNode.check();
        }
        // Ident '(' [FuncRParams] ')' // c d e j
        else if(identToken != null) {
            // 先检查符号未定义c
            if(ErrorCheckTool.judgeAndHandleUndefinedError(identToken)){
                // 检查其是否为函数符号 如果不是 则报参数个数错误
                FuncSymbol symbol = (FuncSymbol) SymbolTableStack.getSymbol(identToken.str, SymbolType.Function);
                if(symbol == null){
                    ErrorHandler.addError(new Error(ErrorType.d, identToken.lineNum));
                }
                // 检测参数个数是否一致
                else if(symbol.getParams().size() != getParamsNum()){
                    ErrorHandler.addError(new Error(ErrorType.d, identToken.lineNum));
                }
                // 检验参数类型是否一一对应
                else if(!checkParamsSame(symbol)){
                    ErrorHandler.addError(new Error(ErrorType.e, identToken.lineNum));
                }
            }
            // 检测完符号后，再对其参数一一进行检查
            if(funcRParamsNode != null){
                funcRParamsNode.check();
            }
        }
        // UnaryOp UnaryExp
        else {
            unaryExpNode.check();
        }
    }

    private int getParamsNum(){
        if(funcRParamsNode == null){
            return 0;
        }
        return funcRParamsNode.getExpNodes().size();
    }
    // 检验参数类型是否一致
    private boolean checkParamsSame(FuncSymbol symbol){
        ArrayList<NumSymbol> fParams = symbol.getParams();
        ArrayList<ExpNode> expNodes = funcRParamsNode.getExpNodes();
        int fParamDim, rParamDim;
        for(int i = 0; i < symbol.getParams().size(); i++){
            fParamDim = fParams.get(i).getDim();    // 形参维数
            rParamDim = expNodes.get(i).getDim();   // 实参维数
            // 2代表维数不确定（即实参未定义），此处不处理，留到后面报出错误c
            if(rParamDim != 2 && fParamDim != rParamDim){
                return false;
            }
        }
        return true;
    }

    // 检测当前式子的维数
    public int getDim() {
        // PrimaryExp
        if(primaryExpNode != null){
            return primaryExpNode.getDim();
        }
        // Ident '(' [FuncRParams] ')'
        else if(identToken != null) {
            // 此处先不检查参数的问题，仅根据函数的返回类型进行判断
            FuncSymbol symbol = (FuncSymbol) SymbolTableStack.getSymbol(identToken.str, SymbolType.Function);
            // 函数不存在 不予报错 在后续检测时再报错
            if(symbol == null){
                return -2;
            }
            // 无返回值
            else if(symbol.getReturnType() == FuncSymbol.FuncReturnType.VOID){
                return -1;
            }
            // int返回值
            else{
                return 0;
            }
        }
        // UnaryOp UnaryExp
        else {
            return unaryExpNode.getDim();
        }
    }
}
