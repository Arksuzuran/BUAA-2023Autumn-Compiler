package node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/20
 **/
public class BlockItemNode extends Node{
    private DeclNode declNode;

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        super(NodeType.BlockItem);
        this.declNode = declNode;
        this.stmtNode = stmtNode;
    }

    // 不必输出本结点
    @Override
    public void print() {
        if(declNode != null){
            declNode.print();
        } else {
            stmtNode.print();
        }
    }

    // 语句块项  BlockItem → Decl | Stmt
    @Override
    public void check() {
        if(declNode != null){
            declNode.check();
        } else {
            stmtNode.check();
        }
    }
}
