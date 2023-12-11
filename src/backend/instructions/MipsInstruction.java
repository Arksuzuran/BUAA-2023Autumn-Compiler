package backend.instructions;

import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.operands.MipsVirtualReg;
import utils.MipsTool;

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
    private final ArrayList<MipsOperand> defRegs = new ArrayList<>();
    /**
     * 用于活跃变量分析，记录该指令处的右值寄存器（使用）
     */

    private final ArrayList<MipsOperand> useRegs = new ArrayList<>();

    protected MipsOperand dst = null;     //
    protected ArrayList<MipsOperand> src = new ArrayList<>(){{
        add(null);
        add(null);
        add(null);
    }};

    /**
     * 不含dst的三操作数指令构造函数
     */
    public MipsInstruction(MipsOperand src1, MipsOperand src2, MipsOperand src3, Boolean noDst) {
        setSrc(1, src1);
        setSrc(2, src2);
        setSrc(3, src3);
    }
    /**
     * 三操作数指令构造函数
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        setDst(dst);
        setSrc(1, src1);
        setSrc(2, src2);
    }
    /**
     * 双操作数指令构造函数
     * @param src1  src
     */
    public MipsInstruction(MipsOperand dst, MipsOperand src1) {
        setDst(dst);
        setSrc(1, src1);
    }
    public MipsInstruction() {}

    public void setDst(MipsOperand dst) {
        if(dst != null){
            addDefReg(this.dst, dst);
        }
        this.dst = dst;
    }
    /**
     * 编号从1开始
     */
    public void setSrc(int index, MipsOperand src) {
        if(src != null){
            addUseReg(this.src.get(index - 1), src);
        }
        this.src.set(index - 1, src);
    }
    /**
     * 带替换的登记先定义物理寄存器
     */
    public void addDefReg(MipsOperand oldReg, MipsOperand newReg) {
        if (MipsTool.isReg(oldReg)) {
            defRegs.remove(oldReg);
        }
        addDefReg(newReg);
    }
    /**
     * 带替换的登记先使用物理寄存器
     */
    public void addUseReg(MipsOperand oldReg, MipsOperand newReg) {
        if (MipsTool.isReg(oldReg)) {
            useRegs.remove(oldReg);
        }
        addUseReg(newReg);
    }
    // 对于使用、定义寄存器，都是对物理寄存器而言的
    /**
     * 不带替换的登记先使用寄存器
     */
    public void addUseReg(MipsOperand reg) {
        if (MipsTool.isReg(reg)) {
            useRegs.add(reg);
        }
    }
    /**
     * 不带替换的登记先定义寄存器
     */
    public void addDefReg(MipsOperand reg) {
        if (MipsTool.isReg(reg)) {
            defRegs.add(reg);
        }
    }

    /**
     * 替换所有指定寄存器
     */
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if (dst != null && dst.equals(oldReg)) {
            setDst(newReg);
        }
        for(int i=0; i<src.size(); i++){
            MipsOperand reg = src.get(i);
            if(reg != null && reg.equals(oldReg)){
                setSrc(i + 1, newReg);
            }
        }
    }
    public MipsOperand getDst() {
        return dst;
    }
    /**
     * 编号从1开始！
     */
    public MipsOperand getSrc(int index) {
        return src.get(index - 1);
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
    public ArrayList<MipsOperand> getWriteRegs() {
        return new ArrayList<>(defRegs);
    }

    public ArrayList<MipsOperand> getReadRegs() {
        ArrayList<MipsOperand> readRegs = useRegs;

        if (this instanceof MipsCall) {
            readRegs.add(MipsRealReg.SP);
        }

        return readRegs;
    }
    public ArrayList<MipsOperand> getDefRegs() {
        return defRegs;
    }
    public ArrayList<MipsOperand> getUseRegs() {
        return useRegs;
    }
}
