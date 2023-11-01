package ir;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

/**
 * @Description IrContext 为了少打一点字所以写成Irc
 * 生成中间代码的递归下降过程中，存储继承属性、综合属性、当前块等全局变量（上下文）的类
 * @Author  H1KARI
 * @Date 2023/10/31
 **/
public class Irc {
    /**
     * 当前所在基本块
     */
    public static BasicBlock curBlock = null;
    /**
     * 当前所在函数
     */
    public static Function curFunction = null;

    /**
     * 综合属性 up向上传递
     */
    public static Value synValue = null;
    /**
     * 继承属性 down向下传递
     */
    public static Value inValue = null;

}
