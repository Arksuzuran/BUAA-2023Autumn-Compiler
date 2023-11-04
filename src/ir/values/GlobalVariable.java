package ir.values;

import ir.types.ValueType;
import ir.values.constants.Constant;

import java.util.ArrayList;

/**
 * @Description 全局变量
 * @Author
 * @Date 2023/10/30
 **/
public class GlobalVariable extends User{
    private final boolean isConst;
    private Constant initValue = null;

    /**
     * 常量声明一定带有初始化
     * 全局变量以@开头
     */
    public GlobalVariable(String name, ValueType type, boolean isConst, ArrayList<Value> operands) {
        super("@" + name, type, Module.getInstance(), operands);
        this.isConst = isConst;
    }
}
