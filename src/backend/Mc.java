package backend;

import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;

import java.util.HashMap;

/**
 * @Description MipsBuilding Context
 * @Author  HIKARI
 * @Date 2023/11/17
 **/
public class Mc {
    private static HashMap<Function, MipsFunction> functionMap = new HashMap<>();
    private static HashMap<BasicBlock, MipsBlock> blockMap = new HashMap<>();

    /**
     * 添加映射：irFunction - mipsFunction
     */
    public static void addFunctionMapping(Function irFunction, MipsFunction mipsFunction){
        functionMap.put(irFunction, mipsFunction);
    }
    /**
     * 添加映射：irFunction - mipsFunction
     */
    public static void addBlockMapping(BasicBlock irBlock, MipsBlock mipsBlock){
        blockMap.put(irBlock, mipsBlock);
    }

    /**
     * 获取ir函数对象 对应的 mips函数对象
     * @param irFunction    ir函数对象
     * @return              mips函数对象
     */
    public static MipsFunction f(Function irFunction){
        return functionMap.get(irFunction);
    }
    /**
     * 获取ir基本块对象 对应的 mips基本块对象
     * @param irBlock   ir基本块对象
     * @return          mips基本块对象
     */
    public static MipsBlock b(BasicBlock irBlock){
        return blockMap.get(irBlock);
    }
}
