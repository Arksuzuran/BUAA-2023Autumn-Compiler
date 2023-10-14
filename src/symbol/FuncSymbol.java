package symbol;

import node.Node;

import java.util.ArrayList;

/**
 * @Description 函数符号
 * @Author
 * @Date 2023/10/13
 **/
public class FuncSymbol extends Symbol{
    public enum FuncReturnType{
        INT, VOID;
    }

    public FuncReturnType getReturnType() {
        return returnType;
    }

    private FuncReturnType returnType;

    public ArrayList<NumSymbol> getParams() {
        return params;
    }

    private ArrayList<NumSymbol> params;    // 参数符号列表

    public FuncSymbol(String name, int declLineNum, Node node, FuncReturnType returnType, ArrayList<NumSymbol> params) {
        super(name, SymbolType.Function, declLineNum, node);
        this.returnType = returnType;
        this.params = params;
    }
}
