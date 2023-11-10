package ir.values.instructions;

import ir.types.IntType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description TODO
 * <result> = icmp <cond> <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Icmp extends Instruction{


    /**
     * @param condType  icmp语句的类型
     * @param op1       操作数1
     * @param op2       操作数2
     */
    public Icmp(String name, BasicBlock parent, CondType condType, Value op1, Value op2) {
        super(name, new IntType(1), parent, op1, op2);
        this.condType = condType;
    }

    public enum CondType{
        /**
         * == equal
         */
        EQL("==", "eq"),
        /**
         * != not equal
         */
        NEQ("!=", "ne"),

        /**
         *  <= less or equal
         */
        LEQ("<=", "sle"),

        /**
         * < less than
         */
        LSS("<", "slt"),

        /**
         * >= greater or equal
         */
        GEQ(">=", "sge"),

        /**
         * > greater than
         */
        GRE(">", "sgt");

        private String string;
        private String irString;
        CondType(String string, String irString){
            this.string = string;
            this.irString = irString;
        }
        @Override
        public String toString() {
            return this.irString;
        }
    }

    public CondType getCondType() {
        return condType;
    }

    private CondType condType;

    // %5 = icmp ne i32 0, %4
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = icmp ");  // "%5 = icmp "
        stringBuilder.append(condType).append(" ");          // "ne "
        stringBuilder.append(IrTool.tnstr(getOperands().get(0))).append(", ");   //"i32 0, "
        stringBuilder.append(getOperands().get(1).getName());   // "%4"

        return stringBuilder.toString();
    }
}
