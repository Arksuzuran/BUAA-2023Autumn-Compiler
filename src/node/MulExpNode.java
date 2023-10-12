package node;

import token.Token;

/**
 * @Description 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 * @Author
 * @Date 2023/9/21
 **/
public class MulExpNode extends Node{
    private UnaryExpNode unaryExpNode;
    private Token opToken;
    private MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.MulExp);
        this.unaryExpNode = unaryExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    @Override
    public void print(){
        if(opToken == null){
            unaryExpNode.print();
        } else {
            mulExpNode.print();
            opToken.print();
            unaryExpNode.print();
        }
        printNodeType();
    }

}
