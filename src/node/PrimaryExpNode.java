package node;

import token.Token;

/**
 * @Description 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
 * @Author
 * @Date 2023/9/20
 **/
public class PrimaryExpNode extends Node{
    private Token lparentToken;
    private ExpNode expNode;
    private Token rparentToken;
    private LValNode lValNode;
    private NumberNode numberNode;

    public PrimaryExpNode(Token lparentToken, ExpNode expNode, Token rparentToken, LValNode lValNode, NumberNode numberNode) {
        super(NodeType.PrimaryExp);
        this.lparentToken = lparentToken;
        this.expNode = expNode;
        this.rparentToken = rparentToken;
        this.lValNode = lValNode;
        this.numberNode = numberNode;
    }

    @Override
    public void print(){
        if(lparentToken!=null){
            lparentToken.print();
            expNode.print();
            rparentToken.print();
        } else if(lValNode != null) {
            lValNode.print();
        } else {
            numberNode.print();
        }
        printNodeType();
    }
    // 基本表达式   PrimaryExp → '(' Exp ')' | LVal | Number
    @Override
    public void check() {
        if(lparentToken!=null){
            expNode.check();
        } else if(lValNode != null) {
            lValNode.check();
        }
    }

    public int getDim(){
        // '(' Exp ')'
        if(lparentToken!=null){
            return expNode.getDim();
        }
        // LVal
        else if(lValNode != null) {
            return lValNode.getDim();
        }
        // Number
        else {
            return 0;
        }
    }
}
