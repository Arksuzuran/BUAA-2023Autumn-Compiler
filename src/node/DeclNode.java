package node;

import utils.IO;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class DeclNode extends Node{
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        super(NodeType.Decl);
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
    }

    @Override
    public void check(){
        if(constDeclNode!=null){
            constDeclNode.check();
        }
        if(varDeclNode!=null){
            varDeclNode.check();
        }
    }
    // 不必输出本结点
    @Override
    public void print() {
        if(constDeclNode!=null){
            constDeclNode.print();
        }
        if(varDeclNode!=null){
            varDeclNode.print();
        }
    }
}
