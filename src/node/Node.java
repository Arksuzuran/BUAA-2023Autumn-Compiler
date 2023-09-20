package node;

public abstract class Node {
    public NodeType type;
//    public abstract void printNode();

    public Node(NodeType type) {
        this.type = type;
    }
}
