package node;

import utils.IO;

/**
 * @Description 声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
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

    @Override
    public void buildIr() {
        if(constDeclNode!=null){
            constDeclNode.buildIr();
        }
        if(varDeclNode!=null){
            varDeclNode.buildIr();
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
