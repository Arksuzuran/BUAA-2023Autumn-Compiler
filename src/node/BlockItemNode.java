package node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class BlockItemNode extends Node{
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        super(NodeType.BlockItem);
        this.declNode = declNode;
        this.stmtNode = stmtNode;
    }
}
