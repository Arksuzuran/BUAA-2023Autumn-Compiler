package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description 操作HI寄存器
 * @Author
 * @Date 2023/11/19
 **/
public class MipsMoveHI extends MipsInstruction{
    public enum MoveHIType{
        /**
         * Move to HI
         * 将一个通用寄存器的值移动到HI寄存器中
         */
        MTHI,
        /**
         * Move from HI
         * 将HI寄存器中的值移动到一个通用寄存器中
         */
        MFHI;
    }
    private MoveHIType type;

    /**
     * MTHI: dst为null (HI)
     * MFHI: src为null (HI)
     */
    public MipsMoveHI(MoveHIType type, MipsOperand dst, MipsOperand src) {
        super(type == MoveHIType.MTHI ? null : dst,
                type == MoveHIType.MFHI ? null : src);
        this.type = type;
    }

}
