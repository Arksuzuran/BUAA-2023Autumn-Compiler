package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsCompare extends MipsInstruction{
    /**
     * 比较的类型
     */
    private MipsCondType type;

    public MipsCompare(MipsCondType type, MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        super(dst, src1, src2);
        this.type = type;
    }
}
