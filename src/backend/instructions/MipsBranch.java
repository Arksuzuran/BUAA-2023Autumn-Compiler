package backend.instructions;

import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import ir.values.instructions.Icmp;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsBranch extends MipsInstruction{
    /**
     * 跳转时src比较的条件，或null代表无条件跳转
     */
    private MipsCondType condType;
    /**
     *  目的Mips块
     */
    private MipsBlock target;

    /**
     * 无条件跳转指令
     * @param target    要跳转到的基本块
     */
    public MipsBranch(MipsBlock target) {
        super(null, null);
        this.target = target;
        this.condType = null;
    }

    /**
     * 有条件跳转指令
     * @param condType  比较条件
     * @param src1      比较数1
     * @param src2      比较数2
     * @param target    要跳转到的基本块
     */
    public MipsBranch(MipsCondType condType, MipsOperand src1, MipsOperand src2, MipsBlock target) {
        super(null, src1, src2);
        this.target = target;
        this.condType = condType;
    }

}
