package backend.operands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public enum RegType {
    ZERO(0, "zero"),
    AT(1, "at"),

    // $v0---$v1  用于返回值的两个值寄存器  (val)
    V0(2, "v0"),
    V1(3, "v1"),

    // $a0---$a3  用于传递参数的4个寄存器
    A0(4, "a0"),
    A1(5, "a1"),
    A2(6, "a2"),
    A3(7, "a3"),

    // $t0---$t9 (8~15)  存放临时变量的寄存器
    T0(8, "t0"),
    T1(9, "t1"),
    T2(10, "t2"),
    T3(11, "t3"),
    T4(12, "t4"),
    T5(13, "t5"),
    T6(14, "t6"),
    T7(15, "t7"),

    // $s0---$s7(16~23)  存放变量的寄存器
    S0(16, "s0"),
    S1(17, "s1"),
    S2(18, "s2"),
    S3(19, "s3"),
    S4(20, "s4"),
    S5(21, "s5"),
    S6(22, "s6"),
    S7(23, "s7"),
    T8(24, "t8"),
    T9(25, "t9"),

    K0(26, "k0"),
    K1(27, "k1"),

    // $gp: 静态数据的全局指针寄存器(reg 28) global pointer for static data (reg 28)
    GP(28, "gp"),

    // $sp: 堆栈指针寄存器stack pointer (reg 29)
    SP(29, "sp"),

    // $fp: 帧指针寄存器(frame pointer) ，保存过程帧的第一个字 (reg 30)
    FP(30, "fp"),

    // $ra   用于返回起始点的返回地址寄存器 (register address，向PC返回调用点的地址)
    RA(31, "ra");
    public final int index;
    public final String name;

    /**
     * 寄存器名（小写0）转寄存器
     */
    public static final HashMap<String, RegType> name2TypeMap = new HashMap<>();
    /**
     * 进行函数调用时，调用者需要保存的寄存器（现场）, 即 zero, at, v0, a0 ~ a3, sp 以外的寄存器
     */
    public final static HashSet<RegType> regsNeedSaving = new HashSet<>();
    /**
     * 进行寄存器分配时，能够分配出去的寄存器, 即 zero, at, sp 以外的寄存器
     */
    public final static HashSet<RegType> regsCanAlloca = new HashSet<>();
    static {
        for(RegType type : RegType.values()){
            name2TypeMap.put(type.name, type);
            regsNeedSaving.add(type);
            regsCanAlloca.add(type);
        }
        // zero, at, v0, a0 ~ a3, sp 以外
        regsNeedSaving.remove(ZERO);
        regsNeedSaving.remove(AT);
        regsNeedSaving.remove(A0);
        regsNeedSaving.remove(A1);
        regsNeedSaving.remove(A2);
        regsNeedSaving.remove(A3);
        regsNeedSaving.remove(SP);
        regsNeedSaving.remove(V0);
        // zero, at, sp 以外
        regsCanAlloca.remove(ZERO);
        regsCanAlloca.remove(AT);
        regsCanAlloca.remove(SP);
        regsCanAlloca.remove(A0);
    }
    RegType(int index, String name){
        this.index = index;
        this.name = name;
    }

    public static RegType getRegType(int index){
        if(index >= 0 && index <= 31){
            return RegType.values()[index];
        }
        return RegType.values()[0];
    }
    public static RegType getRegType(String name){
        if(name2TypeMap.containsKey(name)){
            return name2TypeMap.get(name);
        }
        return RegType.values()[0];
    }
    public int getIndex(){
        return index;
    }
    public String getName(){
        return name;
    }
    // 这里与RealREg的toString方法一样
    @Override
    public String toString(){
        return '$' + name;
    }
}
