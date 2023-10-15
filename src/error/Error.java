package error;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/13
 **/
public class Error {
    public ErrorType getType() {
        return type;
    }

    private ErrorType type;

    public int getLineNum() {
        return lineNum;
    }

    private int lineNum;

    public Error(ErrorType type, int lineNum) {
        this.type = type;
        this.lineNum = lineNum;
    }

    public String getOutputString(){
        return lineNum + " " + type + "\n";
    }
}
