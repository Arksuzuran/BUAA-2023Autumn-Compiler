package symbol;

import node.Node;

import java.util.Objects;
import java.util.Stack;

/**
 * @Description 栈式符号表
 * @Author
 * @Date 2023/10/13
 **/
public class SymbolTableStack {
    private final Stack<SymbolTable> stack;   //

    // 单例
    private static SymbolTableStack instance = new SymbolTableStack();
    public static SymbolTableStack getInstance(){
        return instance;
    }
    private SymbolTableStack(){
        this.stack = new Stack<>();
    }

    // ====================== 栈操作=======================
    // 创建新的符号表并入栈
    public static void push(SymbolTable symbolTable){
        // 栈非空，则栈顶元素应当记录当前进入的符号表为son
        if(!instance.stack.empty()){
            peek().addSon(symbolTable);
        }
        instance.stack.push(symbolTable);
    }
    // 创建新的node对应的符号表并入栈
    public static void push(Node node){
        instance.stack.push(new SymbolTable(peek(), node));
    }
    // 将栈顶符号表出栈
    public static void pop(){
        instance.stack.pop();
    }
    // 访问栈顶元素 如果栈为空那么返回null
    public static SymbolTable peek(){
        if(instance.stack.empty()){
            return null;
        }
        return instance.stack.peek();
    }
    // 向栈顶符号表中添加元素
    public static void addSymbolToPeek(Symbol symbol){
        peek().addSymbol(symbol);
    }

    // ================= 栈查找 =================
    // 检测栈顶符号表是否包含指定名称的元素
    public static boolean peekHasSymbol(String name){
        return peek().hasSymbol(name);
    }
    // 检查整个栈内是否包含指定名称的元素
    public static boolean stackHasSymbol(String name){
        for(SymbolTable symbolTable : instance.stack){
            if(symbolTable.hasSymbol(name)){
                return true;
            }
        }
        return false;
    }
    // 检查整个栈内是否包含指定名称指定类型的元素
    public static boolean stackHasSymbol(String name, SymbolType symbolType){
        for(SymbolTable symbolTable : instance.stack){
            if(symbolTable.hasSymbol(name, symbolType)){
                return true;
            }
        }
        return false;
    }
    // 在整个栈内查找并返回第一个指定名称指定类型的元素
    public static Symbol getSymbol(String name, SymbolType symbolType){
        Symbol symbol = null;
        for (SymbolTable symbolTable : instance.stack){
            symbol = symbolTable.getSymbol(name, symbolType);
            if(symbol != null){
                return symbol;
            }
        }
        return null;
    }


    // 记录当前所在的循环深度
    private int circleDepth = 0;

    // 进入循环相关
    public static boolean inLoop() {
        return instance.circleDepth > 0;
    }
    public static void enterLoop(boolean into) {
        if(into){
            instance.circleDepth++;
        } else {
            instance.circleDepth = instance.circleDepth > 0 ? instance.circleDepth - 1 : 0;
        }
    }

    // 记录当前是否在一个无返回值的函数内部
    private boolean inVoidFunc = false;
    // 进入函数定义相关
    public static boolean inVoidFunc() {
        return instance.inVoidFunc;
    }
    public static void setInVoidFunc(boolean inVoidFunc) {
        instance.inVoidFunc = inVoidFunc;
    }

}
