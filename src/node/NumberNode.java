package node;

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
}
