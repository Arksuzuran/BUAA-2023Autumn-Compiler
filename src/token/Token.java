package token;

import utils.IO;

/**
 * @Description 封装Token
 * @Author  HIKARI
 * @Date 2023/9/14
 **/
public class Token {
    public String str; // 读入的字符串
    public int lineNum;
    public TokenType type;

    public Token(String str, int lineNum, TokenType type){
        this.str = str;
        this.lineNum = lineNum;
        this.type = type;
    }
    @Override
    public String toString(){
        return type + " " + str;
    }

    // 将当前token输出至文件
    public void print(){
        IO.write(IO.IOType.PARSER, this.toString(), true, true);
    }
}
