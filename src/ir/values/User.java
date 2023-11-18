package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Description Value的使用者。本身也是一个Value
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class User extends Value{
    public ArrayList<Value> getOperands() {
        return operands;
    }

    /**
     * 向操作数列表中添加操作数op
     * @param op    要添加的操作数
     */
    public void addOperands(Value op){
        operands.add(op);
    }
    /**
     * 该User使用过的Value
     */
    protected final ArrayList<Value> operands = new ArrayList<>();

    public User(String name, ValueType type, Value parent) {
        super(name, type, parent);
    }

    /**
     * 带操作对象values的初始化
     * @param name  User的名称
     * @param type  User的类型
     * @param parent
     * @param operands 傳入的操作數
     */
    public User(String name, ValueType type, Value parent, ArrayList<Value> operands) {
        super(name, type, parent);
        this.operands.addAll(operands);
    }

    /**
     * 获取第index个操作数
     * @param index 索引
     * @return  操作数
     */
    public Value getOp(int index){
        if(operands.size() >= index + 1){
            return operands.get(index);
        }
        return null;
    }

    /**
     * 清除所有op对自己的引用，用于删除自身之前
     */
    public void dropAllOperands() {
        for (Value operand : operands)
        {
            operand.removeUser(this);
        }
    }
}
