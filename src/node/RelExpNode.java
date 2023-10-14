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

    // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddEx
    @Override
    public void print(){
        if(opToken == null){
            addExpNode.print();
        } else {
            relExpNode.print();
            opToken.print();
            addExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            addExpNode.check();
        } else {
            relExpNode.check();
            addExpNode.check();
        }
    }
}
