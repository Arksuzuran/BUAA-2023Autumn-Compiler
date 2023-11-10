package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

/**
 * @Description 类型转换 将ty类型的value转换为ty2类型
 * 实际上只需要将i1转换为i32
 * <result> = zext <ty> <value> to <ty2>
 * 实际上是 <result> = zext i1 <value> to i32
 * @Author
 * @Date 2023/10/30
 **/
public class Zext extends Instruction{

    public Zext(String name, BasicBlock parent, Value value) {
        super(name, new IntType(32), parent, value);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = zext i1 ");
        stringBuilder.append(getOperands().get(0).getName());
        stringBuilder.append(" to i32");
        return stringBuilder.toString();
    }
}
