package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import utils.IrTool;

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

    //  %7 = call i32 @aaa(i32 %5, i32 %6)
    //  call void @putint(i32 %7)
    @Override
    public String toString(){
        ArrayList<Value> ops = new ArrayList<>(getOperands());
        Function function = (Function) ops.get(0);
        StringBuilder stringBuilder = new StringBuilder();
        // int返回值
        if(!(function.getReturnType() instanceof VoidType)){
            stringBuilder.append(getName()).append(" = ");
        }
        stringBuilder.append("call ").append(function.getReturnType()).append(" ").append(function.getName()).append("(");
        // 实参列表
        if(ops.size() >= 1){
            ops.remove(0);
            IrTool.appendSBParamList(stringBuilder, ops);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
