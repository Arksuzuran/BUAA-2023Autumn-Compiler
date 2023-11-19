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

    // 需要限制reg必须为寄存器

    /**
     * 登记先使用寄存器
     */
    private void addUseReg(MipsOperand reg) {
        if (reg instanceof MipsRealReg) {
            useRegs.add((MipsRealReg) reg);
        }
    }
    /**
     * 登记先定义寄存器
     */
    private void addDefReg(MipsOperand reg) {
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

    public void replaceReg(MipsOperand oldReg, MipsOperand newReg)
    {}

    public void replaceUseReg(MipsOperand oldReg, MipsOperand newReg)
    {}

    public ArrayList<MipsRealReg> getDefRegs() {
        return defRegs;
    }

    public ArrayList<MipsRealReg> getUseRegs() {
        return useRegs;
    }
}
