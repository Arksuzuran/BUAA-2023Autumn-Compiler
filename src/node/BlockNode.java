package node;

import token.Token;
import token.TokenType;

import java.util.ArrayList;

/**
 * @Description 语句块 Block → '{' { BlockItem } '}'
 * @Author
 * @Date 2023/9/20
 **/
public class BlockNode extends Node{
    private Token lbraceToken;
    private ArrayList<BlockItemNode> blockItemNodes;
    private Token rbraceToken;

    public BlockNode(Token lbraceToken, ArrayList<BlockItemNode> blockItemNodes, Token rbraceToken) {
        super(NodeType.Block);
        this.lbraceToken = lbraceToken;
        this.blockItemNodes = blockItemNodes;
        this.rbraceToken = rbraceToken;
    }
    public ArrayList<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }
    public Token getRbraceToken() {
        return rbraceToken;
    }
    @Override
    public void print() {
        lbraceToken.print();
        if(!blockItemNodes.isEmpty()){
            for (BlockItemNode blockItemNode : blockItemNodes){
                blockItemNode.print();
            }
        }
        rbraceToken.print();
        printNodeType();
    }

    // Block → '{' { BlockItem } '}'
    @Override
    public void check() {
        for(BlockItemNode blockItemNode : blockItemNodes){
            blockItemNode.check();
        }
    }

    @Override
    public void buildIr() {
        for(BlockItemNode blockItemNode : blockItemNodes){
            blockItemNode.buildIr();
        }
    }


}
