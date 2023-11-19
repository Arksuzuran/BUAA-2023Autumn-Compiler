package ir.values;

import backend.Mc;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import backend.parts.MipsModule;
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

    public static ArrayList<Function> getFunctions() {
        return module.functions;
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

    @Override
    public void buildMips(){
        for(GlobalVariable globalVariable : globalVariables){
            globalVariable.buildMips();
        }
        mapFunctionBlockIrToMips();
        for (Function function : functions){
            function.buildMips();
        }
    }

    /**
     * 将中间代码的函数和基本块对象:
     * 1.构建mips里的相应对象
     * 2.加入Module
     * 3.信息存储到mips对象里
     */
    private void mapFunctionBlockIrToMips(){
        // 遍历所有函数
        for (Function irFunction : functions){
            // 构建函数对象
            MipsFunction mipsFunction = new MipsFunction(irFunction.getName(), irFunction.isLibFunc());
            Mc.addFunctionMapping(irFunction, mipsFunction);
            MipsModule.addFunction(mipsFunction);

            // 构建基本块对象
            ArrayList<BasicBlock> blocks = irFunction.getBasicBlocks();
            for (BasicBlock irBlock : blocks){
                MipsBlock mipsBlock = new MipsBlock(irBlock.getName(), irBlock.getLoopDepth());
                Mc.addBlockMapping(irBlock, mipsBlock);
            }
            // 记录mipsBlock的前驱块信息, 前驱块当然也是mipsBlock
            for (BasicBlock irBlock : blocks){
                MipsBlock mipsBlock = Mc.b(irBlock);
                for(BasicBlock irPreBlock : irBlock.getPreBlocks()){
                    mipsBlock.addPreBlock(Mc.b(irPreBlock));
                }
            }
        }
    }

}
