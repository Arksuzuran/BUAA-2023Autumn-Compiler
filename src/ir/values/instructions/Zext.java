package ir.values.instructions;

import backend.Mc;
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

    /**
     * 由于要转换的i1 Value只可能是icmp产生的
     * 因此调用icmp的解析方法即可将其解析，并把解析结果与Zext结果进行对应
     */
    @Override
    public void buildMips() {
        Value i1 = getOp(1);
        if(i1 instanceof Icmp){
            ((Icmp) i1).buildMips();
            // 将Zext结果与i1的解析结果页进行绑定
            Mc.addOperandMapping(this, Mc.op(i1));
        } else {
            System.out.println("[Zext]操作数i1不为Icmp类型");
        }
    }
}
