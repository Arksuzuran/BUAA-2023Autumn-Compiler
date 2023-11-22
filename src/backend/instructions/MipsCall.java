package backend.instructions;

import backend.operands.MipsOperand;
import backend.parts.MipsFunction;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsCall extends MipsInstruction{
    /**
     * 要调用的函数
     */
    private MipsFunction function;

    public MipsCall(MipsFunction function) {
        super();
        this.function = function;
    }

    @Override
    public String toString()
    {
        return "jal\t" + function.getName() + "\n";
    }
}
