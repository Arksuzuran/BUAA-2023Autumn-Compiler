package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description sw rt, offset(base)
 * memory[GPR[base]+offset] <- GPR[rt]
 * @Author
 * @Date 2023/11/19
 **/
public class MipsStore extends MipsInstruction {
    /**
     * store的三个操作数均为use而非def
     */
    public MipsStore(MipsOperand src, MipsOperand addr, MipsOperand offset) {
        super(src, addr, offset);
    }

    public MipsOperand getSrc() {
        return getDst();
    }
    public MipsOperand getBase() {
        return getSrc1();
    }
    public MipsOperand getOffset() {
        return getSrc2();
    }

    // 这里的dst其实是src，dst只是父类起的代表大多数子类的名字，在这里应该是src，为use而非def
    @Override
    public void setDst(MipsOperand dst) {
        addUseReg(this.dst, dst);
        this.dst = dst;
    }
    @Override
    public void replaceUseReg(MipsOperand oldReg, MipsOperand newReg) {
        if (dst.equals(oldReg)) {
            setDst(newReg);
        }
        if (src1.equals(oldReg)) {
            setSrc1(newReg);
        }
        if (src2.equals(oldReg)) {
            setSrc2(newReg);
        }
    }
    @Override
    public String toString() {
        return "sw\t" + getSrc() + ",\t" + getOffset() + "(" + getBase() + ")\n";
    }
}
