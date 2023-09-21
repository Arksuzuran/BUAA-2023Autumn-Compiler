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

    // 逻辑或表达式 LOrExp → LAndExp | LAndExp '||' LOrExp
    @Override
    public void print(){
        lAndExpNode.print();
        printNodeType();    // 紧跟在后输出
        if(opToken != null){
            opToken.print();
            lOrExpNode.print();
        }
    }
}
