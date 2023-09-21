package node;

import token.Token;
import token.TokenType;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class MainFuncDefNode extends Node{
    private Token intToken;
    private Token mainToken;
    private Token lparentToken;
    private Token rparentToken;
    private BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token lparentToken, Token rparentToken, BlockNode blockNode) {
        super(NodeType.MainFuncDef);
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lparentToken = lparentToken;
        this.rparentToken = rparentToken;
        this.blockNode = blockNode;
    }

    @Override
    public void print() {
        intToken.print();
        mainToken.print();
        lparentToken.print();
        rparentToken.print();
        blockNode.print();
        printNodeType();
    }
}
