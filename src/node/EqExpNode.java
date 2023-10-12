package node;

import token.Token;

/**
 * @Description 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
 * @Author
 * @Date 2023/9/21
 **/
public class EqExpNode extends Node{
    private RelExpNode relExpNode;
    private Token opToken;
    private EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode, Token opToken, EqExpNode eqExpNode) {
        super(NodeType.EqExp);
        this.relExpNode = relExpNode;
        this.opToken = opToken;
        this.eqExpNode = eqExpNode;
    }

    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
    @Override
    public void print(){
        if(opToken == null){
            relExpNode.print();
        } else {
            eqExpNode.print();
            opToken.print();
            relExpNode.print();
        }
        printNodeType();
    }
}
