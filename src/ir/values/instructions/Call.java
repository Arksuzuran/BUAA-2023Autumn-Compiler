package ir.values.instructions;

import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 函数调用指令
 * <result> = call [ret attrs] <ty> <fnptrval>(<function args>)
 * @Author
 * @Date 2023/10/30
 **/
public class Call extends Instruction{


    /**
     * call指令的ValueType为函数返回值的ValueType
     * @param parent    一定是Block
     * @param function  op1 函数对象
     * @param rArgs      op2 3 ...函数的实参
     */
    public Call(String name, BasicBlock parent, Function function, ArrayList<Value> rArgs) {
        super(name, function.getReturnType(), parent, new ArrayList<Value>()
        {{
            add(function);
            addAll(rArgs);
        }}.toArray(new Value[0]));
    }
}
