package ir.values;

import ir.IrSymbolTable;
import ir.types.ValueType;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class Function extends Value{
    public ArrayList<Value> getArgValues() {
        return argValues;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ValueType getReturnType() {
        return returnType;
    }
    public IrSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(IrSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    public static Function putstr = null;   // declare void @putstr(i8*)
    public static Function putint = null;   // declare void @putint(i32)
    public static Function putch = null;   // declare void @putch(i32)
    public static Function getint = null;   // declare i32 @getint()
    /**
     * 是否是链接来的库函数
     */
    private boolean isLibFunc = false;
    /**
     * 形参列表
     */
    private final ArrayList<Value> argValues = new ArrayList<>();
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
    private IrSymbolTable symbolTable;

    /**
     *
     * @param name  函数名
     * @param returnType    函数返回类型
     * @param argTypes    函数参数的类型
     */
    public Function(String name, ValueType returnType, ArrayList<ValueType> argTypes, Boolean isLibFunc) {
        // 全局符号前+@
        super("@" + name, returnType, Module.getInstance());
        this.returnType = returnType;
        this.isLibFunc = isLibFunc;
        for(int i = 0; i < argTypes.size(); i++){
            addArgByValueType(argTypes.get(i));
        }
    }

    /**
     * 给函数添加新参数
     * 会将新参数加入符号表 构建对应Value并加入参数表
     * @param valueType 参数的类型
     */
    public void addArgByValueType(ValueType valueType){
        Value arg = new Value("arg" + argValues.size(), valueType, this);
        argValues.add(arg);
//        addSymbol(arg);
    }
    /**
     * 向符号表中添加符号
     * @param value 要添加的符号
     */
    public void addSymbol(String name, Value value){
        symbolTable.addSymbol(name, value);
    }
    /**
     * 向基本块列表的尾部添加基本块
     */
    public void addBlock(BasicBlock block){
        basicBlocks.add(block);
    }

    /**
     * 获取函数头部的第一个基本块
     */
    public BasicBlock getHeadBlock(){
        return basicBlocks.get(0);
    }
}
