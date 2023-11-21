package backend.instructions;

import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsInstruction {
    /**
     * 用于活跃变量分析，记录该指令处的左值寄存器（定义）
     */
    private final ArrayList<MipsRealReg> defRegs = new ArrayList<>();
    /**
     * 用于活跃变量分析，记录该指令处的右值寄存器（使用）
     */
    private final ArrayList<MipsRealReg> useRegs = new ArrayList<>();

    protected MipsOperand dst = null;     //
    protected MipsOperand src1 = null;    //
    protected MipsOperand src2 = null;    //

    /**
     * 三操作数指令构造函数
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        setDst(dst);
        setSrc1(src1);
        setSrc2(src2);
    }
    /**
     * 双操作数指令构造函数
     * @param src1  src
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src1) {
        setDst(dst);
        setSrc1(src1);
    }
    public MipsInstruction() {}

    public void setDst(MipsOperand dst) {
        if(dst != null){
            addDefReg(this.dst, dst);
        }
        this.dst = dst;
    }
    public void setSrc1(MipsOperand src1) {
        if(src1 != null){
            addUseReg(this.src1, src1);
        }
        this.src1 = src1;
    }
    public void setSrc2(MipsOperand src2) {
        if(src2 != null){
            addUseReg(this.src2, src2);
        }
        this.src2 = src2;
    }
    public MipsOperand getDst() {
        return dst;
    }
    public MipsOperand getSrc1() {
        return src1;
    }
    public MipsOperand getSrc2() {
        return src2;
    }
    /**
     * 登记先使用寄存器
     */
    public void addUseReg(MipsOperand reg) {
        if (reg instanceof MipsRealReg) {
            useRegs.add((MipsRealReg) reg);
        }
    }
    /**
     * 登记先定义寄存器
     */
    public void addDefReg(MipsOperand reg) {
        if (reg instanceof MipsRealReg) {
            defRegs.add((MipsRealReg) reg);
        }
    }

    /**
     * 带替换的登记先定义寄存器
     */
    public void addDefReg(MipsOperand oldReg, MipsOperand newReg) {
        if (oldReg instanceof MipsRealReg) {
            defRegs.remove((MipsRealReg) oldReg);
        }
        addDefReg(newReg);
    }
    /**
     * 带替换的登记先使用寄存器
     */
    public void addUseReg(MipsOperand oldReg, MipsOperand newReg) {
        if (oldReg instanceof MipsRealReg) {
            useRegs.remove((MipsRealReg) oldReg);
        }
        addUseReg(newReg);
    }
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if (dst != null && dst.equals(oldReg)) {
            setDst(newReg);
        }
        if (src1 != null && src1.equals(oldReg)) {
            setSrc1(newReg);
        }
        if (src2 != null && src2.equals(oldReg)) {
            setSrc2(newReg);
        }
    }
    public void replaceUseReg(MipsOperand oldReg, MipsOperand newReg) {
        if (src1 != null && src1.equals(oldReg)) {
            setSrc1(newReg);
        }
        if (src2 != null && src2.equals(oldReg)) {
            setSrc2(newReg);
        }
    }

    /**
     * 只有 branch 指令（条件跳转）时候有这个可能为 false
     * @return 当无条件的时候，返回 true
     */
    public boolean hasNoCond() {
        return true;
    }

    /**
     * 表示因此改变的寄存器
     * 可能要比 define 多一些，这是因为寄存器分配只是分析变量
     */
    public ArrayList<MipsRealReg> getWriteRegs() {
        return new ArrayList<>(defRegs);
    }

    public ArrayList<MipsRealReg> getReadRegs() {
        ArrayList<MipsRealReg> readRegs = useRegs;

        if (this instanceof MipsCall) {
            readRegs.add(MipsRealReg.SP);
        }

        return readRegs;
    }

    public ArrayList<MipsRealReg> getDefRegs() {
        return defRegs;
    }

    public ArrayList<MipsRealReg> getUseRegs() {
        return useRegs;
    }
}
