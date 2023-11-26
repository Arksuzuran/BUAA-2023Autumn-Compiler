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
        super(src, addr, offset, true);
    }

    public MipsOperand getSrc() {
        return getSrc(1);
    }
    public MipsOperand getBase() {
        return getSrc(2);
    }
    public MipsOperand getOffset() {
        return getSrc(3);
    }
    public void setOffset(MipsOperand offset){
        setSrc(3, offset);
    }

    @Override
    public String toString() {
        return "sw\t" + getSrc() + ",\t" + getOffset() + "(" + getBase() + ")\n";
    }
}
