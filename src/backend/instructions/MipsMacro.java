package backend.instructions;

/**
 * @Description 宏调用或者注释
 * @Author
 * @Date 2023/11/19
 **/
public class MipsMacro extends MipsInstruction{
    private String content;

    public MipsMacro(String content) {
        this.content = content;
    }
    @Override
    public String toString() {
        return content + "\n";
    }
}
