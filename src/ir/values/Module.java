package ir.values;

import ir.types.VoidType;

import java.util.ArrayList;

/**
 * @Description 整个Ir树的根，其下挂载有function，globalVariable
 * @Author
 * @Date 2023/10/30
 **/
public class Module extends Value{
    // 单例模式
    private static final Module module = new Module();
    public static Module getInstance(){
        return module;
    }
    private Module(){
        super("Module", new VoidType(), null);
    }

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
    public static void addFunction(Function function){
        module.functions.add(function);
    }
    /**
     * 添加新的全局变量定义
     * @param globalVariable
     */
    public static void addGlobalVariable(GlobalVariable globalVariable){
        module.globalVariables.add(globalVariable);
    }

    /**
     * 生成中间代码的字符串
     * @return
     */
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(!globalVariables.isEmpty()){
            for (GlobalVariable globalVariable : globalVariables){
                stringBuilder.append(globalVariable);
            }
            stringBuilder.append("\n");
        }
        if(!functions.isEmpty()){
            for (Function function : functions){
                stringBuilder.append(function);
            }
        }
        return stringBuilder.toString();
    }
}
