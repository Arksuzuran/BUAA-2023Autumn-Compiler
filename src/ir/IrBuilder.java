package ir;

import ir.types.FunctionType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.Value;
import ir.values.instructions.*;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 中间代码生成的控制器类
 * @Author
 * @Date 2023/10/28
 **/
public class IrBuilder {

    // 全局单实例
    private static final IrBuilder irBuilder= new IrBuilder();
    public static IrBuilder getIrBuilder(){
        return irBuilder;
    }
    private IrBuilder(){}

    // module
    public static final Module module = Module.getInstance();

    /**
     * 命名计数器
     */
    private static int nameCnt = 0;
    private static String getNameString(){
        return "" + nameCnt++;
    }
    // 工厂模式方法
    /**
     * 新建函数定义指令
     * 会将该函数加入Module的函数列表 以及全局符号表
     * @param name      函数标识符名
     * @param returnType    函数返回类型
     * @param argTypes      函数参数ValueType的列表
     * @param isLibFunc     是否是链接进来的库函数
     * @return          新建的Function对象
     */
    public static Function buildFunction(String name, ValueType returnType, ArrayList<ValueType> argTypes, boolean isLibFunc){
        Function function = new Function(name, returnType, argTypes, isLibFunc);
        Module.addFunction(function);
        IrSymbolTableStack.globalSymbolTable.addSymbol(name, function);
        return function;
    }

    /**
     * 创建函数的下属基本块
     * 会将该基本块加入函数的基本块列表
     * @param function  所属的函数
     * @return          创建的新基本块
     */
    public static BasicBlock buildBasicBlock(Function function){
        BasicBlock basicBlock = new BasicBlock(getNameString(), function);
        function.addBlock(basicBlock);
        return basicBlock;
    }

    /**
     * 创建返回指令
     * 会将该指令加入基本块
     * @param parent        所属基本块
     * @param returnValue   返回值Value 为空则返回void
     */
    public static void buildRetInstruction(BasicBlock parent, Value returnValue){
        // int返回值
        if(returnValue != null){
            parent.addInstruction(new Ret(parent, returnValue));
        }
        // void返回值
        else{
            parent.addInstruction(new Ret(parent));
        }
    }

    /**
     * 在函数体头部创建内存分配指令
     * @param pointingType  要存储的类型
     * @param parent        所在基本块
     * @return
     */
    public static Alloca buildAllocaInstruction(ValueType pointingType, BasicBlock parent){
        // 内存分配全部放在头部
        BasicBlock funcHeadBlock = IrTool.getHeadBlockOfParentFunction(parent);
        Alloca alloca = new Alloca(getNameString(), pointingType, funcHeadBlock);
        // 要插入头部基本块的最前面，因为头部基本块后面可能有顺序执行的非内存分配指令
        funcHeadBlock.addInstructionAtHead(alloca);
        return alloca;
    }

    /**
     * 创建store指令
     * @param value     要存储的内容
     * @param pointer   要存入的地址
     * @param parent    基本块
     */
    public static void buildStoreInstruction(Value value, Value pointer, BasicBlock parent){
        Store store = new Store(getNameString(), parent, value, pointer);
        parent.addInstruction(store);
    }

    /**
     * 创建加法指令
     * @param op1   加数1
     * @param op2   加数2
     * @param parent    基本块
     * @return      创建好的指令
     */
    public static Add buildAddInstruction(Value op1, Value op2, BasicBlock parent) {
        Add add = new Add(getNameString(), parent, op1, op2);
        parent.addInstruction(add);
        return add;
    }

    /**
     * 创建减法指令
     * @param op1   被减数
     * @param op2   减数
     * @param parent    基本块
     * @return      创建好的指令
     */
    public static Sub buildSubInstruction(Value op1, Value op2, BasicBlock parent) {
        Sub sub = new Sub(getNameString(), parent, op1, op2);
        parent.addInstruction(sub);
        return sub;
    }

    /**
     * 构建乘法指令
     */
    public static Mul buildMulInstruction(Value op1, Value op2, BasicBlock parent) {
        Mul mul = new Mul(getNameString(), parent, op1, op2);
        parent.addInstruction(mul);
        return mul;
    }

    /**
     *  构建除法指令
     */
    public static Sdiv buildSdivInstruction(Value op1, Value op2, BasicBlock parent) {
        Sdiv sdiv = new Sdiv(getNameString(), parent, op1, op2);
        parent.addInstruction(sdiv);
        return sdiv;
    }
    /**
     *  构建取余指令
     */
    public static Srem buildSremInstruction(Value op1, Value op2, BasicBlock parent) {
        Srem srem = new Srem(getNameString(), parent, op1, op2);
        parent.addInstruction(srem);
        return srem;
    }

    /**
     * 构建比较指令
     * @param type  比较的类型
     * @return      比较指令（ValueType为IntType(i1)）
     */
    public static Icmp buildIcmpInstruction(Value op1, Value op2, Icmp.CondType type, BasicBlock parent){
        Icmp icmp = new Icmp(getNameString(), parent, type, op1, op2);
        parent.addInstruction(icmp);
        return icmp;
    }

    /**
     * 构建从i1到i32的强制类型转换指令
     * @param op        要转换的i1类型value
     * @return          转换指令（valueType为IntType(i32)）
     */
    public static Zext buildZextInstruction(Value op, BasicBlock parent){
        Zext zext = new Zext(getNameString(), parent, op);
        parent.addInstruction(zext);
        return zext;
    }

}
