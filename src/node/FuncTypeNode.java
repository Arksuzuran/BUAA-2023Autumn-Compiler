package node;

import token.Token;

/**
 * @Description  FuncType → 'void' | 'int'
 * @Author
 * @Date 2023/9/20
 **/
public class FuncTypeNode extends Node{
    private Token type;

    public FuncTypeNode(Token type) {
        super(NodeType.FuncType);
        this.type = type;
    }

    @Override
    public void print() {
        type.print();
        printNodeType();
    }
}
