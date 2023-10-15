package frontend;

import exception.LexerException;
import token.Token;
import token.TokenType;
import utils.IO;

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
    private Token token;

    // 扫描结果列表
    private final ArrayList<Token> lexerResultList = new ArrayList<>();
    public ArrayList<Token> getLexerResultList(){
        return lexerResultList;
    }

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
            pos++;
        }
    }

    // 跳过注释
    // return 0: 未跳过任何注释
    // return 1: 跳过了注释
    private int jumpNote(){
        if(pos >= maxPos){
            return 0;
        }
        // 多行注释
        if(text.charAt(pos) == '/' && text.charAt(pos+1) == '*'){
            pos += 2;
            while(pos <= maxPos){
                // 可以跳出注释了
                if(pos < maxPos && text.charAt(pos) == '*' && text.charAt(pos+1) == '/'){
                    pos += 2;
                    return 1;
                }
                // 无法跳出注释，那么更新行号，同时继续往后扫描
                if (text.charAt(pos) == '\n') {
                    lineNum++;
                }
                pos++;
            }
        }
        // 单行注释
        else if(text.charAt(pos) == '/' && text.charAt(pos+1) == '/') {
            pos += 2;
            while (pos <= maxPos) {
//                System.out.println("扫描注释：" + text.charAt(pos) + "\n");
                // 可以跳出注释了，同时别忘了更新行号
                if (text.charAt(pos) == '\n') {
                    lineNum++;
                    pos++;
                    return 1;
                }
                pos++;
            }
        }
        // 注释一直持续到文件末尾，那么也算作是扫描到了注释
        if(pos >= maxPos){
            return 1;
        }
        return 0;
    }

    // 立即获取下一个token
    public Token next () throws LexerException {
        // 清空当前token
        token = null;
        // 跳过注释和空白符
        jumpWhiteCharacter();
        while(jumpNote() != 0){
            jumpWhiteCharacter();
        }
        // 已经完成读取
        if (pos > maxPos) {
            return null;
        }
//        System.out.println("调用next方法开始扫描：" + text.charAt(pos));
        // 按照首字符进行贪心匹配
        char chr = text.charAt(pos);
        // 1.数字开头 应为数字
        if (Character.isDigit(chr)) {
            int endPos = pos + 1; // 截取字符串的末端的后一个位置

            // 截取该token的字符串
            while (endPos <= maxPos && Character.isDigit(text.charAt(endPos))) {
                endPos++;
            }
            String str = text.substring(pos, endPos);
            // 注意不要忘了更新pos
            pos = endPos;

            // 生成token对象
            token = new Token(str, lineNum, TokenType.INTCON);
        }
        // 2.字母或下划线开头 应为保留字或者标识符
        else if (Character.isLetter(chr) || chr == '_') {
            int endPos = pos + 1; // 截取字符串的末端的后一个位置

            // 截取该token的字符串
            while (endPos <= maxPos &&
                    (Character.isDigit(text.charAt(endPos)) ||
                            Character.isLetter(text.charAt(endPos)) ||
                            text.charAt(endPos) == '_')) {
                endPos++;
            }
            String str = text.substring(pos, endPos);
            // 注意不要忘了更新pos
            pos = endPos;

            // 生成token对象
            TokenType type = TokenType.isReservedToken(str); // 先判断是否是保留字
            // 是保留字
            if(type!=null){
                token = new Token(str, lineNum, type);
            }
            // 是标识符
            else{
                token = new Token(str, lineNum, TokenType.IDENFR);
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
            token = new Token(str, lineNum, TokenType.STRCON);
        }
        // 4.单字符或双字符分隔符
        else{
            boolean flagSingle = TokenType.singleCharDeliList.contains(chr);
            boolean flagDouble = TokenType.doubleCharDeliList.contains(chr);
            TokenType tokenType = null;
            String str = "";
            int posMove = 0;
            // 对于单字符分隔符，那么直接生成token
            if(flagSingle){
                tokenType = TokenType.getTokenType((str = text.substring(pos, pos+1)));
                posMove = 1;
            }
            // 可能的双字符分隔符 要截取两位来生成token
            if(pos < maxPos && flagDouble){
                TokenType tmp = TokenType.getTokenType(text.substring(pos, pos+2));
                // 确认生成成功后，再进行记录，以防覆盖单双皆可的结果
                if(tmp != null){
                    tokenType = tmp;
                    str = text.substring(pos, pos+2);
                    posMove = 2;
                }
            }
            if(tokenType != null){
                token = new Token(str, lineNum, tokenType);
                pos += posMove;
            }
        }
        // 进行记录
        if(token != null){
            lexerResultList.add(token);
        }
        // 在此产生报错
        return token;
    }

    // 直接进行一次完整的扫描
    public void doLexicalAnalysisByPass(boolean printResult){
        Token token = null;
        try {
            do {
                token = next();
                if (token != null && printResult) {
                    System.out.println(token);
                }
            } while (token != null);
        } catch (LexerException e) {
            System.out.println(e);
        }
    }

    // 输出词法分析结果到文件
    public void outputLexicalResult(){
        // 读取词法分析结果
        StringBuilder stringBuilder = new StringBuilder();
        for(Token token : lexerResultList){
            stringBuilder.append(token.type).append(" ").append(token.str).append("\n");
        }
        String result = stringBuilder.toString();
        // 输出词法分析结果至文件
        IO.write(IO.IOType.LEXER, result, false, false);
    }
}

