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
        // 绑定被使用者
        for (Value value : operands) {
            if (value != null) {
                value.addUser(this);
            }
        }
    }

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
     * 获取第index个操作数, ！！从1开始！！
     * @param index 索引
     * @return  操作数
     */
    public Value getOp(int index){
        if(operands.size() >= index){
            return operands.get(index - 1);
        }
        return null;
    }

    /**
     * 清除所有op对自己的引用，用于删除自身之前
     */
    public void dropAllOperands() {
        for (Value operand : operands) {
            operand.removeUser(this);
        }
    }
    /**
     * 去掉原来 index 对应的 Value，并且解除 oldValue 的 use
     * @param newValue 新的Value
     */
    public void setUsedValue(int index, Value newValue) {
        Value oldValue = operands.get(index);
        if (oldValue != null) {
            oldValue.removeUser(this);
        }
        operands.set(index, newValue);
        newValue.addUser(this);
    }
}
