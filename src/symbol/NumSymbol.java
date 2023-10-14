package symbol;

import node.Node;

/**
 * @Description const & var
 * @Author
 * @Date 2023/10/13
 **/
public class NumSymbol extends Symbol{
    public int getDim() {
        return dim;
    }

    private int dim;        // 数组维数 0 1 2

    public NumSymbol(String name, SymbolType type, int declLineNum, Node node, int dim) {
        super(name, type, declLineNum, node);
        this.dim = dim;
    }
}
