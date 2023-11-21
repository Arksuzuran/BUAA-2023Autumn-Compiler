package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsBinary extends MipsInstruction{
    public enum BinaryType{
        /**
         * SUBU (Subtract Unsigned):
         * 作用：执行无符号整数减法，结果存储在目标寄存器中。
         * 语法：SUBU rd, rs, rt
         */
        SUBU("subu"),

        /**
         * ADDU (Add Unsigned):
         * 作用：执行无符号整数加法，将rs和rt相加的结果存储在目标寄存器中。
         * 语法：ADDU rd, rs, rt
         */
        ADDU("addu"),

        /**
         * MUL (Multiply):
         * 作用：执行有符号整数乘法，将rs和rt相乘的结果存储在目标寄存器中。
         * 语法：MUL rd, rs, rt
         */
        MUL("mul"),

        /**
         * DIV (Divide):
         * 作用：执行有符号整数除法，将rs除以rt的结果存储在目标寄存器中。
         * 语法：DIV rd, rs, rt
         */
        DIV("div"),

        /**
         * XOR (Exclusive OR):
         * 作用：执行按位异或操作，将两个寄存器的内容进行异或运算，结果存储在目标寄存器中。
         * 语法：XOR rd, rs, rt
         */
        XOR("xor"),

        /**
         * SLTU (Set on Less Than Unsigned):
         * 作用：如果无符号整数rs小于rt，则将目标寄存器设置为1，否则设置为0。
         * 语法：SLTU rd, rs, rt
         */
        SLTU("sltu"),

        /**
         * SLT (Set on Less Than):
         * 作用：如果有符号整数rs小于rt，则将目标寄存器设置为1，否则设置为0。
         * 语法：SLT rd, rs, rt
         */
        SLT("slt"),

        /**
         * SMMUL (Signed Multiply and Multiply-Accumulate):
         * 作用：执行有符号整数乘法，并将乘积累加到目标寄存器中的值。
         * 语法：SMMUL rd, rs, rt
         */
        SMMUL("smmul"),

        /**
         * SMMADD (Signed Multiply and Multiply-Add):
         * 作用：执行有符号整数乘法，并将乘积与第三个寄存器中的值相加。
         * 在这里我们指定ra为HI
         * 语法：SMMADD rd, rs, rt, ra
         */
        SMMADD("smmadd");

        public String name;
        BinaryType(String name){
            this.name = name;
        }
    }
    private final BinaryType type;
    public MipsBinary(BinaryType type, MipsOperand dst, MipsOperand src1, MipsOperand src2) {
        super(dst, src1, src2);
        this.type = type;
    }
}
