package node;

import ir.IrSymbolTableStack;
import ir.Irc;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import token.Token;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class InitValNode extends Node{
    private ExpNode expNode;
    private Token lbraceToken;
    private ArrayList<InitValNode> initValNodes;
    private ArrayList<Token> commaTokens;
    private Token rbraceToken;

    private ArrayList<Integer> dims;

    public InitValNode(ExpNode expNode, Token lbraceToken, ArrayList<InitValNode> initValNodes, ArrayList<Token> commaTokens, Token rbraceToken) {
        super(NodeType.InitVal);
        this.expNode = expNode;
        this.lbraceToken = lbraceToken;
        this.initValNodes = initValNodes;
        this.commaTokens = commaTokens;
        this.rbraceToken = rbraceToken;
    }

    public void setDims(ArrayList<Integer> dims) {
        this.dims = dims;
    }
    @Override
    public void print() {
        if(expNode != null){
            expNode.print();
        }
        else{
            lbraceToken.print();
            if(!initValNodes.isEmpty()){
                initValNodes.get(0).print();
                if(!commaTokens.isEmpty()){
                    for(int i=0; i<commaTokens.size(); i++){
                        commaTokens.get(i).print();
                        initValNodes.get(i+1).print();
                    }
                }
            }
            rbraceToken.print();
        }
        printNodeType();
    }

    // 变量初值    InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    @Override
    public void check() {
        if(expNode != null){
            expNode.check();
        } else {
            for(InitValNode initValNode : initValNodes){
                initValNode.check();
            }
        }
    }

    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    /**
     * 向上传递变量的初始值
     * @synValue    解析好的初值，ConstInt 或 ConstArray
     * @synValueArray    解析好的展平Value列表   ArrayList<Value>
     */
    @Override
    public void buildIr() {
        // Exp不为空，直接向上传递
        // 关于isBuildingConstExp，在addExp下层已经进行了处理，会返回对应的synInt or synValue
        // 在此将synInt封装为synValue
        if(expNode != null){
            expNode.buildIr();
            if(Irc.isBuildingConstExp){
                Irc.synValue = new ConstInt(32, Irc.synInt);
//                System.out.println("构建全局非数组变量 " + Irc.synInt);
            }
        }
        // InitVal, InitVal, InitVal, InitVal ... 递归处理
        //
        else {
            // 全局变量数组
            // 需要构建constantInitArray，传递给synValue,用于数组的初始化
            if(IrSymbolTableStack.isBuildingGlobalSymbolTable()){
                ArrayList<Constant> constantInitArray = new ArrayList<>();
                // 一维，说明下层constExpNode != null 且传上来的是ConstInt
                if(dims.size() == 1){
                    for(InitValNode initValNode : initValNodes){
                        initValNode.buildIr();
                        constantInitArray.add((ConstInt) Irc.synValue);
                    }
                }
                // 多维，还需要为下层设置dim 因为下层会进入上边一维的分支，即用到dims
                // 且传上来的是ConstArray
                else{
                    for(InitValNode initValNode : initValNodes){
                        initValNode.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                        initValNode.buildIr();
                        constantInitArray.add((Constant) Irc.synValue);
                    }
                }
                Irc.synValue = new ConstArray(constantInitArray);
            }
            // 局部变量数组
            // 需要构建展平的Value数组，传递给synValueArray，用于store常量数组的值
            else {
                ArrayList<Value> flattenValueArray = new ArrayList<>();
                if(dims.size() == 1){
                    for(InitValNode initValNode : initValNodes){
                        initValNode.buildIr();
                        flattenValueArray.add(Irc.synValue);
                    }
                }
                // 多维，还需要为下层设置dim 因为下层会进入上边一维的分支，即用到dims
                // 且传上来的是ConstArray
                else{
                    for(InitValNode initValNode : initValNodes){
                        initValNode.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                        initValNode.buildIr();
                        flattenValueArray.addAll(Irc.synValueArray);
                    }
                }
                Irc.synValueArray = flattenValueArray;
            }
        }
    }
}
