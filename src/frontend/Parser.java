package frontend;

import error.Error;
import node.*;
import error.*;
import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @Description 语法分析器 递归下降子程序实现
 * @Author H1KARI
 * @Date 2023/9/18
 **/
public class Parser {
    int flag = 0;
    // 词法分析器的token列表
    private final ArrayList<Token> tokens;

    // 当前扫描到的位置
    private int pos;
    // 最末位置
    private int maxPos;
    // 当前token
    private Token curToken;
    private CompUnitNode parsingResultNode = null;
    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.maxPos = tokens.size() - 1;
        this.curToken = tokens.get(pos);
    }

    // 进行语法分析
    public void doParsing(){
        if(tokens!=null && !tokens.isEmpty()){
            parsingResultNode = CompUnit();
        }
    }
    // 返回语法分析结果
    public CompUnitNode getParsingResultNode(){
        return parsingResultNode;
    }
    // 输出语法分析结果至文件
    public void outputParsingResult(){
        parsingResultNode.print();
    }


    // matchToken所用到的，缺失token和错误类型的对应关系
    private final HashMap<TokenType, ErrorType> tokenErrorMap = new HashMap<>(){{
        put(TokenType.SEMICN, ErrorType.i);
        put(TokenType.RPARENT, ErrorType.j);
        put(TokenType.RBRACK, ErrorType.k);
    }};
    // 所有匹配最终都会归结到对终结符的匹配上，而取下一个终结符的时机就是上一个终结符被读取时，因此将这两个操作合二为一：
    // 将当前token与指定类型的token相匹配，匹配成功则推进匹配进度pos++，否则进行报错
    // 对于规定的若干错误类型，不予报错，而是记录并进行补全
    private Token matchToken(TokenType tokenType) {
        if (curToken.type == tokenType) {
            Token tmp = curToken;
//            System.out.println("匹配成功:" + tmp);
            if (pos < maxPos) {
                pos++;
                curToken = tokens.get(pos);
            }
            return tmp;
        }
        // 匹配失败 尝试匹配错误类型 此处curToken不必再向下滑动
        else if(tokenType == TokenType.SEMICN || tokenType == TokenType.RPARENT || tokenType == TokenType.RBRACK){
            // 此处应该返回前一个非终结符的位置 这里前一个非终结符一定不为空
            int lineNum = tokens.get(pos-1).lineNum;
            String str = tokenType.getStr();
            ErrorType errorType = tokenErrorMap.get(tokenType);

            ErrorHandler.addError(new Error(errorType, lineNum));
            return new Token(str, lineNum, tokenType);
        }
        return null;
    }

    // 向前预先读取shift个token并进行匹配，但不改变pos的值
    private boolean preMatchToken(int shift, TokenType tokenType) {
        if (pos + shift <= maxPos) {
            Token token = shift == 0 ? curToken : tokens.get(pos + shift);
            return token.type == tokenType;
        }
        return false;
    }

    int savedPos = 0;

    // 保存当前pos位置，用于回溯
    private void savePos() {
        savedPos = pos;
    }

    // 回溯至上一次标记点
    private void recoverPos() {
        pos = savedPos;
        curToken = tokens.get(pos);
    }
    //
    // 递归下降子程序
    //

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    // MainFuncDef  int main()
    // FuncDef      int/void f()
    // Decl         [const] int var =
    private CompUnitNode CompUnit() {
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;
        // Decl
        while (!preMatchToken(2, TokenType.LPARENT)) {
            declNodes.add(Decl());
        }
        // FuncDef
        while (!preMatchToken(1, TokenType.MAINTK)) {
            funcDefNodes.add(FuncDef());
        }
        // MainFuncDef
        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    //  Decl → ConstDecl | VarDecl
    //  在First集合中，ConstDecl 带有 'const'
    private DeclNode Decl() {
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if (curToken.type == TokenType.CONSTTK) {
            constDeclNode = ConstDecl();
        } else {
            varDeclNode = VarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDefNode FuncDef() {
        FuncTypeNode funcTypeNode = FuncType();
        Token identToken = matchToken(TokenType.IDENFR);
        Token lparentToken = matchToken(TokenType.LPARENT);
        FuncFParamsNode funcFParamsNode = null;
        if (curToken.type == TokenType.INTTK) {
            funcFParamsNode = FuncFParams();
        }
        Token rparentToken = matchToken(TokenType.RPARENT);
//        System.out.println("即将从FuncDef进入block" + curToken);
        BlockNode blockNode = Block();
//        System.out.println("离开block" + curToken);
        return new FuncDefNode(funcTypeNode, identToken, lparentToken, funcFParamsNode, rparentToken, blockNode);
    }

    private MainFuncDefNode MainFuncDef() {
        Token intToken = matchToken(TokenType.INTTK);
        Token mainToken = matchToken(TokenType.MAINTK);
        Token lparentToken = matchToken(TokenType.LPARENT);
        Token rparentToken = matchToken(TokenType.RPARENT);
        BlockNode blockNode = Block();
        return new MainFuncDefNode(intToken, mainToken, lparentToken, rparentToken, blockNode);
    }

    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDeclNode ConstDecl() {
        Token constToken = matchToken(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        ArrayList<Token> commaTokens = new ArrayList<>();
        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();

        constDefNodes.add(ConstDef());
        while (curToken.type == TokenType.COMMA) {
            commaTokens.add(matchToken(TokenType.COMMA));
            constDefNodes.add(ConstDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, commaTokens, constDefNodes, semicnToken);
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    private VarDeclNode VarDecl() {
        BTypeNode bTypeNode = BType();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        varDefNodes.add(VarDef());
        while (curToken.type == TokenType.COMMA) {
            commaTokens.add(matchToken(TokenType.COMMA));
            varDefNodes.add(VarDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode, varDefNodes, commaTokens, semicnToken);
    }

    // BType → 'int'
    private BTypeNode BType() {
        Token intToken = matchToken(TokenType.INTTK);
        return new BTypeNode(intToken);
    }

    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private ConstDefNode ConstDef() {
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        while (curToken.type == TokenType.LBRACK) {
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rbrackTokens.add(matchToken(TokenType.RBRACK));
        }
        Token assignToken = matchToken(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = ConstInitVal();
        return new ConstDefNode(identToken, lbrackTokens, constExpNodes, rbrackTokens, assignToken, constInitValNode);
    }

    private ConstExpNode ConstExp() {
        AddExpNode addExpNode = AddExp();
        return new ConstExpNode(addExpNode);
    }

    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 后者的first是 LBRACE, 以此进行区分
    private ConstInitValNode ConstInitVal() {
        ConstExpNode constExpNode = null;
        Token lbraceToken = null;
        ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        Token rbraceToken = null;
        // 数组的初始化, 以{开头
        if (curToken.type == TokenType.LBRACE) {
            lbraceToken = matchToken(TokenType.LBRACE);
            // 下一个token不是右括号, 那么中间应当含有constinitval
            if (curToken.type != TokenType.RBRACE) {
                constInitValNodes.add(ConstInitVal());
                while (curToken.type == TokenType.COMMA) {
                    commaTokens.add(matchToken(TokenType.COMMA));
                    constInitValNodes.add(ConstInitVal());
                }
            }
            // 无论中间是否有数组初始值，都应该匹配右括号
            rbraceToken = matchToken(TokenType.RBRACE);
        }
        // 非数组初始化，仅有一个constexp
        else {
            constExpNode = ConstExp();
        }
        return new ConstInitValNode(constExpNode, lbraceToken, constInitValNodes, commaTokens, rbraceToken);
    }

    // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private VarDefNode VarDef() {
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        Token assignToken = null;
        InitValNode initValNode = null;
        // 数组指定元素初始化
        while (curToken.type == TokenType.LBRACK) {
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rbrackTokens.add(matchToken(TokenType.RBRACK));
        }
        // 根据是否有等号来判断是否执行初始化
        if (curToken.type == TokenType.ASSIGN) {
            assignToken = matchToken(TokenType.ASSIGN);
            initValNode = InitVal();
        }
        return new VarDefNode(identToken, lbrackTokens, constExpNodes, rbrackTokens, assignToken, initValNode);
    }

    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    // 数组初始化带有{
    private InitValNode InitVal() {
        ExpNode expNode = null;
        Token lbraceToken = null;
        ArrayList<InitValNode> initValNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        Token rbraceToken = null;

        if (curToken.type == TokenType.LBRACE) {
            lbraceToken = matchToken(TokenType.LBRACE);
            if (curToken.type != TokenType.RBRACE) {
                initValNodes.add(InitVal());
                while (curToken.type == TokenType.COMMA) {
                    commaTokens.add(matchToken(TokenType.COMMA));
                    initValNodes.add(InitVal());
                }
            }
            rbraceToken = matchToken(TokenType.RBRACE);
        } else {
            expNode = Exp();
        }
        return new InitValNode(expNode, lbraceToken, initValNodes, commaTokens, rbraceToken);
    }

    // 表达式 Exp → AddExp
    private ExpNode Exp() {
//        if(flag < 50){
////            System.out.println("exp:" + curToken);
//            flag++;
//        }
        AddExpNode addExpNode = AddExp();
        return new ExpNode(addExpNode);
    }

    // 函数类型 FuncType → 'void' | 'int'
    private FuncTypeNode FuncType() {
        Token type;
        if (curToken.type == TokenType.VOIDTK) {
            type = matchToken(TokenType.VOIDTK);
        } else {
            type = matchToken(TokenType.INTTK);
        }
        return new FuncTypeNode(type);
    }

    // 函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
    private FuncFParamsNode FuncFParams() {
        ArrayList<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        funcFParamNodes.add(FuncFParam());
        while (curToken.type == TokenType.COMMA) {
            commaTokens.add(matchToken(TokenType.COMMA));
            funcFParamNodes.add(FuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commaTokens);
    }

    // 函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private FuncFParamNode FuncFParam() {
        BTypeNode bTypeNode = BType();
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        if (curToken.type == TokenType.LBRACK) {
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            rbrackTokens.add(matchToken(TokenType.RBRACK));
            while (curToken.type == TokenType.LBRACK) {
                lbrackTokens.add(matchToken(TokenType.LBRACK));
                constExpNodes.add(ConstExp());
                rbrackTokens.add(matchToken(TokenType.RBRACK));
            }
        }
        return new FuncFParamNode(bTypeNode, identToken, lbrackTokens, rbrackTokens, constExpNodes);
    }

    private BlockNode Block() {
//        System.out.println("进入block" + curToken);
        Token lbraceToken = matchToken(TokenType.LBRACE);
        ArrayList<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (curToken.type != TokenType.RBRACE) {
            blockItemNodes.add(BlockItem());
        }
        Token rbraceToken = matchToken(TokenType.RBRACE);
        return new BlockNode(lbraceToken, blockItemNodes, rbraceToken);
    }

    // 语句块项 BlockItem → Decl | Stmt
    private BlockItemNode BlockItem() {
        DeclNode declNode = null;
        StmtNode stmtNode = null;
        // Decl:  ConstDecl → 'const'... 或者 VarDecl → BType...
        if (curToken.type == TokenType.CONSTTK || curToken.type == TokenType.INTTK) {
            declNode = Decl();
        } else {
            stmtNode = Stmt();
        }
        return new BlockItemNode(declNode, stmtNode);
    }

    /* 语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
        | [Exp] ';' //有无Exp两种情况
        | Block
        | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        | 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
        | 'break' ';' | 'continue' ';'
        | 'return' [Exp] ';' // 1.有Exp 2.无Exp
        | LVal '=' 'getint''('')'';'
        | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
     */

    private StmtNode Stmt() {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        StmtNode.StmtType type;
        ArrayList<Boolean> posFlag = new ArrayList<>(Arrays.asList(false, false, false));   // for语句的三个槽位
//        if(flag <= 50){
//            System.out.println("进入stmt" + curToken);
//            flag++;
//        }
        switch (curToken.type) {
            case LBRACE -> { // 语句块 Block → '{' { BlockItem } '}'
                nodes.add(Block());
                type = StmtNode.StmtType.BLOCK;
            }
            case IFTK -> {  //  'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                tokens.add(matchToken(TokenType.IFTK));
                tokens.add(matchToken(TokenType.LPARENT));
                nodes.add(Cond());
                tokens.add(matchToken(TokenType.RPARENT));
                nodes.add(Stmt());
                if (curToken.type == TokenType.ELSETK) {
                    tokens.add(matchToken(TokenType.ELSETK));
                    nodes.add(Stmt());
                }
                type = StmtNode.StmtType.IF;
            }
            case FORTK -> { //  'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
                tokens.add(matchToken(TokenType.FORTK));
                tokens.add(matchToken(TokenType.LPARENT));

                // 第一个ForStmt
                if (curToken.type != TokenType.SEMICN) {
                    nodes.add(ForStmt());
                    posFlag.set(0, true);
                }
                tokens.add(matchToken(TokenType.SEMICN));

                // Cond
                if (curToken.type != TokenType.SEMICN) {
                    nodes.add(Cond());
                    posFlag.set(1, true);
                }
                tokens.add(matchToken(TokenType.SEMICN));

                // 第二个forStmt
                if (curToken.type != TokenType.RPARENT) {
                    nodes.add(ForStmt());
                    posFlag.set(2, true);
                }
                tokens.add(matchToken(TokenType.RPARENT));

                nodes.add(Stmt());
                type = StmtNode.StmtType.FOR;
            }
            case BREAKTK -> {   // 'break' ';'
                tokens.add(matchToken(TokenType.BREAKTK));
                tokens.add(matchToken(TokenType.SEMICN));
                type = StmtNode.StmtType.BREAK;
            }
            case CONTINUETK -> {    // 'continue' ';'
                tokens.add(matchToken(TokenType.CONTINUETK));
                tokens.add(matchToken(TokenType.SEMICN));
                type = StmtNode.StmtType.CONTINUE;
            }
            case RETURNTK -> {  // 'return' [Exp] ';'
                tokens.add(matchToken(TokenType.RETURNTK));
                if(curToken.type != TokenType.SEMICN){
                    nodes.add(Exp());
                }
                tokens.add(matchToken(TokenType.SEMICN));
                type = StmtNode.StmtType.RETURN;
            }
            case PRINTFTK -> {  // 'printf''('FormatString{','Exp}')'';'
                tokens.add(matchToken(TokenType.PRINTFTK));
                tokens.add(matchToken(TokenType.LPARENT));
                tokens.add(matchToken(TokenType.STRCON));
                while (curToken.type == TokenType.COMMA) {
                    tokens.add(matchToken(TokenType.COMMA));
                    nodes.add(Exp());
                }
                tokens.add(matchToken(TokenType.RPARENT));
                tokens.add(matchToken(TokenType.SEMICN));
                type = StmtNode.StmtType.PRINTF;
            }
            default -> {
                // 余下三种情况：
                //  LVal '=' Exp ';'
                //  [Exp] ';'
                //  LVal '=' 'getint''('')'';'
                // 其中：左值表达式 LVal → Ident {'[' Exp ']'}
                //      表达式 Exp → AddExp → MulExp → UnaryExp  → PrimaryExp | Ident   PrimaryExp → '('
                // 直接为分号，则无需进行任何操作
                if (curToken.type == TokenType.SEMICN) {
                    tokens.add(matchToken(TokenType.SEMICN));
                    type = StmtNode.StmtType.EXP;
                }
                else{
                    // 先使用Exp消去LVal和Exp
                    savePos();
//                    System.out.println("stmt开始进行试探性检验" + curToken);
                    ExpNode expNode = Exp();
//                    System.out.println("stmt检验完成" + curToken);
                    // 直接为分号，应该是Exp
                    if (curToken.type == TokenType.SEMICN) {
                        nodes.add(expNode);
                        tokens.add(matchToken(TokenType.SEMICN));
                        type = StmtNode.StmtType.EXP;
                    }
                    // 非分号，那么进行回溯以读取LVal
                    else {
                        recoverPos();
                        LValNode lValNode = LVal();
                        nodes.add(lValNode);
                        tokens.add(matchToken(TokenType.ASSIGN));
                        // LVal '=' 'getint''('')'';'
                        if (curToken.type == TokenType.GETINTTK) {
                            tokens.add(matchToken(TokenType.GETINTTK));
                            tokens.add(matchToken(TokenType.LPARENT));
                            tokens.add(matchToken(TokenType.RPARENT));
                            tokens.add(matchToken(TokenType.SEMICN));
                            type = StmtNode.StmtType.LVALGETINT;
                        }
                        // LVal '=' Exp ';'
                        else {
                            nodes.add(Exp());
                            tokens.add(matchToken(TokenType.SEMICN));
                            type = StmtNode.StmtType.LVALASSIGN;
                        }
                    }
                }
            }
        }
        return new StmtNode(type, tokens, nodes, posFlag);
    }

    // 条件表达式 Cond → LOrExp
    private CondNode Cond() {
        LOrExpNode lOrExpNode = LOrExp();
        return new CondNode(lOrExpNode);
    }

    // 左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    private LValNode LVal() {
//        System.out.println("当前正在分析" + curToken);
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        while (curToken.type == TokenType.LBRACK) {
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            expNodes.add(Exp());
            rbrackTokens.add(matchToken(TokenType.RBRACK));
        }
        return new LValNode(identToken, lbrackTokens, expNodes, rbrackTokens);
    }

    // For循环第三语句 ForStmt → LVal '=' Exp
    private ForStmtNode ForStmt() {
        LValNode lValNode = LVal();
        Token assignToken = matchToken(TokenType.ASSIGN);
        ExpNode expNode = Exp();
        return new ForStmtNode(lValNode, assignToken, expNode);
    }

    // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    private AddExpNode AddExp() {
        MulExpNode mulExpNode = MulExp();
        Token opToken = null;
        AddExpNode addExpNode = null;

        // 存在('+' | '−') 那么捕获外层结构，然后组装回一层层的AddExp
        while(curToken.type == TokenType.PLUS || curToken.type == TokenType.MINU){
            // 将上一轮捕获的单位进行组装
            addExpNode = new AddExpNode(mulExpNode, opToken, addExpNode);
            // 以('+' | '−') MulExp為單位继续进行捕获
            opToken = matchToken(curToken.type);
            mulExpNode = MulExp();
        }
        return new AddExpNode(mulExpNode, opToken, addExpNode);
    }

    // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
    private PrimaryExpNode PrimaryExp() {

        Token lparentToken = null;
        ExpNode expNode = null;
        Token rparentToken = null;
        LValNode lValNode = null;
        NumberNode numberNode = null;
        // '(' Exp ')'
        if (curToken.type == TokenType.LPARENT) {
            lparentToken = matchToken(TokenType.LPARENT);
            expNode = Exp();
            rparentToken = matchToken(TokenType.RPARENT);
        }
        // Number
        else if (curToken.type == TokenType.INTCON) {
            numberNode = Number();
        }
        // LVal
        else {
            lValNode = LVal();
        }
        return new PrimaryExpNode(lparentToken, expNode, rparentToken, lValNode, numberNode);
    }

    // 数值 Number → IntConst
    private NumberNode Number() {
        Token intConstToken = matchToken(TokenType.INTCON);
        return new NumberNode(intConstToken);
    }

    // 不能依据右括号来判断是否进入params的判断，而应该根据Exp的First集来判断，因为右括号可能会缺失
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // 存在即可
    // PrimaryExp → '(' Exp ')' | LVal | Number
    //  LVal → Ident {'[' Exp ']'}
    private final ArrayList<TokenType> expFirstSetList = new ArrayList<>(Arrays.asList(TokenType.IDENFR, TokenType.PLUS, TokenType.MINU, TokenType.NOT, TokenType.LPARENT, TokenType.INTCON));
    private boolean judgeExpFirstSet(){
        return expFirstSetList.contains(curToken.type);
    }
    // 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'
    private UnaryExpNode UnaryExp() {
//        System.out.println("unary" + curToken);
        PrimaryExpNode primaryExpNode = null;
        Token identToken = null;
        Token lparentToken = null;
        FuncRParamsNode funcRParamsNode = null;
        Token rparentToken = null;
        UnaryOpNode unaryOpNode = null;
        UnaryExpNode unaryExpNode = null;
        // Ident '(' [FuncRParams] ')'
        if(curToken.type == TokenType.IDENFR && preMatchToken(1, TokenType.LPARENT)){
            identToken = matchToken(TokenType.IDENFR);
            lparentToken = matchToken(TokenType.LPARENT);
            if(judgeExpFirstSet()){
                funcRParamsNode = FuncRParams();
            }
            rparentToken = matchToken(TokenType.RPARENT);
        }
        // UnaryOp UnaryExp
        else if(curToken.type == TokenType.PLUS || curToken.type == TokenType.MINU || curToken.type == TokenType.NOT){
            unaryOpNode = UnaryOp();
            unaryExpNode = UnaryExp();
        }
        // PrimaryExp
        else{
            primaryExpNode = PrimaryExp();
        }
        return new UnaryExpNode(primaryExpNode, identToken, lparentToken, funcRParamsNode, rparentToken, unaryOpNode, unaryExpNode);
    }

    // 函数实参表 FuncRParams → Exp { ',' Exp }
    private FuncRParamsNode FuncRParams(){
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        expNodes.add(Exp());
        while(curToken.type == TokenType.COMMA){
            commaTokens.add(matchToken(TokenType.COMMA));
            expNodes.add(Exp());
        }
        return new FuncRParamsNode(expNodes, commaTokens);
    }

    // 单目运算符 UnaryOp → '+' | '−' | '!'
    private UnaryOpNode UnaryOp(){
        Token opToken;
        if(curToken.type == TokenType.PLUS || curToken.type == TokenType.MINU || curToken.type == TokenType.NOT){
            opToken = matchToken(curToken.type);
        }
        // 失配，后续可能需要修改这里
        else {
            opToken = matchToken(TokenType.PLUS);
        }
        return new UnaryOpNode(opToken);
    }

    // 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 可以考虑循环提取所有unary之后再拼接回去
    private MulExpNode MulExp(){
        UnaryExpNode unaryExpNode = UnaryExp();
        Token opToken = null;
        MulExpNode mulExpNode = null;

        // 存在('+' | '−') 那么捕获外层结构，然后组装回一层层的AddExp
        while(curToken.type == TokenType.MULT || curToken.type == TokenType.DIV || curToken.type == TokenType.MOD){
            // 将上一轮捕获的单位进行组装
            mulExpNode = new MulExpNode(unaryExpNode, opToken, mulExpNode);
            opToken = matchToken(curToken.type);
            unaryExpNode = UnaryExp();
        }
        return new MulExpNode(unaryExpNode, opToken, mulExpNode);
    }

    // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // RelExp → AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp 
    private RelExpNode RelExp(){
        AddExpNode addExpNode = AddExp();
        Token opToken = null;
        RelExpNode relExpNode = null;

        while(curToken.type == TokenType.LSS || curToken.type == TokenType.GRE || curToken.type == TokenType.LEQ || curToken.type == TokenType.GEQ){
            // 将上一轮捕获的单位进行组装
            relExpNode = new RelExpNode(addExpNode, opToken, relExpNode);
            opToken = matchToken(curToken.type);
            addExpNode = AddExp();
        }
        return new RelExpNode(addExpNode, opToken, relExpNode);
    }

    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private EqExpNode EqExp(){
        RelExpNode relExpNode = RelExp();
        Token opToken = null;
        EqExpNode eqExpNode = null;
        while(curToken.type == TokenType.EQL || curToken.type == TokenType.NEQ){
            // 将上一轮捕获的单位进行组装
            eqExpNode = new EqExpNode(relExpNode, opToken, eqExpNode);
            opToken = matchToken(curToken.type);
            relExpNode = RelExp();
        }
        return new EqExpNode(relExpNode, opToken, eqExpNode);
    }

    // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    private LAndExpNode LAndExp(){
        EqExpNode eqExpNode = EqExp();
        Token opToken = null;
        LAndExpNode lAndExpNode = null;

        while(curToken.type == TokenType.AND){
            // 将上一轮捕获的单位进行组装
            lAndExpNode = new LAndExpNode(eqExpNode, opToken, lAndExpNode);
            opToken = matchToken(curToken.type);
            eqExpNode = EqExp();
        }
        return new LAndExpNode(eqExpNode, opToken, lAndExpNode);
    }

    // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    private LOrExpNode LOrExp(){
        LAndExpNode lAndExpNode = LAndExp();
        Token opToken = null;
        LOrExpNode lOrExpNode = null;

        while(curToken.type == TokenType.OR){
            // 将上一轮捕获的单位进行组装
            lOrExpNode = new LOrExpNode(lAndExpNode, opToken, lOrExpNode);
            opToken = matchToken(curToken.type);
            lAndExpNode = LAndExp();
        }
        return new LOrExpNode(lAndExpNode, opToken, lOrExpNode);
    }
}
