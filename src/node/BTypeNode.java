package node;

import token.Token;
import utils.IO;

/**
 * @Description TODO
 * @Author
 * @Date 2023/9/19
 **/
public class BTypeNode extends Node{
    private Token BTypeToken;

    public BTypeNode(Token BTypeToken) {
        super(NodeType.BType);
        this.BTypeToken = BTypeToken;
    }

    // 不必输出本结点
    @Override
    public void print() {
        BTypeToken.print();
    }
}
