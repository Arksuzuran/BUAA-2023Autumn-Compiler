package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import symbol.FuncSymbol;
import symbol.NumSymbol;
import symbol.SymbolTableStack;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class FuncDefNode extends Node{
    private FuncTypeNode funcTypeNode;
    private Token identToken;
    private Token lparentToken;
    private FuncFParamsNode funcFParamsNode;
    private Token rparentToken;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token identToken, Token lparentToken, FuncFParamsNode funcFParamsNode, Token rparentToken, BlockNode blockNode) {
        super(NodeType.FuncDef);
        this.funcTypeNode = funcTypeNode;
        this.identToken = identToken;
        this.lparentToken = lparentToken;
        this.funcFParamsNode = funcFParamsNode;
        this.rparentToken = rparentToken;
        this.blockNode = blockNode;
    }

    @Override
    public void print() {
        funcTypeNode.print();
        identToken.print();
        lparentToken.print();
        if(funcFParamsNode != null){
            funcFParamsNode.print();
        }
        rparentToken.print();
        blockNode.print();
        printNodeType();
    }

    // 函数定义    FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g j
    @Override
    public void check() {
        // 一、检测重名，构建函数符号
        // 返回类型
        FuncSymbol.FuncReturnType returnType = funcTypeNode.hasReturnVal() ? FuncSymbol.FuncReturnType.INT : FuncSymbol.FuncReturnType.VOID;

        // 检查重名
        // 将当前函数加入符号表
        if(ErrorCheckTool.judgeAndHandleDuplicateError(identToken)){
            // 提取所有参数并创建成symbol
            ArrayList<NumSymbol> params = new ArrayList<>();
            if(funcFParamsNode != null){
                for(FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()){
                    params.add(ErrorCheckTool.transFuncFParam2Symbol(funcFParamNode));
                }
            }
            // 构建该函数的符号
            FuncSymbol funcSymbol = new FuncSymbol(identToken.str, identToken.lineNum, this, returnType, params);
            SymbolTableStack.addSymbolToPeek(funcSymbol);
        }

        // 二、即将进入函数
        // 1.如果当前进入了一个有返回值的函数内部
        if(returnType == FuncSymbol.FuncReturnType.INT){
            // 检测错误g：有返回值但结尾没有return语句
            ErrorCheckTool.handleNoReturnEndError(blockNode);
        } else {
            // 标记进入了无返回值的函数
            SymbolTableStack.setInVoidFunc(true);
        }
        // 2.入栈新的符号表
        SymbolTableStack.push(this);

        // 三、拜访函数
        // 对函数参数进行拜访 这些操作都在新的符号表上进行
        if(funcFParamsNode != null){
            funcFParamsNode.check();
        }
        // 对函数主体进行拜访
        blockNode.check();

        // 四、离开函数 :
        // 1.标记退出了无返回值的函数
        SymbolTableStack.setInVoidFunc(false);
        // 2.当前函数生成的符号表要弹栈
        SymbolTableStack.pop();
    }

}
