package ir;

import ir.values.Value;
import symbol.Symbol;

import java.util.Stack;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class IrSymbolTableStack {
    // 单例
    private static IrSymbolTableStack instance = new IrSymbolTableStack();
    public static IrSymbolTableStack getInstance(){
        return instance;
    }
    /**
     * 符号表栈
     */
    private final Stack<IrSymbolTable> stack = new Stack<>();
    /**
     * 全局符号表
     */
    public static IrSymbolTable globalSymbolTable;

    // ====================== 栈操作=======================

    /**
     * 初始化符号表栈
     * 清空符号表栈，并把全局符号表入栈
     */
    public static void init(){
        instance.stack.clear();
        globalSymbolTable = new IrSymbolTable();
        instance.stack.push(globalSymbolTable);
    }
    /**
     * 入栈一个给定的符号表
     * @param symbolTable   给定的符号表
     */
    public static void push(IrSymbolTable symbolTable){
        instance.stack.push(symbolTable);
    }
    /**
     * 创建一个新的符号表并入栈
     */
    public static IrSymbolTable push(){
        IrSymbolTable irSymbolTable = new IrSymbolTable();
        push(irSymbolTable);
        return irSymbolTable;
    }

    /**
     * 将栈顶符号表出栈
     * 全局根符号表并不会出栈
     */
    public static void pop(){
        if(instance.stack.size() > 1){
            instance.stack.pop();
        }
    }
    // 访问栈顶元素(符号表) 如果栈为空那么返回null
    public static IrSymbolTable peek(){
        if(instance.stack.empty()){
            return null;
        }
        return instance.stack.peek();
    }
    // 向栈顶符号表中添加元素
    public static void addSymbolToPeek(String name, Value value){
        peek().addSymbol(name, value);
    }
    // 向全局符号表中添加元素
    public static void addSymbolToGlobal(String name, Value value){
        globalSymbolTable.addSymbol(name, value);
    }
    // ================= 栈查找 =================
    // 检测栈顶符号表是否包含指定名称的元素
    public static boolean peekHasSymbol(String name){
        return peek().hasSymbol(name);
    }
    // 检查整个栈内是否包含指定名称的元素
    public static boolean stackHasSymbol(String name){
        for(IrSymbolTable symbolTable : instance.stack){
            if(symbolTable.hasSymbol(name)){
                return true;
            }
        }
        return false;
    }
    /**
     * 寻找指定name的符号，返回其Value
     * @param name  名称
     * @return      对应Value
     */
    public static Value getSymbol(String name){
        for(int i=instance.stack.size()-1; i>=0; i--){
            Value value = instance.stack.get(i).getSymbol(name);
            if(value != null){
                return value;
            }
        }
        return null;
    }
    /**
     * 判断当前是否在构建全局符号表
     * @return
     */
    public static boolean isBuildingGlobalSymbolTable(){
        return instance.stack.size() == 1;
    }
}
