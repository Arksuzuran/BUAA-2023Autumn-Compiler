package node;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.Irc;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import symbol.SymbolTableStack;
import token.Token;
import error.ErrorCheckTool;
import utils.IrTool;

import java.util.ArrayList;

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
        BLOCK,
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
            case BLOCK -> {
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
                if(!nodes.isEmpty()){
                    ErrorCheckTool.handleVoidFuncReturnInt(tokens.get(0));
                }
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
        if(type == StmtType.BLOCK){ SymbolTableStack.push(this); }

        for(Node node : nodes){
            node.check();
        }

        // 如果是要退出Block 那么应当出栈符号表
        if(type == StmtType.BLOCK){ SymbolTableStack.pop();}
        // 如果是要退出循环 那么应当表明
        if(type == StmtType.FOR){ SymbolTableStack.enterLoop(false); }

    }

    // 语句  Stmt → LVal '=' Exp ';'
    //    | [Exp] ';'   xx
    //    | Block       xx
    //    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //    | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    //    | 'break' ';' | 'continue' ';'
    //    | 'return' [Exp] ';'
    //    | LVal '=' 'getint''('')'';'
    //    | 'printf''('FormatString{,Exp}')'';'
    @Override
    public void buildIr() {
        switch (type){
            // LVal '=' Exp ';'
            case LVALASSIGN -> buildLValAssignIr();
            // [Exp] ';'
            case EXP -> buildExpIr();
            // Block
            case BLOCK -> buildBlockIr();
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            case IF -> buildIfIr();
            // 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            case FOR -> buildForIr();
            // 'break' ';'
            case BREAK -> buildBreakIr();
            // 'continue' ';'
            case CONTINUE -> buildContinueIr();
            // 'return' [Exp] ';'
            case RETURN -> buildReturnIr();
            // LVal '=' 'getint''('')'';'
            case LVALGETINT -> buildLValGetintIr();
            // 'printf''('FormatString{,Exp}')'';'
            case PRINTF -> buildPrintfIr();
        }
    }
    // 语句  Stmt → LVal '=' Exp ';'
    //    | [Exp] ';'   xx
    //    | Block       xx
    //    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    //    | 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    //    | 'break' ';' | 'continue' ';'
    //    | 'return' [Exp] ';'
    //    | LVal '=' 'getint''('')'';'
    //    | 'printf''('FormatString{,Exp}')'';'

    /**
     * LVal '=' Exp ';'
     */
    // 在非buildingConstExp下，LVal返回变量所在的地址，因此直接store即可
    private void buildLValAssignIr(){
        nodes.get(0).buildIr();
        Value lVal = Irc.synValue;
        nodes.get(1).buildIr();
        Value exp = Irc.synValue;
        // 向左值所处地址内存储Exp的内容
        IrBuilder.buildStoreInstruction(exp, lVal, Irc.curBlock);
    }

    /**
     * [Exp] ';'
     */
    private void buildExpIr(){
        if(!nodes.isEmpty()){
            nodes.get(0).buildIr();
        }
    }

    /**
     * Block
     */
    private void buildBlockIr(){
        // 在此创建新块的符号表
        IrSymbolTableStack.push();
        nodes.get(0).buildIr();
        IrSymbolTableStack.pop();
    }

    /**
     * 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     */
    private void buildIfIr(){
        // 为解析Cond准备基本块
        BasicBlock trueBranch = IrBuilder.buildBasicBlock(Irc.curFunction);
        BasicBlock nextBlock = IrBuilder.buildBasicBlock(Irc.curFunction);  // 结束if后跳转到的新块
        BasicBlock falseBranch = nextBlock;

        // 如果有else，则nextBlock与falseBranch不同，另需新建一个falseBranch
        boolean hasElse = nodes.size() > 2;
        if(hasElse){
            falseBranch = IrBuilder.buildBasicBlock(Irc.curFunction);
        }

        CondNode condNode = (CondNode) nodes.get(0);
        condNode.setTrueBranch(trueBranch);
        condNode.setFalseBranch(falseBranch);
        // 解析Cond
        condNode.buildIr();

        // 在trueBranch里解析Stmt
        // 最后，应该跳转到nextBlock
        Irc.curBlock = trueBranch;
        nodes.get(1).buildIr();
        IrBuilder.buildBrInstruction(nextBlock, Irc.curBlock);

        // 如果有else，则在falseBranch里解析Stmt2
        // 最后，应该跳转到nextBlock
        if(hasElse){
            Irc.curBlock = falseBranch;
            nodes.get(2).buildIr();
            IrBuilder.buildBrInstruction(nextBlock, Irc.curBlock);
        }

        // 回到nextBlock，完成解析
        Irc.curBlock = nextBlock;
    }

    /**
     * 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     */
    private void buildForIr(){
        // 需要构建四个块：cond，loop，loopEnd, end
        BasicBlock condBlock = IrBuilder.buildBasicBlock(Irc.curFunction);  // 条件判断
        BasicBlock loopBlock = IrBuilder.buildBasicBlock(Irc.curFunction);  // 循环主体
        BasicBlock loopEndBlock = IrBuilder.buildBasicBlock(Irc.curFunction);   // 循环主题结束的[ForStmt]
        BasicBlock endBlock = IrBuilder.buildBasicBlock(Irc.curFunction);   // 下一个基本块
        int n = 0;

        // 1.headBlock
        // [ForStmt]
        if(posFlag.get(0)){
            nodes.get(n++).buildIr();
        }
        IrBuilder.buildBrInstruction(condBlock, Irc.curBlock);  // 进入CondBlock

        // 2.CondBlock
        Irc.curBlock = condBlock;
        // 存在Cond
        if(posFlag.get(1)){
            CondNode condNode = (CondNode) nodes.get(n++);
            condNode.setTrueBranch(loopBlock);
            condNode.setFalseBranch(endBlock);
            condNode.buildIr();
        }
        // 不存在Cond，直接跳转到loopBlock
        else{
            IrBuilder.buildBrInstruction(loopBlock, Irc.curBlock);
        }

        // 3.loopEndBlock
        Irc.curBlock = loopEndBlock;
        // 存在ForStmt2
        if(posFlag.get(2)){
            nodes.get(n++).buildIr();
        }
        // 解析完成后跳转到condBlock
        IrBuilder.buildBrInstruction(condBlock, Irc.curBlock);

        // 4.loopBlock
        Irc.curBlock = loopBlock;
        // 需要设置好loopEndBlock栈和endBlock栈
        Irc.loopEndBlockStack.push(loopEndBlock);
        Irc.endBlockStack.push(endBlock);
        // 解析Stmt
        nodes.get(n).buildIr();
        // 完成当前解析，弹栈
        Irc.loopEndBlockStack.pop();
        Irc.endBlockStack.pop();
        // loopBlock结束后跳转到自增块
        IrBuilder.buildBrInstruction(loopEndBlock, Irc.curBlock);

        // 5.endBlock
        Irc.curBlock = endBlock;
    }

    /**
     * 'break' ';'
     */
    private void buildBreakIr(){
        // 强制跳转至endBlock
        IrBuilder.buildBrInstruction(Irc.endBlockStack.peek(), Irc.curBlock);
        // break后的代码应该是失效的，因此新建一个块, 将其附着在新的Function上面，但该function不加入module，以达到丢弃这些代码的效果
        Irc.curBlock = new BasicBlock("dead_block_break", new Function("dead_function_break", new VoidType(), new ArrayList<>(), false));
    }

    /**
     * 'continue' ';'
     */
    private void buildContinueIr(){
        // 强制跳转至endLoopBlock
        IrBuilder.buildBrInstruction(Irc.loopEndBlockStack.peek(), Irc.curBlock);
        // continue后的代码应该是失效的，因此新建一个块, 将其附着在新的Function上面，但该function不加入module，以达到丢弃这些代码的效果
        Irc.curBlock = new BasicBlock("dead_block_break", new Function("dead_function_break", new VoidType(), new ArrayList<>(), false));
    }

    /**
     * 'return' [Exp] ';'
     */
    private void buildReturnIr(){
        Value returnExp = null;
        if(!nodes.isEmpty()){
            nodes.get(0).buildIr();
            returnExp = Irc.synValue;
        }
        IrBuilder.buildRetInstruction(Irc.curBlock, returnExp);
    }

    /**
     * LVal '=' 'getint''('')'';'
     */
    private void buildLValGetintIr(){
        // 解析LVal
        nodes.get(0).buildIr();
        Value lVal = Irc.synValue;
        // 调用getint
        Value result = IrBuilder.buildCallInstruction(Function.getint, new ArrayList<>(), Irc.curBlock);
        // 将读到的值存入lVal
        IrBuilder.buildStoreInstruction(result, lVal, Irc.curBlock);
    }

    /**
     * 'printf''('FormatString{,Exp}')'';'
     */
    // putstr形式
    private void buildPrintfIr(){
        ArrayList<Value> params = new ArrayList<>();
        // 先解析Exp们
        for(Node expNode : nodes){
            expNode.buildIr();
            params.add(Irc.synValue);
        }
        assert getFormatStringToken() != null;
        String s = getFormatStringToken().str;
        ArrayList<String> spiltStrings = IrTool.spiltFormatString(s);

        int expIndex = 0;
        for(String spiltString : spiltStrings){
            // %d, 改为输出对应的exp
            if(spiltString.equals("%d")){
                Value param = params.get(expIndex);
                IrBuilder.buildCallInstruction(Function.putint, new ArrayList<>(){{
                    add(param);
                }}, Irc.curBlock);
                expIndex++;
            }
            // 常量字符串，改为构造全局变量，并输出之
            else {
                // 全局变量[n x i8]*
                Value stringGlobalVariable = IrBuilder.buildGlobalConstString(spiltString);
                // 作为函数参数应当是i8*, 进行降维
                Value param = IrBuilder.buildRankDownInstruction(stringGlobalVariable, Irc.curBlock);
                IrBuilder.buildCallInstruction(Function.putstr, new ArrayList<>(){{
                    add(param);
                }}, Irc.curBlock);
            }
        }
    }
    // putch形式
//    private void buildPrintfIr(){
//        ArrayList<Value> params = new ArrayList<>();
//        // 先解析Exp们
//        for(Node expNode : nodes){
//            expNode.buildIr();
//            params.add(Irc.synValue);
//        }
//        assert getFormatStringToken() != null;
//        String s = getFormatStringToken().str;
//        // 遍历formatString
//        int len = s.length() - 1;
//        int paramIndex = 0;
//        for (int i = 1; i < len; i++) {
//            // \n 替换为 10
//            if (i + 1 < len && s.charAt(i) == '\\' && s.charAt(i+1) == 'n') {
//                IrBuilder.buildCallInstruction(Function.putch, new ArrayList<>(){{
//                    add(new ConstInt(32, 10));
//                }}, Irc.curBlock);
//                i++;
//            }
//            // %d 替换为需要输出的exp
//            else if (i + 1 < len && s.charAt(i) == '%' && s.charAt(i+1) == 'd') {
//                Value exp = params.get(paramIndex++);
//                IrBuilder.buildCallInstruction(Function.putint, new ArrayList<>(){{
//                    add(exp);
//                }}, Irc.curBlock);
//                i++;
//            }
//            // 普通字符
//            else {
//                int c = s.charAt(i);
//                IrBuilder.buildCallInstruction(Function.putch, new ArrayList<>(){{
//                    add(new ConstInt(32, c));
//                }}, Irc.curBlock);
//            }
//        }
//    }

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
        // 可以不必检查开头结尾的双引号
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
