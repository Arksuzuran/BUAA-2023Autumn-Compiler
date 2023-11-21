package backend.instructions;

import ir.values.instructions.Icmp;

import java.util.HashMap;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public enum MipsCondType{
    /**
     * == equal
     */
    EQ("==", "eq"),
    /**
     * != not equal
     */
    NE("!=", "ne"),

    /**
     *  <= less or equal
     */
    LE("<=", "le"),

    /**
     * < less than
     */
    LT("<", "lt"),

    /**
     * >= greater or equal
     */
    GE(">=", "ge"),

    /**
     * > greater than
     */
    GT(">", "gt");

    private String meaning;
    private String mipsName;

    MipsCondType(String meaning, String name){
        this.meaning = meaning;
        this.mipsName = name;
    }

    private static final HashMap<Icmp.CondType, MipsCondType> ir2mips = new HashMap<>(){{
        put(Icmp.CondType.EQL, MipsCondType.EQ);
        put(Icmp.CondType.NEQ, MipsCondType.NE);
        put(Icmp.CondType.GEQ, MipsCondType.GE);
        put(Icmp.CondType.GRE, MipsCondType.GT);
        put(Icmp.CondType.LEQ, MipsCondType.LE);
        put(Icmp.CondType.LSS, MipsCondType.LT);
    }};

    /**
     * 将中间代码中的CondType类转化为Mips的CondType类
     */
    public static MipsCondType IrCondType2MipsCondType(Icmp.CondType type){
        return ir2mips.get(type);
    }

    /**
     * 根据条件类型，直接进行运算
     */
    public static int doCondCalculation(MipsCondType type, int src1, int src2){
        boolean result = false;
        switch (type){
            case EQ -> result = src1 == src2;
            case NE -> result = src1 != src2;
            case GE -> result = src1 >= src2;
            case GT -> result = src1 > src2;
            case LE -> result = src1 <= src2;
            case LT -> result = src1 < src2;
        }
        return result ? 1 : 0;
    }
}
