package ir.values;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.constants.Constant;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 全局变量
 * @Author
 * @Date 2023/10/30
 **/
public class GlobalVariable extends User{
    private final boolean isConst;

    public Constant getInitValue() {
        return initValue;
    }

    private Constant initValue = null;

    /**
     * 常量声明一定带有初始化
     * 全局变量本身以指针形式存在
     * 全局变量以@开头
     */
    public GlobalVariable(String name, boolean isConst, Constant initValue) {
        super("@" + name,
                new PointerType(initValue.getType()),
                Module.getInstance(),
                new ArrayList<>(){{add(initValue);}});
        this.isConst = isConst;
    }

    // @a = dso_local global [6 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5, i32 6]
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        // 头部
        stringBuilder.append(getName())
                .append(" = dso_local ");
        // 常量或变量
        if(isConst){
            stringBuilder.append("constant ");
        } else {
            stringBuilder.append("global ");
        }
        // 变量类型应当为指针指向的类型
        stringBuilder.append(getOperands().get(0).getType())
                .append(" ")
                .append(getOperands().get(0))
                .append("\n");
        return stringBuilder.toString();
    }
}
