package node;

import token.Token;
import token.TokenType;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class FuncDefNode extends Node{
    private FuncTypeNode funcTypeNode;
    private Token identToken;
    private Token lparentToken;
    private FuncFParamsNode funcFParamsNode;
    private Token rparentToken;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token identToken, Token lparentToken, FuncFParamsNode funcFParamsNode, Token rparentToken, BlockNode blockNode) {
        super(NodeType.FuncDef);
        this.funcTypeNode = funcTypeNode;
        this.identToken = identToken;
        this.lparentToken = lparentToken;
        this.funcFParamsNode = funcFParamsNode;
        this.rparentToken = rparentToken;
        this.blockNode = blockNode;
    }
}
