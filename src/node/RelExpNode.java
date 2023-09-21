package node;

import token.Token;

/**
 * @Description 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddEx
 * @Author
 * @Date 2023/9/21
 **/
public class RelExpNode extends Node{
    private AddExpNode addExpNode;
    private Token opToken;
    private RelExpNode relExpNode;

    public RelExpNode(AddExpNode addExpNode, Token opToken, RelExpNode relExpNode) {
        super(NodeType.RelExp);
        this.addExpNode = addExpNode;
        this.opToken = opToken;
        this.relExpNode = relExpNode;
    }

    // 关系表达式 RelExp → AddExp | AddEx ('<' | '>' | '<=' | '>=') RelExp
    @Override
    public void print(){
        addExpNode.print();
        printNodeType();    // 紧跟在后输出
        if(opToken != null){
            opToken.print();
            relExpNode.print();
        }
    }
}
