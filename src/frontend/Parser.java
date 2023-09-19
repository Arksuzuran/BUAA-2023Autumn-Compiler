package frontend;

import node.*;
import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description 语法分析器 递归下降子程序实现
 * @Author  H1KARI
 * @Date 2023/9/18
 **/
public class Parser {
    // 词法分析器的token列表
    private final ArrayList<Token> tokens;

    // 当前扫描到的位置
    private int pos;
    // 最末位置
    private int maxPos;
    // 当前token
    private Token curToken = null;

    public Parser(ArrayList<Token> tokens){
        this.tokens = tokens;
        this.pos = 0;
        this.maxPos = tokens.size() - 1;
        this.curToken = tokens.get(pos);
    }

    // 所有匹配最终都会归结到对终结符的匹配上，而取下一个终结符的时机就是上一个终结符被读取时，因此将这两个操作合二为一：
    // 将当前token与指定类型的token相匹配，匹配成功则推进匹配进度pos++，否则进行报错
    private Token matchToken(TokenType tokenType) {
        if(curToken.type == tokenType){
            Token tmp = curToken;
            if(pos < maxPos){
                curToken = tokens.get(++pos);
            }
            return tmp;
        }
        return null;
    }
    // 向前预先读取shift个token并进行匹配，但不改变pos的值
    private boolean preMatchToken(int shift, TokenType tokenType){
        if(pos + shift <= maxPos){
            Token token = shift == 0 ? curToken : tokens.get(pos + shift);
            if(token.type == tokenType){
                return true;
            }
        }
        return false;
    }
    //
    // 递归下降子程序
    //

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    // MainFuncDef  int main()
    // FuncDef      int/void f()
    // Decl         [const] int var =
    private CompUnitNode CompUnit(){
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode = null;
        // Decl
        while (!preMatchToken(2, TokenType.LPARENT)){
            declNodes.add(Decl());
        }
        // FuncDef
        while(!preMatchToken(1, TokenType.MAINTK)){
            funcDefNodes.add(FuncDef());
        }
        // MainFuncDef
        mainFuncDefNode = MainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    //  Decl → ConstDecl | VarDecl
    //  在First集合中，ConstDecl 带有 'const'
    private DeclNode Decl(){
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if(curToken.type == TokenType.CONSTTK){
            constDeclNode = ConstDecl();
        } else {
            varDeclNode = VarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    private FuncDefNode FuncDef(){
        return null;
    }

    private MainFuncDefNode MainFuncDef(){
        return null;
    }

    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDeclNode ConstDecl(){
        Token constToken = matchToken(TokenType.CONSTTK);
        BTypeNode bTypeNode = BType();
        ConstDefNode constDefNode = ConstDef();
        ArrayList<Token> commas = new ArrayList<>();
        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
        while(curToken.type == TokenType.COMMA){
            commas.add(matchToken(TokenType.COMMA));
            constDefNodes.add(ConstDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNode, commas, constDefNodes, semicnToken);
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    private VarDeclNode VarDecl(){
        BTypeNode bTypeNode = BType();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        varDefNodes.add(VarDef());
        while(curToken.type == TokenType.COMMA){
            commas.add(matchToken(TokenType.COMMA));
            varDefNodes.add(VarDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new VarDeclNode(bTypeNode, varDefNodes, commas, semicnToken);
    }

    // BType → 'int'
    private BTypeNode BType() {
        Token intToken = matchToken(TokenType.INTTK);
        return new BTypeNode(intToken);
    }

    // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private ConstDefNode ConstDef(){
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        while(curToken.type == TokenType.LBRACK){
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rbrackTokens.add(matchToken(TokenType.RBRACK));
        }
        Token assignToken = matchToken(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = ConstInitVal();
        return new ConstDefNode(identToken, lbrackTokens, constExpNodes, rbrackTokens, assignToken, constInitValNode);
    }

    private ConstExpNode ConstExp(){
        return null;
    }

    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 后者的first是 LBRACE, 以此进行区分
    private ConstInitValNode ConstInitVal(){
        ConstExpNode constExpNode = null;
        Token lbraceToken = null;
        ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        Token rbraceToken = null;
        // 数组的初始化, 以{开头
        if(curToken.type == TokenType.LBRACE){
            lbraceToken = matchToken(TokenType.LBRACE);
            // 下一个token不是右括号, 那么中间应当含有constinitval
            if(curToken.type != TokenType.RBRACE){
                constInitValNodes.add(ConstInitVal());
                while(curToken.type == TokenType.COMMA){
                    commas.add(matchToken(TokenType.COMMA));
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
        return new ConstInitValNode(constExpNode, lbraceToken, constInitValNodes, commas, rbraceToken);
    }

    // VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private VarDefNode VarDef(){
        Token identToken = matchToken(TokenType.IDENFR);
        ArrayList<Token> lbrackTokens = new ArrayList<>();
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> rbrackTokens = new ArrayList<>();
        Token assignToken = null;
        InitValNode initValNode = null;
        // 数组指定元素初始化
        while(curToken.type == TokenType.LBRACK){
            lbrackTokens.add(matchToken(TokenType.LBRACK));
            constExpNodes.add(ConstExp());
            rbrackTokens.add(matchToken(TokenType.RBRACK));
        }
        // 根据是否有等号来判断是否执行初始化
        if(curToken.type == TokenType.ASSIGN){
            assignToken = matchToken(TokenType.ASSIGN);
            initValNode = InitVal();
        }
        return new VarDefNode(identToken, lbrackTokens, constExpNodes, rbrackTokens, assignToken, initValNode);
    }

    private InitValNode InitVal(){
        return null;
    }
}
