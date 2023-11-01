package symbol;

import node.Node;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/13
 **/
public class Symbol {
    public String name;        // 符号名
    public SymbolType type;    // 符号类型
    public int declLineNum;    // 声明的行号
    public Node node;      // 加入符号表时，其对应的语法树中的node

    public int useLineNum = -1;     // 使用的行号

    public int val;             // 符号的值

    // 变量或者常量的构造函数
    public Symbol(String name, SymbolType type, int declLineNum, Node node) {
        this.name = name;
        this.type = type;;
        this.declLineNum = declLineNum;
        this.node = node;
    }
}
