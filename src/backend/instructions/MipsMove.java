package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsMove extends MipsInstruction{

    private MipsOperand dst;
    private MipsOperand src;
    public MipsMove(MipsOperand dst, MipsOperand src) {
        this.src = src;
        this.dst = dst;
    }
}
