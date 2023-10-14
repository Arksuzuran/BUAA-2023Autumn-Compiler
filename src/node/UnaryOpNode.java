package node;

import token.Token;

/**
 * @Description 单目运算符 UnaryOp → '+' | '−' | '!'
 * @Author
 * @Date 2023/9/20
 **/
public class UnaryOpNode extends Node{
    private Token opToken;

    public UnaryOpNode(Token opToken) {
        super(NodeType.UnaryOp);
        this.opToken = opToken;
    }

    @Override
    public void print(){
        opToken.print();
        printNodeType();
    }

    @Override
    public void check() {
    }
}
