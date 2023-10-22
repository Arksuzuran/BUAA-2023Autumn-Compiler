package token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description Token类型的枚举类，同时保存有保留字、双分界符等常量及其相应方法
 * @Author H1KARI
 * @Date 2023/9/14
 **/
public enum TokenType {
    // 标识符
    IDENFR("[a-zA-Z_][a-zA-Z_0-9]*"),
    // int整数
    INTCON("[1-9][0-9]*|0"),
    // 字符串
    STRCON("\".*?\""),

    // 保留字
    MAINTK("main"),
    CONSTTK("const"),
    INTTK("int"),
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
    NOT("!"),
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
    LSS("<"),
    GRE(">"),

    // 括号、逗号、分号
    LPARENT("("),
    RPARENT(")"),
    LBRACK("["),
    RBRACK("]"),
    LBRACE("{"),
    RBRACE("}"),
    SEMICN(";"),
    COMMA(",");

    private final String str;
    public String getStr(){
        return str;
    }
    //    private final int id;
    TokenType(String str) {
        this.str = str;
    }
    // 从字符串到TokenType的映射
    private static final Map<String, TokenType> str2TokenTypeMap = new HashMap<>();
    static {
        for (TokenType tokenType : TokenType.values()) {
            str2TokenTypeMap.put(tokenType.getStr(), tokenType);
        }
    }
    // 根据所给str返回相应的非标识符或常量的TokenType
    public static TokenType getTokenType(String str){
        return str2TokenTypeMap.get(str);
    }

    // 1.保留字
    // 保留字列表
    public static final List<String> reservedTokenList = Arrays.asList(
            "int", "main", "break", "continue",
            "for", "return", "const", "getint",
            "if", "else", "void", "printf");
    // 判断所给字符串是否是保留字 如果是则返回其对应的TOKEN枚举类型
    public static TokenType isReservedToken(String str){
        if(reservedTokenList.contains(str)){
            return getTokenType(str);
        }
        return null;
    }


    // 2.单/双字符分界符
    // 单字符分界符列表
    public static final List<Character> singleCharDeliList = Arrays.asList(
            '+', '-', '*', '/', '%', '>', '<', '=', '!', ',', ';', '(', ')', '[', ']', '{', '}');
    // 双字符分界符中 首字符的列表
    // && || <= >= == !=
    public static final List<Character> doubleCharDeliList = Arrays.asList(
            '&', '|', '<', '>', '=', '!');
//    @Override
//    public String toString() {
//        return this.str;
//    }
}
