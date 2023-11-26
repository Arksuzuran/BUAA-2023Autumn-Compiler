package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description GPR[rt] memory[GPR[base]+offset]
 * lw $v1, 8($s0)
 *
 * @Author
 * @Date 2023/11/19
 **/
public class MipsLoad extends MipsInstruction {
    /**
     * @param dst        存储在dst内
     * @param base      存储在src1内
     * @param offset    存储在src2内
     */
    public MipsLoad(MipsOperand dst, MipsOperand base, MipsOperand offset) {
        super(dst, base, offset);
    }

    public MipsOperand getBase() {
        return getSrc(1);
    }
    public MipsOperand getOffset() {
        return getSrc(2);
    }
    public void setOffset(MipsOperand offset) {
        setSrc(2, offset);
    }
    @Override
    public String toString() {
        return "lw\t" + getDst() + ",\t" + getOffset() + "(" + getBase() + ")\n";
    }
}
