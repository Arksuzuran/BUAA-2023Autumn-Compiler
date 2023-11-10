package node;

import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import token.Token;
import token.TokenType;

/**
 * @Description  FuncType → 'void' | 'int'
 * @Author
 * @Date 2023/9/20
 **/
public class FuncTypeNode extends Node{
    private Token type;

    public FuncTypeNode(Token type) {
        super(NodeType.FuncType);
        this.type = type;
    }

    @Override
    public void print() {
        type.print();
        printNodeType();
    }

    public boolean hasReturnVal(){
        return type.type == TokenType.INTTK;
    }
    @Override
    public void check() {

    }

    @Override
    public void buildIr() {

    }

    /**
     * 中间代码生成 获取ValueType类型的返回值类型
     * @return
     */
    public ValueType getIrReturnType(){
        return type.type == TokenType.VOIDTK ? new VoidType() : new IntType(32);
    }
}
