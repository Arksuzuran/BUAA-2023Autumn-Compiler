package ir.types;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/1
 **/
public class FunctionType extends ValueType{
    @Override
    public int getSize() {
        System.out.println("[FunctionTypeSize] 非法获取Function类型的Size！");
        return 0;
    }
}
