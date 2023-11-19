package ir.values;

import backend.Mc;
import ir.IrSymbolTable;
import ir.analyze.Loop;
import ir.types.ValueType;
import utils.IrTool;

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
    public void setBasicBlocks(ArrayList<BasicBlock> newBlocks) {
        basicBlocks.clear();
        basicBlocks.addAll(newBlocks);
    }
    public BasicBlock getEntryBlock(){
        return basicBlocks.get(0);
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

    public boolean isLibFunc() {
        return isLibFunc;
    }

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
            addArgByValueType(argTypes.get(i), i);
        }
    }

    /**
     * 给函数添加新参数
     * 会将新参数加入符号表 构建对应Value并加入参数表
     * @param valueType 参数的类型
     */
    public void addArgByValueType(ValueType valueType, int argNumber){
        Value arg = new Value("%arg" + argValues.size(), valueType, this, argNumber);
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

    // define dso_local i32 @a2(i32 %0, i32* %1) {
    //    %3 = alloca i32*
    //    ret i32 %9
    //}
    // declare void @putint(i32)
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        // 头部
        if(!isLibFunc){
            stringBuilder.append("define dso_local ");
        } else {
            stringBuilder.append("declare ");
        }
        stringBuilder.append(getReturnType())
                .append(" ")
                .append(getName());

        // 非库函数：完整的函数参数列表以及函数主体
        if(!isLibFunc){
            // 参数列表
            stringBuilder.append("(");
            IrTool.appendSBParamList(stringBuilder, argValues);
            stringBuilder.append(")");
            // 函数主体
            stringBuilder.append(" {\n");
            for (BasicBlock block : basicBlocks){
                stringBuilder.append(block);
            }
            stringBuilder.append("}");
        }
        // 库函数：仅有带类型的参数列表
        else{
            stringBuilder.append("(");
            for(Value arg : argValues){
                stringBuilder.append(arg.getType()).append(", ");
            }
            IrTool.cutSBTailComma(stringBuilder);
            stringBuilder.append(")");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }



    // ========== 循环分析 ==========
    /**
     * 函数内的所有Loop
     */
    private ArrayList<Loop> loops;
    /**
     * 函数内深度为1的loop
     */
    private ArrayList<Loop> loopsAtTop;
    public ArrayList<Loop> getLoops() {
        return loops;
    }

    public void setLoops(ArrayList<Loop> loops) {
        this.loops = loops;
    }

    public ArrayList<Loop> getLoopsAtTop() {
        return loopsAtTop;
    }

    public void setLoopsAtTop(ArrayList<Loop> loopsAtTop) {
        this.loopsAtTop = loopsAtTop;
    }

    // ========== 中间代码生成 ==========
    @Override
    public void buildMips(){
        // 非内建函数才需要解析
        if(!isLibFunc){
            Mc.curIrFunction = this;
            for(BasicBlock basicBlock : basicBlocks){
                basicBlock.buildMips();
            }
//            parsePhis(irFunction);
//            fMap.get(irFunction).blockSerial(bMap.get(irFunction.getBasicBlocks().getHead().getVal()), phiCopysLists);
        }
    }
}
