package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import symbol.SymbolTableStack;
import token.Token;
import utils.ErrorCheckTool;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Description Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
             * | [Exp] ';' //有无Exp两种情况
             * | Block
             * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
             * | 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
             * | 'break' ';' | 'continue' ';'
             * | 'return' [Exp] ';' // 1.有Exp 2.无Exp
             * | LVal '=' 'getint''('')'';'
             * | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
 * @Author  H1KARI
 * @Date 2023/9/20
 **/
public class StmtNode extends Node{
    public enum StmtType{
        Block,
        IF,
        FOR,
        BREAK,
        CONTINUE,
        RETURN,
        PRINTF,
        LVALGETINT, // LVal '=' 'getint''('')'';'
        LVALASSIGN, // LVal '=' Exp ';'
        EXP;        // [Exp] ';'
    }

    // 获取stmt类型
    public StmtType getType() {
        return type;
    }
    // 如果是返回值类型，返回return的值
    public Node getReturnTypeResult(){
        if(type == StmtType.RETURN && !nodes.isEmpty()){
            return nodes.get(0);
        }
        return null;
    }

    // 选项过多，不再采用设出全部变量的形式，而是直接使用列表
    private StmtType type;
    private ArrayList<Token> tokens;
    private ArrayList<Node> nodes;

    private ArrayList<Boolean> posFlag;    // 首个forstmt，第二个Cond, 第三个forstmt
    public StmtNode(StmtType type, ArrayList<Token> tokens, ArrayList<Node> nodes, ArrayList<Boolean> posFlag) {
        super(NodeType.Stmt);
        this.type = type;
        this.tokens = tokens;
        this.nodes = nodes;
        this.posFlag = posFlag;
    }

    @Override
    public void print() {
        int t = 0, n = 0;
        switch (type) {
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            case IF -> {
                // if (
                tokens.get(t++).print();
                tokens.get(t++).print();
                // Cond
                nodes.get(n++).print();
                // )
                tokens.get(t++).print();
                // Stmt
                nodes.get(n++).print();
                // else Stmt
                if(tokens.size() > 3){
                    tokens.get(t).print();
                    nodes.get(n).print();
                }
            }
            // 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
            case FOR -> {
                // 'for' '('
                tokens.get(t++).print();
                tokens.get(t++).print();
                // [ForStmt]
                if(posFlag.get(0)){
                    nodes.get(n++).print();
                }
                // ;
                tokens.get(t++).print();
                // [Cond]
                if(posFlag.get(1)){
                    nodes.get(n++).print();
                }
                // ;
                tokens.get(t++).print();
                // [ForStmt]
                if(posFlag.get(2)){
                    nodes.get(n++).print();
                }
                // )
                tokens.get(t).print();
                // Stmt
                nodes.get(n).print();
            }
            // 'break' ';' | 'continue' ';'
            case BREAK, CONTINUE -> {
                tokens.get(t++).print();
                tokens.get(t).print();
            }
            // 'return' [Exp] ';'
            case RETURN -> {
                tokens.get(t++).print();
                if(!nodes.isEmpty()){
                    nodes.get(n).print();
                }
                tokens.get(t).print();
            }
            // 'printf''('FormatString{','Exp}')'';'
            case PRINTF -> {
                // printf
                tokens.get(t++).print();
                // (
                tokens.get(t++).print();
                // FormatString
                tokens.get(t++).print();
                // {','Exp}
                for(; n<nodes.size();){
                    tokens.get(t++).print();
                    nodes.get(n++).print();
                }
                tokens.get(t++).print();
                tokens.get(t).print();
            }
            // LVal '=' Exp ';'
            case LVALASSIGN -> {
                nodes.get(n++).print();
                tokens.get(t++).print();
                nodes.get(n).print();
                tokens.get(t).print();
            }
            //  [Exp] ';'
            case EXP -> {
                if(!nodes.isEmpty()){
                    nodes.get(n).print();
                }
                tokens.get(t).print();
            }
            // Block
            case Block -> {
                nodes.get(n).print();
            }
            // LVal '=' 'getint''('')'';'
            case LVALGETINT -> {
                nodes.get(n).print();
                // =
                tokens.get(t++).print();
                // getint
                tokens.get(t++).print();
                // (
                tokens.get(t++).print();
                // )
                tokens.get(t++).print();
                // ;
                tokens.get(t).print();
            }
        }
        printNodeType();
    }
    // 语句  Stmt → LVal '=' Exp ';' xx
    //    | [Exp] ';' xx
    //    | Block // h i xx
    //    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j xx
    //    | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt xx
    //    | 'break' ';' | 'continue' ';' // i m xx
    //    | 'return' [Exp] ';' // f i
    //    | LVal '=' 'getint''('')'';' // h i j xx
    //    | 'printf''('FormatString{,Exp}')'';' // i j l
    @Override
    public void check() {
        // 进行本层才能进行的特殊检验
        switch (type) {
            // LVal '=' Exp ';' // h i
            case LVALASSIGN, LVALGETINT -> {
                // 检查错误h：给常量赋值
                ErrorCheckTool.handleConstAssignError(getLValNode().getIdentToken());
            }
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] 没有特别地注意点
            // 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' 没有特别地注意点
            // 'break' ';' | 'continue' ';' // i m
            case BREAK, CONTINUE -> {
                // 检查错误m: 在非循环块中使用break和continue语句
                ErrorCheckTool.handleBreakContinueOutOfLoop(tokens.get(0));
            }
            // 'return' [Exp] ';' // f i
            case RETURN -> {
                // 检查错误f 无返回值的函数存在带有Exp的return语句
                ErrorCheckTool.handleVoidFuncReturnInt(tokens.get(0));
            }
            // 'printf''('FormatString{,Exp}')'';'
            case PRINTF -> {
                Token formatStringToken = getFormatStringToken();
                // 检查字符串本身是否合法
                if(!checkFormatString(formatStringToken.str)){
                    ErrorHandler.addError(new Error(ErrorType.a, formatStringToken.lineNum));
                }
                // 检查%d和实际参数的个数是否对应
                if(getFormatStringDNum(formatStringToken.str) != nodes.size()){
                    ErrorHandler.addError(new Error(ErrorType.l, formatStringToken.lineNum));
                }
            }
        }

        // 如果是要进入循环 那么应当表明
        if(type == StmtType.FOR){ SymbolTableStack.enterLoop(true); }
        // 如果是要进入Block 那么应当入栈新符号表
        if(type == StmtType.Block){ SymbolTableStack.push(this);}

        for(Node node : nodes){
            node.check();
        }

        // 如果是要退出Block 那么应当出栈符号表
        if(type == StmtType.Block){ SymbolTableStack.pop();}
        // 如果是要退出循环 那么应当表明
        if(type == StmtType.FOR){ SymbolTableStack.enterLoop(false); }

    }
    private LValNode getLValNode(){
        if(type == StmtType.LVALASSIGN || type == StmtType.LVALGETINT){
            return (LValNode) nodes.get(0);
        }
        return null;
    }
    private Token getFormatStringToken(){
        if(type == StmtType.PRINTF){
            return tokens.get(2);
        }
        return null;
    }
    // 获取str字符串中%d的数量
    private int getFormatStringDNum(String str){
        int result = 0;
        for(int i = 1; i<str.length() - 1; i++){
            if(str.charAt(i) == '%' && str.charAt(i+1) == 'd'){
                result++;
            }
        }
        return result;
    }
    // 检查字符串是否合法
    public boolean checkFormatString(String str) {
        for (int i = 1; i < str.length() - 1; i++) {
            char chr = str.charAt(i);
            if (chr == '%') {
                return str.charAt(i + 1) == 'd';
            } else if (chr == '\\') {
                return str.charAt(i + 1) == 'n';
            } else if (!(chr == 32 || chr == 33 || (chr >= 40 && chr <= 126))) {
                return false;
            }
        }
        return true;
    }
}
