package backend.operands;


import java.util.Objects;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsLabel extends MipsOperand{
    private String name;

    public MipsLabel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsLabel objLabel = (MipsLabel) o;
        return Objects.equals(name, objLabel.name);
    }
}
