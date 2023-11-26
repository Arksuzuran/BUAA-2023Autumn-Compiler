package backend.instructions;

/**
 * @Description 代码注释
 * @Author
 * @Date 2023/11/25
 **/
public class MipsAnnotation extends MipsInstruction{
    private String content;

    public MipsAnnotation(String content) {
        this.content = content;
    }
    @Override
    public String toString() {
        return "# " + content + "\n";
    }
}
