package backend.instructions;

import backend.operands.MipsImm;
import backend.operands.MipsLabel;
import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsMove extends MipsInstruction {
    public MipsMove(MipsOperand dst, MipsOperand src1) {
        super(dst, src1);
    }

    public String toString() {
        // 立即数:li
        if (getSrc(1) instanceof MipsImm) {
            return "li\t" + dst + ",\t" + getSrc(1) + "\n";
        }
        // 加载标签地址 la，处理全局变量
        // 在我们先前的llvm中，全局变量在引用时都是以指针的形式出现
        // 因此相应的，mips里引用全局变量亦为其地址
        else if (getSrc(1) instanceof MipsLabel) {
            return "la\t" + dst + ",\t" + getSrc(1) + "\n";
        }
        // 寄存器：move
        else {
            return "move\t" + dst + ",\t" + getSrc(1) + "\n";
        }
    }
}
