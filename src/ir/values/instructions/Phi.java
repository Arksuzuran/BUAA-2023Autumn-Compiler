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
public class Phi extends Instruction {
//    private int branchNum = 0;
//    /**
//     * 记录新的跳转分支
//     * 如果超出了一开始预定的分支个数，那么
//     *
//     * @param value
//     * @param basicBlock
//     */
//    public void addBranch(Value value, BasicBlock basicBlock){
//        getOperands().add(value);
//        getOperands().add(basicBlock);
//        this.branchNum++;
//        value.adduser(this);
//        basicBlock.adduser(this);
//    }
    private int predecessorNum;

    public Phi(String name, ValueType type, BasicBlock parent, int predecessorNum) {
        super(name, type, parent, true, new Value[predecessorNum * 2]);
        this.predecessorNum = predecessorNum;
    }

    public int getPredecessorNum() {
        return predecessorNum;
    }

    public void addIncoming(Value value, BasicBlock block) {
        int i = 0;
        while (i < predecessorNum && getOp(i+1) != null) {
            i++;
        }
        if (i < predecessorNum) {
            setUsedValue(i, value);
            setUsedValue(i + predecessorNum, block);
//            System.out.println("11");
        } else {
            getOperands().add(predecessorNum, value);
            predecessorNum++;
            addOperands(block);
//            System.out.println("22");
        }
        value.addUser(this);
        block.addUser(this);
    }

    /**
     * 移除冗余的 phi，比如说所有的 input 都相等的情况
     *
     * @param reducePhi 是否进行
     */
    public void removeIfRedundant(boolean reducePhi) {
        if (getUsers().isEmpty()) {
            dropAllOperands();
            eraseFromParent();
            return;
        }
        if (predecessorNum == 0) {
            throw new AssertionError(this + "'s predecessorNum = 0!");
        }
        Value commonValue = getOp(1);
        for (int i = 1; i < predecessorNum; i++) {
            if (commonValue != getOp(i + 1)) {
                return;
            }
        }
        if (!reducePhi && commonValue instanceof Instruction) {
            return;
        }
        replaceAllUsesWith(commonValue);
        dropAllOperands();
        eraseFromParent();
    }

    public Value getInputValForBlock(BasicBlock block) {
        for (int i = 0; i < predecessorNum; i++) {
            if (getOp(i + predecessorNum + 1) == block) {
                return getOp(i + 1);
            }
        }

        throw new AssertionError("block not found for phi!");
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getName() + " = phi ").append(getType());
        for (int i = 0; i < predecessorNum; i++) {
            if (getOp(i + 1) == null) break;
            s.append(" [ ").append(getOp(i + 1).getName()).append(", ")
                    .append(getOp(i + predecessorNum + 1).getName()).append(" ], ");
        }
        s.delete(s.length() - 2, s.length());
        return s.toString();
    }
}
