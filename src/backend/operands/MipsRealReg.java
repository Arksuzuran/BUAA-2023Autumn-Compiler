package backend.operands;

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
}
