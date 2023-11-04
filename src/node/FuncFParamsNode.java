package node;

import ir.IrBuilder;
import ir.IrSymbolTableStack;
import ir.Irc;
import ir.values.Value;
import ir.values.instructions.Alloca;
import ir.values.instructions.Store;
import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class FuncFParamsNode extends Node{

    private ArrayList<FuncFParamNode> funcFParamNodes;
    private ArrayList<Token> commaTokens;

    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commaTokens) {
        super(NodeType.FuncFParams);
        this.funcFParamNodes = funcFParamNodes;
        this.commaTokens = commaTokens;
    }
    public ArrayList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }
    @Override
    public void print() {
        funcFParamNodes.get(0).print();
        if(!commaTokens.isEmpty()){
            for(int i=0; i<commaTokens.size(); i++){
                commaTokens.get(i).print();
                funcFParamNodes.get(i+1).print();
            }
        }
        printNodeType();
    }

    // 函数形参表   FuncFParams → FuncFParam { ',' FuncFParam }
    @Override
    public void check() {
        for(FuncFParamNode funcFParamNode : funcFParamNodes){
            funcFParamNode.check();
        }
    }

    @Override
    public void buildIr() {
        for(FuncFParamNode funcFParamNode : funcFParamNodes){
            funcFParamNode.buildIr();
        }
        // 之前已经将参数加入了function对象
        // 使用刚刚解析好的函数参数，来构建SSA形式的参数加载语句
        /**
         * 参考：
         * define dso_local i32 @a2(i32 %0, i32* %1) {
         *     %3 = alloca i32*
         *     store i32* %1, i32* * %3
         * }
         */
        ArrayList<Value> args = Irc.curFunction.getArgValues();
        for(int i=0; i<funcFParamNodes.size(); i++){
            Value arg = args.get(i);
            Alloca alloca = IrBuilder.buildAllocaInstruction(arg.getType(), Irc.curBlock);
            IrBuilder.buildStoreInstruction(arg, alloca, Irc.curBlock);
            // 在符号表中记录形参，其对应value为alloca，之后调用的时候要load
            IrSymbolTableStack.addSymbolToPeek(funcFParamNodes.get(i).getIdentToken().str, alloca);
        }
    }
}
