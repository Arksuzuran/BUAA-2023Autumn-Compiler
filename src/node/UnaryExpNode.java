package node;

import token.Token;

/**
 * @Description TODO
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
}
