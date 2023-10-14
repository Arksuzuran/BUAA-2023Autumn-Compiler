package node;

import token.Token;

/**
 * @Description 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
 * @Author
 * @Date 2023/9/20
 **/
public class LOrExpNode extends Node{
   private LAndExpNode lAndExpNode;
   private Token opToken;
   private LOrExpNode lOrExpNode;

    public LOrExpNode(LAndExpNode lAndExpNode, Token opToken, LOrExpNode lOrExpNode) {
        super(NodeType.LOrExp);
        this.lAndExpNode = lAndExpNode;
        this.opToken = opToken;
        this.lOrExpNode = lOrExpNode;
    }

    // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    public void print(){
        if(opToken == null){
            lAndExpNode.print();
        } else {
            lOrExpNode.print();
            opToken.print();
            lAndExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            lAndExpNode.check();
        } else {
            lOrExpNode.check();
            lAndExpNode.check();
        }
    }
}
