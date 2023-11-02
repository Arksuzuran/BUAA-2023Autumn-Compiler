package ir.values.constants;

import ir.types.ValueType;
import ir.values.User;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @Description 常量类 无需指定name和parent
 * @Author
 * @Date 2023/10/31
 **/
public class Constant extends User {

    /**
     * 无初始值的常量
     * @param type  常量类型
     */
    public Constant(ValueType type) {
        super("", type, null);
    }

    /**
     * 带初始值的常量
     * @param type  常量类型
     * @param operands
     */
    public Constant(ValueType type, ArrayList<Value> operands) {
        super("", type, null, operands);
    }
}
