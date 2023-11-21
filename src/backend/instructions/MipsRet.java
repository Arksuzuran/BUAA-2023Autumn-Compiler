package backend.instructions;

import backend.operands.MipsOperand;
import backend.parts.MipsFunction;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsRet extends MipsInstruction{
    // 所属的函数
    private MipsFunction function;

    public MipsRet(MipsFunction function) {
        this.function = function;
    }
}
