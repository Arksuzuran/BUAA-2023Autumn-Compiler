package frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 词法分析器
 * @Author H1KARI
 * @Date 2023/9/14
 **/
public class Lexer {
    private final String text;    // 文本
    private final int textLen;     // 文本字符串长度
    private int pos;        // 当前扫描到了文本字符串的哪个单词

    private int lineNum;    // 当前所在行号
    private String token;   // 当前所截取的单词

    private final List<Token> lexerResultList = new ArrayList<>();
    public Lexer(String text){
        this.text = text;
        pos = 0;
        lineNum = 1;
        token = "";
        textLen = text.length();
    }

    // 立即获取下一个token
    public Token next(){
        if(pos >= textLen - 1){
            return null;
        }
        return null;
    }

}
