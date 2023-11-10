package ir.values.constants;

import ir.types.ValueType;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/7
 **/
public class ZeroInitializer extends Constant{
    public ZeroInitializer(ValueType type) {
        super(type);
    }
    @Override
    public String toString(){
        return "zeroinitializer";
    }
}
