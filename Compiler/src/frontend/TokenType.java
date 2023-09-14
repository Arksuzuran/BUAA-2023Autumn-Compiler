package frontend;

import java.util.Arrays;
import java.util.List;

/**
 * @Description Token type enum
 * @Author H1KARI
 * @Date 2023/9/14
 **/
public enum TokenType {
    // 保留字
    INTTK("int"),
    MAINTK("main"),
    CONSTTK("const"),
    BREAKTK("break"),
    CONTINUETK("continue"),
    IFTK("if"),
    ELSETK("else"),
    FORTK("for"),
    GETINTTK("getint"),
    PRINTFTK("printf"),
    RETURNTK("return"),
    VOIDTK("void"),

    // 运算符
    AND("&&"),
    OR("||"),
    PLUS("+"),
    MINU("-"),
    MULT("*"),
    DIV("/"),
    MOD("%"),
    ASSIGN("="),

    LEQ("<="),
    GEQ(">="),
    EQL("=="),
    NEQ("!="),
    NOT("!"),
    LSS("<"),
    RSS(">"),

    // 括号、逗号、分号
    LPARENT("("),
    RPARENT(")"),
    LBRACK("["),
    RBRACK("]"),
    LBRACE("{"),
    RBRACE("}"),
    SEMICN(";"),
    COMMA(","),

    // 标识符
    IDENFR("[a-zA-Z_][a-zA-Z_0-9]*"),
    // int整数
    INTCON("[1-9][0-9]*|0"),
    // 字符串
    STRCON("\".*?\"");

    private final String str;

    //    private final int id;
    TokenType(String str) {
        this.str = str;
//        this.id = id;
    }

    // 保留字列表
    public static final List<String> reservedTokenList = Arrays.asList(
            "int", "main", "break", "continue",
            "for", "return", "const", "getint",
            "if", "else", "void", "printf");

    // 判断所给字符串是否是保留字 如果是则返回其对应的TOKEN枚举类型
    public static TokenType isReservedToken(String str){
        if(reservedTokenList.contains(str)){
            return TokenType.valueOf(str);
        }
        return null;
    }

    @Override
    public String toString() {
        return this.str;
    }
}
