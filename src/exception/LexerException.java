package exception;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/15
 **/
public class LexerException extends Exception{
    private int lineNum;
    public LexerException(int lineNum){
        super("Word analyses error: line" + lineNum);
        this.lineNum = lineNum;
    }
}
