package node;

import utils.IO;

public abstract class Node {
    public NodeType type;

    /**
     * 将当前node的语法分析格式输出至文件
     */
    public abstract void print();

    /**
     * 递归下降地创建符号表 并进行错误处理
     */
    public abstract void check();

    /**
     * 递归下降地生成中间代码
     */
    public abstract void buildIr();

    public Node(NodeType type) {
        this.type = type;
    }

    // 将当前node的类型输出至文件
    public void printNodeType(){
        IO.write(IO.IOType.PARSER, type.getOutputString(), true, true);
    }
}
