package ir;

import ir.values.Value;
import java.util.Stack;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class IrSymbolTableStack {
    private final Stack<IrSymbolTable> stack = new Stack<>();   //

    // 单例
    private static IrSymbolTableStack instance = new IrSymbolTableStack();
    public static IrSymbolTableStack getInstance(){
        return instance;
    }

    // ====================== 栈操作=======================
    // 创建新的符号表并入栈
    public static void push(IrSymbolTable symbolTable){
        instance.stack.push(symbolTable);
    }
    // 将栈顶符号表出栈
    public static void pop(){
        instance.stack.pop();
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
}
