package backend.parts;

import ir.values.GlobalVariable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsModule {
    private MipsModule(){
    }
    public static MipsModule instance = new MipsModule();
    public static MipsModule getInstance(){
        return instance;
    }
    private static MipsFunction mainFunction;

    public ArrayList<MipsFunction> getFunctions() {
        return functions;
    }

    private final ArrayList<MipsFunction> functions = new ArrayList<>();
    private final ArrayList<MipsGlobalVariable> globalVariables = new ArrayList<>();

    public static void addGlobalVariable(MipsGlobalVariable globalVariable){
        instance.globalVariables.add(globalVariable);
    }

    public static void addFunction(MipsFunction mipsFunction){
        if(Objects.equals(mipsFunction.getName(), "main")){
            mainFunction = mipsFunction;
        }
        instance.functions.add(mipsFunction);
    }

    /**
     * 生成MIPS汇编代码
     * 数据段各个全局变量
     * 跳转到 main 的语句和结束（_start）
     * 两个内建函数的打印
     * 非内建函数打印
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# HIKARI 2023/11/22 \n");
        // 宏定义
        // putstr
        sb.append(".macro putstr\n");
        sb.append("\tli\t$v0,\t4\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n\n");
        // putint
        sb.append(".macro putint\n");
        sb.append("\tli\t$v0,\t1\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n\n");
        // getint
        sb.append(".macro getint\n");
        sb.append("\tli\t$v0,\t5\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n\n");

        // 数据段
        sb.append(".data\n");
        // int 及 int 数组
        // .word
        for(MipsGlobalVariable globalVariable : globalVariables){
            if (globalVariable.getType() == MipsGlobalVariable.GVType.Int){
                sb.append(globalVariable).append("\n");
            }
        }
        // 未初始化的变量
        // .space
        for(MipsGlobalVariable globalVariable : globalVariables){
            if (globalVariable.getType() == MipsGlobalVariable.GVType.Zero){
                sb.append(globalVariable).append("\n");
            }
        }
        // 字符串
        // .ascizz
        for(MipsGlobalVariable globalVariable : globalVariables){
            if (globalVariable.getType() == MipsGlobalVariable.GVType.String){
                sb.append(globalVariable).append("\n");
            }
        }

        // 代码段
        sb.append(".text\n");
        // 打印函数
        // 首先打印主函数
        sb.append(mainFunction);
        // 打印其他函数
        for(MipsFunction function : functions){
            if (!function.isLibFunc() && function != mainFunction){
                sb.append(function).append("\n");
            }
        }
        return sb.toString();
    }
}
