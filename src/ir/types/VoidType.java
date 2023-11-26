package ir.types;

/**
 * @Description 无类型，用于填充无类型函数、alloca指令中，Value要求的type字段
 * @Author
 * @Date 2023/10/31
 **/
public class VoidType extends ValueType{
    @Override
    public String toString(){
        return "void";
    }
    @Override
    public int getSize() {
        System.out.println("[VoidTypeSize] 非法获取Void类型的Size！");
        return 0;
    }
}
