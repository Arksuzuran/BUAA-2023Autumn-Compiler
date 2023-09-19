package frontend;

import node.*;
import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description 语法分析器 递归下降子程序
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

    // 所有匹配最终都会归结到对终结符的匹配上
    // 将当前token与指定类型的token相匹配
    // 匹配成功则推进匹配进度，否则进行报错
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
    // 递归下降子程序

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
        if(preMatchToken(0, TokenType.CONSTTK)){
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

        return null;
    }

    private VarDeclNode VarDecl(){
        return null;
    }

    private BTypeNode BType() {
        Token intToken = matchToken(TokenType.INTTK);
        return new BTypeNode(intToken);
    }
}
