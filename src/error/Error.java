package error;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/13
 **/
public class Error {
    private ErrorType type;
    private int lineNum;

    public Error(ErrorType type, int lineNum) {
        this.type = type;
        this.lineNum = lineNum;
    }
}
