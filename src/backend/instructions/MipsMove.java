package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsMove extends MipsInstruction{
    public MipsMove(MipsOperand dst, MipsOperand src) {
        super(dst, src);
    }
}
