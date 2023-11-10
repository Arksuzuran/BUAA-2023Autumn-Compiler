package node;

import ir.IrSymbolTableStack;
import ir.Irc;
import ir.values.Value;
import ir.values.constants.ConstArray;
import ir.values.constants.ConstInt;
import ir.values.constants.Constant;
import token.Token;
import utils.IO;

import java.util.ArrayList;

/**
 * @Description ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
 * @Author H1KARI
 * @Date 2023/9/19
 **/
public class ConstInitValNode extends Node{
    private ConstExpNode constExpNode;
    private Token lbraceToken;
    // 需注意，constInitValNodes比 commaTokens长1
    private ArrayList<ConstInitValNode> constInitValNodes;
    private ArrayList<Token> commaTokens;
    private Token rbraceToken;

    public void setDims(ArrayList<Integer> dims) {
        this.dims = dims;
    }
    /**
     * 各维的长度
     */
    private ArrayList<Integer> dims;

    public ConstInitValNode(ConstExpNode constExpNode, Token lbraceToken, ArrayList<ConstInitValNode> constInitValNodes, ArrayList<Token> commaTokens, Token rbraceToken) {
        super(NodeType.ConstInitVal);
        this.constExpNode = constExpNode;
        this.lbraceToken = lbraceToken;
        this.constInitValNodes = constInitValNodes;
        this.commaTokens = commaTokens;
        this.rbraceToken = rbraceToken;
    }

    @Override
    public void print() {
        if(lbraceToken != null){
            lbraceToken.print();
            if(!constInitValNodes.isEmpty()){
                constInitValNodes.get(0).print();
                if(!commaTokens.isEmpty()){
                    for(int i=0; i<commaTokens.size(); i++){
                        commaTokens.get(i).print();
                        constInitValNodes.get(i+1).print();
                    }
                }
            }
            rbraceToken.print();
        }
        else if(constExpNode != null){
            constExpNode.print();
        }
        printNodeType();
    }

    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    @Override
    public void check() {
        if(lbraceToken != null){
            if(!constInitValNodes.isEmpty()){
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.check();
                }
            }
        }
        else if(constExpNode != null){
            constExpNode.check();
        }
    }
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'

    /**
     * 向上传递常量的初始值
     * @synValue 解析好的初值，ConstInt 或 ConstArray
     * @synValueArray    解析好的展平Value列表   ArrayList<Value>
     */
    @Override
    public void buildIr() {
        // 直接向上传递结果
        if(constExpNode != null){
            constExpNode.buildIr();
        }
        // 需要向下迭代 ConstInitVal, ConstInitVal, ConstInitVal...
        else{
            // 全局常量数组
            // 需要构建constantInitArray，传递给synValue,用于常量数组的初始化
            // 局部常量数组
            // 需要构建constantInitArray，传递给synValue,用于常量数组的初始化
            // 需要构建展平的Value数组，传递给synValueArray，用于store常量数组的值
            ArrayList<Constant> constantInitArray = new ArrayList<>();
            ArrayList<Value> flattenValueArray = new ArrayList<>();
            // 一维，说明下层constExpNode != null 且传上来的是ConstInt
            if(dims.size() == 1){
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.buildIr();
                    constantInitArray.add((ConstInt) Irc.synValue);
                    flattenValueArray.add(Irc.synValue);
                }
            }
            // 多维，还需要为下层设置dim 因为下层会进入上边一维的分支，即用到dims
            // 且传上来的是ConstArray
            else{
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                    constInitValNode.buildIr();
                    constantInitArray.add((ConstArray) Irc.synValue);
                    flattenValueArray.addAll(Irc.synValueArray);
                }
            }
            Irc.synValue = new ConstArray(constantInitArray);
            Irc.synValueArray = flattenValueArray;
        }
    }
}
