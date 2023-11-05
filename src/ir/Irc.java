package ir;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;

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
     * 当前是否正在计算 无变量常数表达式
     * 如果是，那么综合属性只需要传递syvInt，且计算情况有所减少
     */
    public static boolean isBuildingConstExp = false;
    /**
     * 当前是否在进行全局变量的初始化
     */
    public static boolean isBuildingGlobalInit = false;
    /**
     *  当前是否正在构建一个int类型的实参
     *  如果是，但是当前解析到的却是int*类型，那么需要load
     */
    public static boolean isBuildingPointerRParam = false;

    //=========================== 综合属性 =================================
    /**
     * Value类型列表的综合属性 up向上传递
     */
    public static ArrayList<Value> synValueArray = null;
    /**
     * Value类型的综合属性 up向上传递
     */
    public static Value synValue = null;
    /**
     * int类型的综合属性 up向上传递
     */
    public static int synInt = -1;
    //============================= 继承属性 =================================
    /**
     * Value类型的继承属性 down向下传递
     */
    public static Value inValue = null;
    /**
     * int类型的继承属性 down向下传递
     */
    public static int inInt = -1;
}
