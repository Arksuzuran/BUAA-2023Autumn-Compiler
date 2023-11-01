package ir.values.constants;

import ir.types.ValueType;
import ir.values.User;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/31
 **/
public class Constant extends User {

    public Constant(String name, ValueType type, Value parent) {
        super(name, type, parent);
    }

    /**
     * 带初始值的Const
     */
    public Constant(String name, ValueType type, Value parent, Value... operands) {
        super(name, type, parent, operands);
    }
}
