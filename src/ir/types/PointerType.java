package ir.types;

import ir.values.Value;

/**
 * @Description 指针类型
 * @Author
 * @Date 2023/10/31
 **/
public class PointerType extends ValueType {
    /**
     * 指向的类型
     */
    public ValueType pointingType;

    /**
     * @param pointingType  指针要指向的类型
     */
    public PointerType(ValueType pointingType) {
        this.pointingType = pointingType;
    }
}
