package node;

import ir.Irc;
import ir.values.constants.ConstInt;
import token.Token;

/**
 * @Description 数值 Number → IntConst
 * @Author
 * @Date 2023/9/20
 **/
public class NumberNode extends Node{
    private Token intConstToken;

    public NumberNode(Token intConstToken) {
        super(NodeType.Number);
        this.intConstToken = intConstToken;
    }

    @Override
    public void print(){
        intConstToken.print();
        printNodeType();
    }

    @Override
    public void check() {
    }

    /**
     * 计算常数仅传递synInt
     * 计算非常数仅传递synValue
     */
    @Override
    public void buildIr() {
        int num = Integer.parseInt(intConstToken.str);
        if(Irc.isBuildingConstExp){
            Irc.synInt = num;
        } else{
            Irc.synValue = new ConstInt(32, num);
        }
    }
}
