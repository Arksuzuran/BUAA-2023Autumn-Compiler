package backend.parts;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsGlobalVariable {
    public enum GVType{
        String, // 字符串
        Zero,   // 未初始化
        Int,    // int类型或int数组
    }
    private String name;
    private GVType type;
    private String string;  // 字符串内容
    private int size;
    private final ArrayList<Integer> ints = new ArrayList<>();  // int初始化值

    /**
     * 字符串全局变量
     */
    public MipsGlobalVariable(String name, String string) {
        this.name = name;
        this.string = string;
        this.type = GVType.String;
    }
    /**
     *  未初始化的全局数组变量
     */
    public MipsGlobalVariable(String name, int size) {
        this.name = name;
        this.size = size;
        this.type = GVType.Zero;
    }
    /**
     *  int或int数组的全局变量
     */
    public MipsGlobalVariable(String name, ArrayList<Integer> ints) {
        this.name = name;
        this.ints.addAll(ints);
        this.type = GVType.Int;
    }
}
