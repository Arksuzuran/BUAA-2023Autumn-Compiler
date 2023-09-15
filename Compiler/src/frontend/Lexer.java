package frontend;

import exception.LexerException;

import java.util.ArrayList;
import java.util.List;


/**
 * @Description 词法分析器
 * @Author H1KARI
 * @Date 2023/9/14
 **/
public class Lexer {
    private final String text;    // 文本
    private final int maxPos;     // pos最大值
    private int pos;        // 当前扫描到了文本字符串的哪个单词

    private int lineNum;    // 当前所在行号
    private Token<?> token;

    private final List<Token<?>> lexerResultList = new ArrayList<>();  //扫描结果列表

    public Lexer(String text) {
        this.text = text;
        this.pos = 0;
        this.lineNum = 1;
        this.token = null;
        this.maxPos = text.length() - 1;
    }

    // 跳过空白字符
    private void jumpWhiteCharacter(){
        // 跳过空白字符
        while (pos <= maxPos && Character.isWhitespace(text.charAt(pos))){
            // linux下换行符\n
            if(text.charAt(pos) == '\n'){
                lineNum++;
            }
            // windows下换行符为两个，\r\n
            else if(text.charAt(pos) == '\r' && pos < maxPos && text.charAt(pos+1) == '\n'){
                pos++;
                lineNum++;
            }
            pos++;
        }
    }
    // 跳过注释
    private void jumpNote(){
        if(pos >= maxPos){
            return;
        }
        // 多行注释
        if(text.charAt(pos) == '\\' && text.charAt(pos+1) == '*'){
            while(pos <= maxPos){
                //可以跳出注释了
                if(pos < maxPos && text.charAt(pos) == '*' && text.charAt(pos+1) == '\\'){
                    pos += 2;
                  return;
                }
                pos++;
            }
        }
        // 单行注释
        else if(text.charAt(pos) == '\\' && text.charAt(pos+1) == '\\') {
            while (pos <= maxPos) {
                //可以跳出注释了
                if (pos < maxPos && text.charAt(pos) == '\r' && text.charAt(pos + 1) == '\n') {
                    pos += 2;
                    return;
                } else if (text.charAt(pos) == '\n') {
                    pos++;
                    return;
                }
                pos++;
            }
        }
    }
    // 立即获取下一个token
    public Token<?> next () throws LexerException {
        // 当前是否在单行注释中
        boolean inSingleNote = false;
        // 当前是否处在多行注释中
        boolean inMultipleNote = false;
        // 当前是否处在字符串中
        boolean inStrNote = false;
        // 清空当前token
        token = null;

        // 跳过注释和空白符
        jumpWhiteCharacter();
        jumpNote();
        jumpWhiteCharacter();
        // 已经完成读取
        if (pos > maxPos) {
            return null;
        }

        // 按照首字符进行贪心匹配
        char chr = text.charAt(pos);
        // 1.数字开头 应为数字
        if (Character.isDigit(chr)) {
            int endPos = pos + 1; // 截取字符串的末端的后一个位置

            // 截取该token的字符串
            while (endPos <= maxPos && Character.isDigit(text.charAt(endPos))) {
                endPos++;
            }
            // 查看最末尾字符，如果不是空字符则报错
            if(endPos <= maxPos && !Character.isWhitespace(text.charAt(endPos))){
                throw new LexerException(lineNum);
            }
            String str = text.substring(pos, endPos);
            // 注意不要忘了更新pos
            pos = endPos;

            // 生成token对象
            token = new Token<Integer>(Integer.parseInt(str), lineNum, TokenType.INTCON);

        }
        // 2.字母开头 应为保留字或者标识符
        else if (Character.isLetter(chr)) {
            int endPos = pos + 1; // 截取字符串的末端的后一个位置

            // 截取该token的字符串
            while (endPos <= maxPos &&
                    (Character.isDigit(text.charAt(endPos)) ||
                            Character.isLetter(text.charAt(endPos)) ||
                            text.charAt(endPos) == '_')) {
                endPos++;
            }
            // 查看最末尾字符，如果不是空字符则报错
            if(endPos <= maxPos && !Character.isWhitespace(text.charAt(endPos))){
                throw new LexerException(lineNum);
            }
            String str = text.substring(pos, endPos);
            // 注意不要忘了更新pos
            pos = endPos;

            // 生成token对象
            TokenType type = TokenType.isReservedToken(str); // 先判断是否是保留字
            // 是保留字
            if(type!=null){
                token = new Token<String>(str, lineNum, type);
            }
            // 是标识符
            else{
                token = new Token<String>(str, lineNum, TokenType.IDENFR);
            }
        }
        // 3.双引号 格式化字符串
        else if(chr == '"'){
            int endPos = pos + 1;
            while (endPos <= maxPos && text.charAt(endPos) != '"'){
                endPos++;
            }
            // 查看最末尾字符，如果不是"则报错
            if(endPos > maxPos){
                throw new LexerException(lineNum);
            }
            // 注意末尾的"也要包括
            endPos++;
            String str = text.substring(pos, endPos);
            // 注意不要忘了更新pos
            pos = endPos;

            // 生成token对象
            token = new Token<String>(str, lineNum, TokenType.STRCON);
        }
        return token;
    }

}

