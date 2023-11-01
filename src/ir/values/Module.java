package ir.values;

import java.util.ArrayList;

/**
 * @Description 整个Ir树的根，其下挂载有function，globalVariable
 * @Author
 * @Date 2023/10/30
 **/
public class Module{
    // 单例模式
    private static final Module module = new Module();
    public static Module getInstance(){
        return module;
    }
    private Module(){}

    /**
     * 全部函数定义
     */
    public final ArrayList<Function> functions = new ArrayList<>();
    /**
     * 全部全局变量定义
     */
    public final ArrayList<GlobalVariable> globalVariables = new ArrayList<>();

    /**
     * 添加新的函数定义
     * @param function
     */
    public void addFunction(Function function){
        functions.add(function);
    }
    /**
     * 添加新的全局变量定义
     * @param globalVariable
     */
    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariables.add(globalVariable);
    }

    /**
     * 生成中间代码的字符串
     * @return
     */
    public String getIrString(){
        StringBuilder stringBuilder = new StringBuilder();


        return stringBuilder.toString();
    }
}
