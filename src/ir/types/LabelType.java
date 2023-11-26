package ir.types;

/**
 * @Description 标签类型 为基本块作为标识
 * @Author
 * @Date 2023/10/31
 **/
public class LabelType extends ValueType{
    @Override
    public String toString(){
        return "label";
    }

    @Override
    public int getSize() {
        System.out.println("[LabelTypeSize] 非法获取Label类型的Size！");
        return 0;
    }
}
