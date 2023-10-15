package node;

import utils.IO;

public abstract class Node {
    public NodeType type;

    // 将当前node输出至文件
    public abstract void print();
    // 递归下降地创建符号表 并进行错误处理
    public abstract void check();

    public Node(NodeType type) {
        this.type = type;
    }

    // 将当前node的类型输出至文件
    public void printNodeType(){
        IO.write(IO.IOType.PARSER, type.getOutputString(), true, true);
    }
}
