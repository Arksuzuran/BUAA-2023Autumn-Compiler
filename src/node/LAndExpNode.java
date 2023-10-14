package node;

import token.Token;

/**
 * @Description 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
 * @Author
 * @Date 2023/9/21
 **/
public class LAndExpNode extends Node{
    private EqExpNode eqExpNode;
    private Token opToken;
    private LAndExpNode lAndExpNode;

    public LAndExpNode(EqExpNode eqExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LAndExp);
        this.eqExpNode = eqExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }

    // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    @Override
    public void print(){
        if(opToken == null){
            eqExpNode.print();
        } else {
            lAndExpNode.print();
            opToken.print();
            eqExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            eqExpNode.check();
        } else {
            lAndExpNode.check();
            eqExpNode.check();
        }
    }
}
