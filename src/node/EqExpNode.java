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

    // 相等性表达式 EqExp → RelExp | RelExp ('==' | '!=') EqExp
    @Override
    public void print(){
        relExpNode.print();
        printNodeType();    // 紧跟在后输出
        if(opToken != null){
            opToken.print();
            eqExpNode.print();
        }
    }
}
