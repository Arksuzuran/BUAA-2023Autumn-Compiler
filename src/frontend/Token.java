package frontend;

/**
 * @Description 封装Token
 * @Author  HIKARI
 * @Date 2023/9/14
 **/
public class Token<T> {
    public T val;
    public String str; // 读入的字符串
    public int lineNum;
    public TokenType type;

    Token(T val, String str, int lineNum, TokenType type){
        this.val = val;
        this.str = str;
        this.lineNum = lineNum;
        this.type = type;
    }
    @Override
    public String toString(){
        return type + " " + str;
    }
}
