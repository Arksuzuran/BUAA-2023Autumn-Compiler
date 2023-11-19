package backend.operands;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsImm extends MipsOperand{
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public MipsImm(int value) {
        this.value = value;
    }



}
