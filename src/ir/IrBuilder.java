package ir;

import ir.types.ValueType;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.*;
import node.CompUnitNode;
import utils.IO;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 中间代码生成的控制器类
 * @Author
 * @Date 2023/10/28
 **/
public class IrBuilder {

    //==================构建器===================
    private final CompUnitNode compUnitNode;
    public IrBuilder(CompUnitNode compUnitNode){
        this.compUnitNode = compUnitNode;
    }
    public void doIrBuilding(){
        compUnitNode.buildIr();
    }
    public void outputIr(){
        IO.write(IO.IOType.IR_BUILDER, Module.getInstance().toString(), false, false);
    }

    //===================中间代码生成方法=================
    /**
     * 命名计数器
     */
    private static int nameCnt = 0;
    private static String getNameString(){
        return "" + nameCnt++;
    }
    /**
     * 初始化命名计数器
     */
    public static void resetNameCnt(){
        nameCnt = 0;
    }
    /**
     * 格式化字符串常量命名计数器
     */
    private static int formatStringNameCnt = 0;
    private static String getFormatStringNameString(){
        return "FORMAT_STRING_" + formatStringNameCnt++;
    }

    //===================工厂模式方法===================
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
        resetNameCnt(); // 刷新命名计数器
        Function function = new Function(name, returnType, argTypes, isLibFunc);
        Module.addFunction(function);
        IrSymbolTableStack.addSymbolToGlobal(name, function);
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
     * 不带const初值
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
     * 在函数体头部创建内存分配指令
     * 带const初值
     * @param pointingType  要存储的类型
     * @param parent        所在基本块
     * @param constArray    常量初值
     * @return
     */
    public static Alloca buildAllocaInstruction(ValueType pointingType, BasicBlock parent, ConstArray constArray){
        // 内存分配全部放在头部
        BasicBlock funcHeadBlock = IrTool.getHeadBlockOfParentFunction(parent);
        Alloca alloca = new Alloca(getNameString(), pointingType, funcHeadBlock, constArray);
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
     * 已知op为i32类型，构建从i1到i32的强制类型转换指令
     * @param op        要转换的i1类型value
     * @return          转换指令（valueType为IntType(i32)）
     */
    public static Value buildZextInstruction(Value op, BasicBlock parent){
        Zext zext = new Zext(getNameString(), parent, op);
        parent.addInstruction(zext);
        return zext;
    }
    /**
     * 如果op是i1类型的Value，那么构建从i1到i32的强制类型转换指令
     * 否则原样返回
     * @param op        可能要转换的i1类型value
     * @return          转换指令（valueType为IntType(i32)） 或原样返回
     */
    public static Value buildZextInstructionIfI1(Value op, BasicBlock parent){
        if(op.getType().isI1()){
            return buildZextInstruction(op, parent);
        }
        return op;
    }

    /**
     * 构建函数调用指令
     * @param function  被调用的函数
     * @param rArgs     实参列表
     */
    public static Call buildCallInstruction(Function function, ArrayList<Value> rArgs, BasicBlock parent){
        Call call = new Call(getNameString(), parent, function, rArgs);
        parent.addInstruction(call);
        return call;
    }

    /**
     * 构建加载指令
     * @param pointer   要加载的地址，从这个地址处读取操作数
     * @return          完成加载的Value
     */
    public static Load buildLoadInstruction(Value pointer, BasicBlock parent){
        Load load = new Load(getNameString(), parent, pointer);
        parent.addInstruction(load);
        return load;
    }

    /**
     * 构建用于降低一维的gep指令
     * @param ptrval    基地址
     */
    public static GetElementPtr buildRankDownInstruction(Value ptrval, BasicBlock parent){
        return buildGetElementPtrInstruction(ptrval, ConstInt.ZERO(), ConstInt.ZERO(), parent);
    }
    /**
     * 构建带有二维寻址的gep指令
     * 返回的指针将会降一级
     * @param ptrval    基地址
     * @param index1    本维寻址
     * @param index2    第一维寻址
     */
    public static GetElementPtr buildGetElementPtrInstruction(Value ptrval, Value index1, Value index2, BasicBlock parent){
        GetElementPtr getElementPtr = new GetElementPtr(getNameString(), parent, ptrval, index1, index2);
        parent.addInstruction(getElementPtr);
        return getElementPtr;
    }

    /**
     * 构建带有本维寻址的gep指令
     * 返回的指针是同级的，即向前挪动index
     * @param ptrval    基地址
     * @param index    本维寻址
     */
    public static GetElementPtr buildGetElementPtrInstruction(Value ptrval, Value index, BasicBlock parent){
        GetElementPtr getElementPtr = new GetElementPtr(getNameString(), parent, ptrval, index);
        parent.addInstruction(getElementPtr);
//        System.out.println(getElementPtr);
        return getElementPtr;
    }

    /**
     * 带指定初始值的全局变量
     * @param name      变量标识符名
     * @param isConst   是否常量
     * @param initVal   初始值
     */
    public static void buildGlobalVariable(String name, Boolean isConst, Constant initVal){
        GlobalVariable globalVariable = new GlobalVariable(name, isConst, initVal);
        Module.addGlobalVariable(globalVariable);
        IrSymbolTableStack.addSymbolToGlobal(name, globalVariable);
    }

    public static GlobalVariable buildGlobalFormatString(String formatString){
        ArrayList<Constant> constCharArray = new ArrayList<>();
        for(int i = 0; i < formatString.length(); i++){
            constCharArray.add(new ConstInt(8, formatString.charAt(i)));
        }
        ConstArray initVal = new ConstArray(constCharArray);
        String name = getFormatStringNameString();
        GlobalVariable globalVariable = new GlobalVariable(name, true, initVal);
        Module.addGlobalVariable(globalVariable);
        IrSymbolTableStack.addSymbolToGlobal(name, globalVariable);
        return globalVariable;
    }

    /**
     * 构建GEP和store指令，将指定的flatten value array，存入局部数组
     * @param arrayPointer  目标数组指针
     * @param dims          数组维数信息
     * @param flattenArray  要存的value内容数组（展平）
     */
    public static void buildStoreWithValuesIntoArray(Alloca arrayPointer, ArrayList<Integer> dims, ArrayList<Value> flattenArray, BasicBlock parent){
        // 接下来获取一个指向底层元素的指针，挨个存入元素
        GetElementPtr basePtr = IrBuilder.buildRankDownInstruction(arrayPointer, parent);
        for(int i=1; i<dims.size(); i++){
            basePtr = IrBuilder.buildRankDownInstruction(basePtr, parent);
        }

        // 遍历展平之后的数组
        // 依次将数组内的元素使用store进行存储，存储位置为base + i
        GetElementPtr elementPtr = basePtr;
        IrBuilder.buildStoreInstruction(flattenArray.get(0), elementPtr, parent);
        for(int i=1; i < flattenArray.size(); i++){
            // p = base + i
            elementPtr = IrBuilder.buildGetElementPtrInstruction(basePtr, new ConstInt(32, i), parent);
            IrBuilder.buildStoreInstruction(flattenArray.get(i), elementPtr, parent);
        }
    }

    /**
     * 带条件跳转指令
     * @param condition     跳转条件
     */
    public static void buildBrInstruction(Value condition, BasicBlock trueBranch, BasicBlock falseBranch, BasicBlock parent){
        Br br = new Br(parent, condition, trueBranch, falseBranch);
        parent.addInstruction(br);
    }
    /**
     * 无条件跳转指令
     */
    public static void buildBrInstruction(BasicBlock target, BasicBlock parent){
        Br br = new Br(parent, target);
        parent.addInstruction(br);
    }
}
