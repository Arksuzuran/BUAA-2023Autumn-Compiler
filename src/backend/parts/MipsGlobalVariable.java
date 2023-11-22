package backend.parts;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsGlobalVariable {
    public enum GVType {
        String, // 字符串
        Zero,   // 未初始化
        Int,    // int类型或int数组
    }

    private String name;

    public GVType getType() {
        return type;
    }

    private GVType type;
    private String string;  // 字符串内容
    private int size;
    private final ArrayList<Integer> ints = new ArrayList<>();  // int初始化值

    /**
     * 字符串全局变量
     */
    public MipsGlobalVariable(String name, String string) {
        // 需要去除开头的@
        this.name = name.substring(1);
        this.string = string;
        this.type = GVType.String;
    }

    /**
     * 未初始化的全局数组变量
     */
    public MipsGlobalVariable(String name, int size) {
        this.name = name.substring(1);
        this.size = size;
        this.type = GVType.Zero;
    }

    /**
     * int或int数组的全局变量
     */
    public MipsGlobalVariable(String name, ArrayList<Integer> ints) {
        this.name = name.substring(1);
        this.ints.addAll(ints);
        this.type = GVType.Int;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        // 初始化了就用 .word 或者 .ascii
        switch (type) {
            // 未初始化,使用 .space
            case Zero -> {
                sb.append(".space\t").append(size).append("\n");
            }
            // 字符串 .asciiz
            case String -> {
                sb.append(".asciiz\t\"").append(string).append("\"\n");
            }
            // int变量或数组 .word
            case Int -> {
                for (Integer integer : ints) {
                    sb.append(".word\t").append(integer).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
