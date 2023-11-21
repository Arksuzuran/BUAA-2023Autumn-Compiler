package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsShift extends MipsInstruction {
    public enum ShiftType {
        // 算数右移
        SRA,
        // 逻辑左移
        SLL,
        // 逻辑右移
        SRL
    }
    private final ShiftType type;
    private final int shift;

    public MipsShift(ShiftType type, MipsOperand dst, MipsOperand src, int shift) {
        super(dst, src);
        this.type = type;
        this.shift = shift;
    }

    @Override
    public String toString() {
        if (type.equals(ShiftType.SRA)) {
            return "sra " + getDst() + ",\t" + getSrc1() + ",\t" + shift + "\n";
        } else if (type.equals(ShiftType.SRL)) {
            return "srl " + getDst() + ",\t" + getSrc1() + ",\t" + shift + "\n";
        } else {
            return "sll " + getDst() + ",\t" + getSrc1() + ",\t" + shift + "\n";
        }
    }
}
