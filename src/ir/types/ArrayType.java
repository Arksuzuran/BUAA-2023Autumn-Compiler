package ir.types;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/31
 **/
public class ArrayType extends ValueType{

    public ValueType getElementType() {
        return elementType;
    }

    public int getLen() {
        return len;
    }

    @Override
    public int getSize() {
        return size;
    }

    /**
     * 数组中基本元素的类型，即向下摘一维
     */
    private ValueType elementType;

    /**
     * 数组长度
     */
    private int len;
    /**
     * 数组所占空间大小（bit）
     */
    private int size;

    /**
     * 使用数组的第一维信息创建数组类型
     * @param elementType  第一维类型（脱去一个[]）
     * @param len       元素数量
     */
    public ArrayType(ValueType elementType, int len) {
        this.elementType = elementType;
        this.len = len;
        this.size = len * elementType.getSize();
    }

    /**
     * 使用数组的基本数据类型信息，以及各维信息来创建数组
     * @param rootType  基本单元的类型
     * @param dims      各维的长度
     */
    public ArrayType(ValueType rootType, ArrayList<Integer> dims){
        this.len = dims.get(0);
        // 一维数组
        if(dims.size() == 1){
            this.elementType = rootType;
        }
        // 二维数组 那么this应当存储其第一维元素的类型 需要向上构造一个ArrayType
        else{
            this.elementType = new ArrayType(rootType, dims.get(1));
        }
        this.size = this.len * this.elementType.getSize();
    }
}
