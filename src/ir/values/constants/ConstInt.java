package ir.values.constants;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.Value;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/3
 **/
public class ConstInt extends Constant{
    public int getValue() {
        return value;
    }

    private int value;
    public ConstInt(int bits, int value) {
        super(new IntType(bits), new ArrayList<>());
        this.value = value;
    }
}
