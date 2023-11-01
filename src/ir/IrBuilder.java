package ir;

import ir.values.Module;

/**
 * @Description 中间代码生成的控制器类
 * @Author
 * @Date 2023/10/28
 **/
public class IrBuilder {

    // 全局单实例
    private static final IrBuilder irBuilder= new IrBuilder();
    public static IrBuilder getIrBuilder(){
        return irBuilder;
    }
    private IrBuilder(){}

    // module
    public static final Module module = Module.getInstance();


}
