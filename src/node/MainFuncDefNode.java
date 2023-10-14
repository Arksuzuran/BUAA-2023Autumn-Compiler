package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import symbol.*;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class MainFuncDefNode extends Node{
    private Token intToken;
    private Token mainToken;
    private Token lparentToken;
    private Token rparentToken;
    private BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token lparentToken, Token rparentToken, BlockNode blockNode) {
        super(NodeType.MainFuncDef);
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lparentToken = lparentToken;
        this.rparentToken = rparentToken;
        this.blockNode = blockNode;
    }

    @Override
    public void print() {
        intToken.print();
        mainToken.print();
        lparentToken.print();
        rparentToken.print();
        blockNode.print();
        printNodeType();
    }

    // 主函数定义   MainFuncDef → 'int' 'main' '(' ')' Block // g j
    @Override
    public void check() {
        // 检测重复定义
        // 无重定义 加入符号表
        if(ErrorCheckTool.judgeAndHandleDuplicateError(mainToken)) {
            ArrayList<NumSymbol> params = new ArrayList<>();
            FuncSymbol funcSymbol = new FuncSymbol(mainToken.str, mainToken.lineNum, this, FuncSymbol.FuncReturnType.INT, params);
            SymbolTableStack.addSymbolToPeek(funcSymbol);
        }
        // 进入函数前
        // 检测错误g：有返回值但结尾没有return语句
        ErrorCheckTool.handleNoReturnEndError(blockNode);
        // 入栈新的符号表
        SymbolTableStack.push(this);

        // 进入函数
        blockNode.check();

        // 离开函数
        // 弹栈
        SymbolTableStack.pop();
    }
}
