package node;

import token.Token;

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
}
