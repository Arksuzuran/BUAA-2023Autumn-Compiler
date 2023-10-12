package node;

import utils.IO;

public abstract class Node {
    public NodeType type;

    // 将当前node输出至文件
    public abstract void print();

    public Node(NodeType type) {
        this.type = type;
    }

    // 将当前node的类型输出至文件
    public void printNodeType(){
        IO.write(type.getOutputString(), true, true);
    }
}
