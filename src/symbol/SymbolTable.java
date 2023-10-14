package symbol;

import node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @Description 块的符号表
 * @Author
 * @Date 2023/10/13
 **/
public class SymbolTable {

    // 总符号表
    private static final SymbolTable rootSymbolTable = new SymbolTable(null, null);
    public static SymbolTable getRootSymbolTable(){
        return rootSymbolTable;
    }

    private final TreeMap<String, Symbol> symbolMap;
    public final SymbolTable fatherSymbolTable;    // 所属的父符号表
    private final ArrayList<SymbolTable> sonSymbolTables;   // 所有儿子符号表
    private final Node node;    // 绑定的node
    public SymbolTable(SymbolTable fatherSymbolTable, Node node) {
        this.symbolMap = new TreeMap<>();
        this.fatherSymbolTable = fatherSymbolTable;
        this.sonSymbolTables = new ArrayList<>();
        this.node = node;
    }
    // 往符号表里添加符号
    public void addSymbol(Symbol symbol){
        symbolMap.put(symbol.name, symbol);
    }

    // 查询当前符号表内是否有指定名称的符号
    public boolean hasSymbol(String name){
        return symbolMap.containsKey(name);
    }
    // 查询当前符号表内是否有指定名称指定类型的符号
    public boolean hasSymbol(String name, SymbolType symbolType){
        Symbol symbol = symbolMap.get(name);
        return symbol != null && symbol.type == symbolType;
    }

    // 获取指定名称指定类型的符号, symbolType为空时不检索类型
    public Symbol getSymbol(String name, SymbolType symbolType){
        Symbol symbol = symbolMap.get(name);
        if (symbolType != null) {
            if(symbol != null && symbol.type == symbolType){
                return symbol;
            }
            return null;
        }
        else{
            return symbol;
        }
    }

    // 给当前符号表添加儿子符号表
    public void addSon(SymbolTable symbolTable){
        sonSymbolTables.add(symbolTable);
    }

}
