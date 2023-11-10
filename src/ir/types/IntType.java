package ir.types;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/1
 **/
public class IntType extends ValueType{
    private final int bits;
    /**
     * @param bits 位数 可以为1或者32
     */
    public IntType(int bits) {
        this.bits = bits;
    }
    @Override
    public int getSize()
    {
        return bits / 8;
    }

    /**
     * 判断是否是i1类型
     */
    @Override
    public boolean isI1(){
        return bits == 1;
    }
    public int getBits(){
        return bits;
    }

    @Override
    public String toString(){
        return "i" + bits;
    }
}
