package node;

import ir.Irc;
import ir.values.constants.ConstInt;

/**
 * @Description 常量表达式 ConstExp → AddExp
 * @Author
 * @Date 2023/9/19
 **/
public class ConstExpNode extends Node{
    private AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        super(NodeType.ConstExp);
        this.addExpNode = addExpNode;
    }

    @Override
    public void print(){
        addExpNode.print();
        printNodeType();
    }

    @Override
    public void check() {
        addExpNode.check();
    }

    /**
     * 向上传递综合属性Irc.synInt synValue
     */
    @Override
    public void buildIr() {
        Irc.isBuildingConstExp = true;
        addExpNode.buildIr();
        Irc.isBuildingConstExp = false;
        // 顺带构建一下Value
        Irc.synValue = new ConstInt(32, Irc.synInt);
    }
}
