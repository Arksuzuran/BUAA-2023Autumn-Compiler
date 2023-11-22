package backend.operands;

/**
 * @Description 类似Ir的虚拟寄存器
 * @Author
 * @Date 2023/11/19
 **/
public class MipsVirtualReg extends MipsOperand{
    private static int nameCnt = 0;
    private String name;
    private int getNameCnt(){
        return nameCnt++;
    }
    public MipsVirtualReg() {
        this.name = "v" + getNameCnt();
    }

    /**
     * 虚拟寄存器都需要着色
     */
    @Override
    public boolean needsColor()
    {
        return true;
    }
    @Override
    public String toString()
    {
        return name;
    }
}
