package utils;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsTool {
    public static boolean is16BitImm(int imm, boolean signExtend) {
        if (signExtend) {
            return Short.MIN_VALUE <= imm && imm <= Short.MAX_VALUE;
        }
        else {
            return 0 <= imm && imm <= (Short.MAX_VALUE - Short.MIN_VALUE);
        }
    }
}
