package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description GPR[rt] memory[GPR[base]+offset]
 * lw $v1, 8($s0)
 * @Author
 * @Date 2023/11/19
 **/
public class MipsLoad extends MipsInstruction {
    private MipsOperand rt;
    private MipsOperand base;
    private MipsOperand offset;

    public MipsLoad(MipsOperand rt, MipsOperand base, MipsOperand offset) {
        setDst(rt);
        setBase(base);
        setOffset(offset);
    }

    public void setDst(MipsOperand rt) {
        addDefReg(this.rt, rt);
        this.rt = rt;
    }

    public void setBase(MipsOperand base) {
        addUseReg(this.base, base);
        this.base = base;
    }

    public void setOffset(MipsOperand offset) {
        addUseReg(this.offset, offset);
        this.offset = offset;
    }

    public MipsOperand getBase() {
        return base;
    }

    public MipsOperand getOffset() {
        return offset;
    }

    public MipsOperand getRt() {
        return rt;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if (rt.equals(oldReg)) {
            setDst(newReg);
        }
        if (base.equals(oldReg)) {
            setBase(newReg);
        }
        if (offset.equals(oldReg)) {
            setOffset(newReg);
        }
    }

    @Override
    public void replaceUseReg(MipsOperand oldReg, MipsOperand newReg) {
        if (base.equals(oldReg)) {
            setBase(newReg);
        }
        if (offset.equals(oldReg)) {
            setOffset(newReg);
        }
    }

    @Override
    public String toString() {
        return "lw " + rt + ",\t" + offset + "(" + base + ")\n";
    }
}
