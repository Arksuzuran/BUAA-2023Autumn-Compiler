package utils;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import node.BlockItemNode;
import node.BlockNode;
import node.FuncFParamNode;
import node.StmtNode;
import symbol.NumSymbol;
import symbol.SymbolTableStack;
import symbol.SymbolType;
import token.Token;

import java.util.ArrayList;

/**
 * @Description 错误处理和符号表检查的工具方法
 * @Author
 * @Date 2023/10/14
 **/
public class ErrorCheckTool {
    // 将FuncFParam转换为NumSymbol
    public static NumSymbol transFuncFParam2Symbol(FuncFParamNode funcFParamNode){
        Token identToken = funcFParamNode.getIdentToken();
        int dim = funcFParamNode.getParamDim();
        NumSymbol numSymbol = new NumSymbol(identToken.str, SymbolType.Var, identToken.lineNum, funcFParamNode, dim);
        return numSymbol;
    }


    // 检测并处理重定义问题 返回其是否未出现重定义问题
    // false: 出现问题
    // true: 未出现问题
    public static boolean judgeAndHandleDuplicateError(Token token){
        // 在栈顶的符号表中检测是否存在重复定义
        if(SymbolTableStack.peekHasSymbol(token.str)){
            ErrorHandler.addError(new Error(ErrorType.b, token.lineNum));
            return false;
        }
        return true;
    }
    // 检测并处理无定义问题 返回其是否未出现无定义问题
    // false: 出现问题
    // true: 未出现问题
    public static boolean judgeAndHandleUndefinedError(Token token){
//        System.out.println("尝试检查无定义问题" + token.str);
        // 在栈顶的符号表中检测是否存在定义
        if(!SymbolTableStack.stackHasSymbol(token.str)){
            ErrorHandler.addError(new Error(ErrorType.c, token.lineNum));
            return false;
        }
        return true;
    }

    // 查看一个block是否以return语句结尾
    public static boolean hasReturnEnd(BlockNode blockNode){
        ArrayList<BlockItemNode> blockItemNodes = blockNode.getBlockItemNodes();
        if(blockItemNodes.size() >= 1){
            // 取得最后一个item
            BlockItemNode lastItem = blockItemNodes.get(blockItemNodes.size() - 1);
            StmtNode lastStmtNode = lastItem.getStmtNode();
            // 该item必须是stmt且为return类型才可以
            if( lastStmtNode != null){
                return lastStmtNode.getType() == StmtNode.StmtType.RETURN;
            }
        }
        return false;
    }
    // 处理块不以return结尾的错误
    public static void handleNoReturnEndError(BlockNode blockNode){
        if(!hasReturnEnd(blockNode)){
            int lineNum = blockNode.getRbraceToken().lineNum;
            ErrorHandler.addError(new Error(ErrorType.g, lineNum));
        }
    }
    // 处理常量赋值的错误
    public static void handleConstAssignError(Token token){
        if(SymbolTableStack.stackHasSymbol(token.str, SymbolType.Const)){
            ErrorHandler.addError(new Error(ErrorType.h, token.lineNum));
        }
    }
    // 处理在非循环块中使用break和continue语句的错误
    public static void handleBreakContinueOutOfLoop(Token token){
        if(!SymbolTableStack.inLoop()){
            ErrorHandler.addError(new Error(ErrorType.m, token.lineNum));
        }
    }
    // 处理无返回值的函数存在不匹配的return语句的错误
    public static void handleVoidFuncReturnInt(Token token){
        if(SymbolTableStack.inVoidFunc()){
            ErrorHandler.addError(new Error(ErrorType.f, token.lineNum));
        }
    }
}
