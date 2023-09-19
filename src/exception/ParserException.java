package exception;

import frontend.Parser;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class ParserException extends Exception{
    private int lineNum;
    public ParserException(int lineNum){
        super("Parser analyses error: line" + lineNum);
        this.lineNum = lineNum;
    }
}
