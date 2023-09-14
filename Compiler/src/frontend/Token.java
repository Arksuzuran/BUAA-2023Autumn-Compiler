package frontend;

/**
 * @Description 封装Token
 * @Author  HIKARI
 * @Date 2023/9/14
 **/
public class Token {
    public String str;
    public int lineNum;
    public TokenType type;

    Token(String str, int lineNum, TokenType type){
        this.str = str;
        this.lineNum = lineNum;
        this.type = type;
    }
}
