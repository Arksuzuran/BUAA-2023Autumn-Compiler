package node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class DeclNode {
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
    }

}
