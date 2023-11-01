package ir.values.instructions;

import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @Description TODO
 * <result> = phi [fast-math-flags] <ty> [<val0>, <label0>], [<val1>, <label1>],...
 * 根据控制流来源 来选择不同的值
 * @Author
 * @Date 2023/10/30
 **/
public class Phi extends Instruction{
    private int branchNum = 0;

    public Phi(String name, ValueType type, BasicBlock parent) {
        super(name, type, parent);
    }

    /**
     * 记录新的跳转分支
     * 如果超出了一开始预定的分支个数，那么
     * @param value
     * @param basicBlock
     */
    public void addBranch(Value value, BasicBlock basicBlock){
        getOperands().add(value);
        getOperands().add(basicBlock);
        this.branchNum++;
        value.adduser(this);
        basicBlock.adduser(this);
    }
}
