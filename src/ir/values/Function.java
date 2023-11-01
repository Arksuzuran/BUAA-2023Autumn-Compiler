package ir.values;

import ir.IrSymbolTable;
import ir.types.ValueType;

import java.util.ArrayList;
import java.util.Date;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class Function extends Value{
    public ArrayList<Value> getArgs() {
        return args;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    /**
     * 形参列表
     */
    private final ArrayList<Value> args = new ArrayList<>();
    /**
     * 下属基本块
     */
    private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    /**
     * 函数返回类型
     */
    private final ValueType returnType;
    /**
     * 下属符号表
     */
    public IrSymbolTable symbolTable;

    /**
     *
     * @param name  函数名
     * @param returnType    函数返回类型
     * @param symbolTable   函数对应的符号表，理应新建好
     * @param valueTypes    函数参数的类型
     */
    public Function(String name, ValueType returnType, IrSymbolTable symbolTable, ValueType... valueTypes) {
        super("@" + name, returnType, null);
        this.returnType = returnType;
        this.symbolTable = symbolTable;
        for(int i = 0; i < valueTypes.length; i++){
            Value arg = new Value("arg" + i, valueTypes[i], this);
            args.add(arg);
            symbolTable.addSymbol(arg.getName(), arg);
        }
    }

    /**
     * 向符号表中添加符号
     * @param value 要添加的符号
     */
    public void addSymbol(Value value){
        symbolTable.addSymbol(value.getName(), value);
    }
    /**
     * 向基本块列表的尾部添加基本块
     */
    public void addBlock(BasicBlock block){
        basicBlocks.add(block);
    }
}
