package backend;

import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.GlobalVariable;
import ir.values.Value;
import utils.Pair;
import utils.Triple;

import java.util.HashMap;
import java.util.HashSet;

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

    /**
     * 记录在块block内， op1 / op2的结果op3
     */
    private static final HashMap<Triple<MipsBlock, MipsOperand, MipsOperand>, MipsOperand> divMap = new HashMap<>();
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
    public static void addDivMapping(MipsBlock mipsBlock, MipsOperand op1, MipsOperand op2, MipsOperand result){
        divMap.put(new Triple<>(mipsBlock, op1, op2), result);
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

    /**
     * 获取ir Value对象 对应的 mipsOperand对象
     * @param irValue   ir Value对象
     * @return          mipsOperand对象
     */
    public static MipsOperand op(Value irValue){
        return opMap.get(irValue);
    }

    /**
     * 查询在mipsBlock内，op1/op2 是否已有计算结果、
     * @return          计算结果的mipsOperand对象
     */
    public static MipsOperand div(MipsBlock mipsBlock, MipsOperand op1, MipsOperand op2){
        return divMap.get(new Triple<>(mipsBlock, op1, op2));
    }
}
