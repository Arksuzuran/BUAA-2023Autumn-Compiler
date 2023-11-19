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
}
