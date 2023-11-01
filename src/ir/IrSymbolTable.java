package ir;

import ir.values.Value;
import symbol.Symbol;
import symbol.SymbolType;

import java.util.HashMap;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class IrSymbolTable {
    private final HashMap<String, Value> symbolMap = new HashMap<>();

    // 往符号表里添加符号
    public void addSymbol(String name, Value value){
        symbolMap.put(name, value);
    }
    // 获取指定名称的符号
    public Value getSymbol(String name){
        return symbolMap.get(name);
    }
    // 查询当前符号表内是否有指定名称的符号
    public boolean hasSymbol(String name){
        return getSymbol(name) != null;
    }
}
