package backend;

import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.GlobalVariable;
import ir.values.Value;
import utils.Pair;

import java.util.HashMap;

/**
 * @Description MipsBuilding Context
 * @Author  HIKARI
 * @Date 2023/11/17
 **/
public class Mc {
    /**
     * 当前正在解析的irFunction
     */
    public static Function curIrFunction = null;

    private static HashMap<Function, MipsFunction> functionMap = new HashMap<>();
    private static HashMap<BasicBlock, MipsBlock> blockMap = new HashMap<>();
    private static HashMap<Value, MipsOperand> opMap = new HashMap<>();
//    private static HashMap<Pair<GlobalVariable, BasicBlock>, MipsOperand> globalVariableMap = new HashMap<>();
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
     * 添加映射：irValue - mipsValue
     * 会记录instr 的目的寄存器，arg 参数
     * 但是不记录 imm，label
     */
    public static void addOperandMapping(Value irValue, MipsOperand mipsOperand){
        opMap.put(irValue, mipsOperand);
    }
//    public static void addGlobalVariableMapping(GlobalVariable globalVariable, BasicBlock block, MipsOperand mipsOperand){
//        globalVariableMap.put(new Pair<>(globalVariable, block), mipsOperand);
//    }
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

    /**
     * 获取ir Value对象 对应的 mipsOperand对象
     * @param irValue   ir Value对象
     * @return          mipsOperand对象
     */
    public static MipsOperand op(Value irValue){
        return opMap.get(irValue);
    }
//    public static MipsOperand gv(GlobalVariable globalVariable, BasicBlock block){
//        return opMap.get(new Pair<>(globalVariable, block));
//    }
}
