package backend.operands;

import java.util.HashSet;
import java.util.Objects;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsRealReg extends MipsOperand{
    private RegType type;
    private boolean isAllocated;


    public final static MipsRealReg ZERO = new MipsRealReg(0);
    public final static MipsRealReg AT = new MipsRealReg("at");
    public final static MipsRealReg SP = new MipsRealReg("sp");
    public final static MipsRealReg RA = new MipsRealReg("ra");
    public final static MipsRealReg V0 = new MipsRealReg("v0");

    public MipsRealReg(int index) {
        this.type = RegType.getRegType(index);
        this.isAllocated = false;
    }
    public MipsRealReg(String name) {
        this.type = RegType.getRegType(name);
        this.isAllocated = false;
    }
    public MipsRealReg(int index, boolean isAllocated) {
        this.type = RegType.getRegType(index);
        this.isAllocated = isAllocated;
    }
    public MipsRealReg(RegType type, boolean isAllocated) {
        this.type = type;
        this.isAllocated = isAllocated;
    }

    /**
     * 获取物理寄存器编号
     */
    public int getIndex(){
        return type.getIndex();
    }
    public RegType getType(){
        return type;
    }

    /**
     * 如果一个寄存器是物理寄存器,而且还没有被分配,那么就是需要预着色的
     * 所谓的预着色，可能指的是在图着色中没分配，就已经是物理寄存器的情况
     * 可能这个的意思就是，对于物理寄存器，只有两种状态，没分配的叫预着色，分配的叫 allocated
     * @return true 就是预着色
     */
    @Override
    public boolean isPrecolored()
    {
        return !isAllocated;
    }

    @Override
    public boolean isAllocated()
    {
        return isAllocated;
    }

    public void setAllocated(boolean allocated)
    {
        isAllocated = allocated;
    }
    /**
     * 对于一个物理寄存器，只要他还没有被分配，那么就是需要着色的
     */
    @Override
    public boolean needsColor()
    {
        return !isAllocated;
    }

    @Override
    public String toString() {
        return "$" + type.getName();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsRealReg reg = (MipsRealReg) o;
        return type == reg.type && isAllocated == reg.isAllocated;
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(type.index, isAllocated);
    }
}
