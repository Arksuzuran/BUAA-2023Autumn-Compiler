package node;

import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.Irc;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.GlobalVariable;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import ir.values.instructions.Alloca;
import symbol.*;
import token.Token;
import utils.ErrorCheckTool;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 左值表达式 LVal → Ident {'[' Exp ']'}
 * @Author
 * @Date 2023/9/20
 **/
public class LValNode extends Node{
    public Token getIdentToken() {
        return identToken;
    }

    private Token identToken;
    private ArrayList<Token> lbrackTokens;
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> rbrackTokens;

    public LValNode(Token identToken, ArrayList<Token> lbrackTokens, ArrayList<ExpNode> expNodes, ArrayList<Token> rbrackTokens) {
        super(NodeType.LVal);
        this.identToken = identToken;
        this.lbrackTokens = lbrackTokens;
        this.expNodes = expNodes;
        this.rbrackTokens = rbrackTokens;
    }

    @Override
    public void print(){
        identToken.print();
        if(!lbrackTokens.isEmpty()){
            for(int i=0; i<lbrackTokens.size(); i++){
                lbrackTokens.get(i).print();
                expNodes.get(i).print();
                rbrackTokens.get(i).print();
            }
        }
        printNodeType();
    }

    // 左值表达式   LVal → Ident {'[' Exp ']'} // c k
    @Override
    public void check() {
        // 检查未定义的名称
        ErrorCheckTool.judgeAndHandleUndefinedError(identToken);
        for(ExpNode expNode : expNodes){
            expNode.check();
        }
    }

    /**
     * LVal一般返回指针类型的value，该指针是所求变量的地址。
     * 让上层PrimaryExp来判断是否进行加载
     * 如果isBuildingConstExp，那么一定返回synInt
     * 对于函数实参，其降维操作在此执行
     * @synInt      返回左值的ConstInt值. 前提：isBuildingConstExp.
     * @synValue    返回存储左值内容的指针（地址）
     */
    @Override
    public void buildIr() {
        // 查符号表获得左值对应的value
        Value lvalValue = IrSymbolTableStack.getSymbol(identToken.str);
        assert lvalValue != null;

        // 左值为Int类型 无需再进行取值
        if(lvalValue.getType() instanceof IntType){
            if(Irc.isBuildingConstExp){
                Irc.synInt = ((ConstInt) lvalValue).getValue();
            } else {
                Irc.synValue = lvalValue;
            }
        }

        // 左值为Pointer类型 需要进一步取值
        else{
            // 根据指针指向的类型，进行讨论
            ValueType valueType = IrTool.getPointingTypeOfPointer(lvalValue);

            // 指向int类型
            if(valueType instanceof IntType){
                // 需要直接提取计算出常数 且为全局常量指针 则将计算出的常数使用synInt传递
                if(Irc.isBuildingConstExp && lvalValue instanceof GlobalVariable){
                    ConstInt initValue = (ConstInt) (((GlobalVariable) lvalValue).getInitValue());
                    Irc.synInt = initValue.getValue();
                }
                // 否则直接向上传递指针，这一般是变量存储的地址
                else{
                    Irc.synValue = lvalValue;
                }
            }
            // 指向指针
            // 该pointer一定是当前左值所处函数的 [数组形参]
            // 例如f(int a[], int b[][2])中的 a和b
            // 因此一定不在buildingConstExp
            // lvalValue可能的类型有：
            // 一维数组i32* *
            // 二维数组[2 * i32]* *
            // 因为只有形参在满足SSA的时候 会通过alloca和store 来在本来的指针上多附加一层指针 以存储形参指针的值
            // 返回指针
            else if(valueType instanceof PointerType){
                // 取出该指针指向空间的内容
                // 即复原出形参（包括内容和类型）
                // i32*，[2 * i32]*
                Value fParamValue = IrBuilder.buildLoadInstruction(lvalValue, Irc.curBlock);
                // 没有[]，则直接原封不动传回，因为这就是形参
                if(expNodes.isEmpty()){
                    Irc.synValue = fParamValue;
                }
                // 一级[]，根据形参本身是一维还是二维，分为两种情况
                else if(expNodes.size() == 1){
                    expNodes.get(0).buildIr();
                    Value indexValue = Irc.synValue;
                    // 根据index 向前挪动指针的值
                    Value ptrval = IrBuilder.buildGetElementPtrInstruction(fParamValue, indexValue, Irc.curBlock);

                    // 如果形参指向的是数组，那么说明形参是二维的。例如int a[][2] => [2 * i32]*，则形参指向[2* i32]
                    // 但这里只取了一维，也就是说希望传入a[1]
                    // 一定是作为函数的实参！
                    // 那么函数实参传入的类型应当是i32*
                    // 所以应当向下降维
                    if(IrTool.getPointingTypeOfPointer(fParamValue) instanceof ArrayType){
                        ptrval = IrBuilder.buildRankDownInstruction(ptrval, Irc.curBlock);
                    }
                    Irc.synValue = ptrval;
                }
                // 二级[]，只可能是对于二维数组形参int a[][2]的取值，结果应该为int类型
                else{
                    expNodes.get(0).buildIr();
                    Value indexValue1 = Irc.synValue;
                    expNodes.get(1).buildIr();
                    Value indexValue2 = Irc.synValue;
                    Irc.synValue = IrBuilder.buildGetElementPtrInstruction(fParamValue, indexValue1, indexValue2, Irc.curBlock);
                }
            }
            // 指向数组
            // 则应该为正常的局部数组或者全局数组
            else if(valueType instanceof ArrayType){
                // 常量表达式 最后结果必然是ConstInt
                // 两种情况：全局常量数组 局部常量数组
                // 常量数组都已经存储在了对应的对象内，直接调取方法读取即可
                // 返回synInt
                if(Irc.isBuildingConstExp){
                    Constant initVal;
                    // 全局常量数组 是GlobalVariable形式
                    if(lvalValue instanceof GlobalVariable){
                        initVal = ((GlobalVariable) lvalValue).getInitValue();
                    }
                    // 局部常量数组 是alloca的形式
                    else{
                        initVal = ((Alloca) lvalValue).getInitValue();
                    }
                    // 初值数组已经被存储在initVal对象中，根据[]依次获取即可
                    // 此处仍然在buildingConstExp，因此从expNode中获取的是synInt
                    for(ExpNode expNode : expNodes){
                        expNode.buildIr();
                        initVal = ((ConstArray) initVal).getElements().get(Irc.synInt);
                    }
                    Irc.synInt = ((ConstInt) initVal).getValue();
                }
                // 非常量表达式
                // 这里不再有存储好的初值调用，因此应该使用gep指令
                // 返回指针synValue
                else {
                    // 根据[]不断使用gep向下取值
                    for(ExpNode expNode : expNodes){
                        expNode.buildIr();
                        lvalValue = IrBuilder.buildGetElementPtrInstruction(lvalValue, ConstInt.ZERO(), Irc.synValue, Irc.curBlock);
                    }
                    // 特别地，需要判断一下int a[2][3] 只调用了 a[0]的情况 : 定是函数实参
                    // 此时result是指向数组的指针
                    // 那么需要进行降维传参
                    if(IrTool.getPointingTypeOfPointer(lvalValue) instanceof ArrayType){
                        lvalValue = IrBuilder.buildRankDownInstruction(lvalValue, Irc.curBlock);
                    }
                    Irc.synValue = lvalValue;
                }
            }
        }
    }

    public int getDim(){
        // 先检查名称是否存在
        Symbol symbol = SymbolTableStack.getSymbol(identToken.str, null);
        // 是函数类型或不存在 不予报错
        if( symbol == null || (symbol instanceof FuncSymbol)){
            return -2;
        }
        // 非函数类型且存在 那么一定可以计算返回值维数
        NumSymbol symbol1 = (NumSymbol) symbol;
        int dim = expNodes != null ? symbol1.getDim() - expNodes.size() : symbol1.getDim();
        return Math.max(dim, -1);
    }
}
