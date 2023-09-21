package node;

import token.Token;

/**
 * @Description 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 * @Author
 * @Date 2023/9/20
 **/
public class UnaryExpNode extends Node{
    private PrimaryExpNode primaryExpNode;
    private Token identToken;
    private Token lparentToken;
    private FuncRParamsNode funcRParamsNode;
    private Token rparentToken;
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;

    public UnaryExpNode(PrimaryExpNode primaryExpNode, Token identToken, Token lparentToken, FuncRParamsNode funcRParamsNode, Token rparentToken, UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        super(NodeType.UnaryExp);
        this.primaryExpNode = primaryExpNode;
        this.identToken = identToken;
        this.lparentToken = lparentToken;
        this.funcRParamsNode = funcRParamsNode;
        this.rparentToken = rparentToken;
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    @Override
    public void print(){
        if(primaryExpNode != null){
            primaryExpNode.print();
        } else if(identToken != null) {
            identToken.print();
            lparentToken.print();
            if(funcRParamsNode != null){
                funcRParamsNode.print();
            }
            rparentToken.print();
        } else {
            unaryOpNode.print();
            unaryExpNode.print();
        }
        printNodeType();
    }
}
