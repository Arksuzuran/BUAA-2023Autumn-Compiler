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


    public StmtNode(StmtType type, ArrayList<Token> tokens, ArrayList<Node> nodes) {
        super(NodeType.Stmt);
        this.type = type;
        this.tokens = tokens;
        this.nodes = nodes;
    }
}
