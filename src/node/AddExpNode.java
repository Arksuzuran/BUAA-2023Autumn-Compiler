package node;

import token.Token;

/**
 * @Description 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
 * @Author
 * @Date 2023/9/20
 **/
public class AddExpNode extends Node{
    private MulExpNode mulExpNode;
    private Token opToken;
    private AddExpNode addExpNode;

    public AddExpNode(MulExpNode mulExpNode, Token opToken, AddExpNode addExpNode) {
        super(NodeType.AddExp);
        this.mulExpNode = mulExpNode;
        this.opToken = opToken;
        this.addExpNode = addExpNode;
    }

    // 加减表达式 AddExp → MulExp | MulExp ('+' | '−') AddExp
    // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    @Override
    public void print(){
        if(opToken == null){
            mulExpNode.print();
        } else {
            addExpNode.print();
            opToken.print();
            mulExpNode.print();
        }
        printNodeType();    // 紧跟在后输出
    }
}
