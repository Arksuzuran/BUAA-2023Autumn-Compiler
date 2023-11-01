package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;

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
        EQ,

        /**
         *  <= less or equal
         */
        LE,

        /**
         * < less than
         */
        LT,

        /**
         * >= greater or equal
         */
        GE,

        /**
         * > greater than
         */
        GT,

        /**
         * != not equal
         */
        NE;
    }

    public CondType getCondType() {
        return condType;
    }

    private CondType condType;

}
