package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.values.Value;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/2
 **/
public class ConstArray extends Constant{
    public final ArrayList<Constant> elements = new ArrayList<>();
    /**
     * 常量數組初始化
     * @param arr   数组中元素的列表
     */
    public ConstArray(ArrayList<Constant> arr) {
        super(new ArrayType(arr.get(0).getType(), arr.size()), new ArrayList<>() {{
            addAll(arr);
        }});
        elements.addAll(arr);
    }

    /**
     * 获取存储的常量数组
     * @return
     */
    public ArrayList<Constant> getElements(){
        return elements;
    }
    // [[3 x i32] [i32 3, i32 8, i32 5], [3 x i32] [i32 1, i32 2, i32 0], [3 x i32] zeroinitializer]

    /**
     * 获取存储的常量数组的展平形式，即拆开所有数组
     */
    public ArrayList<Constant> getFlattenElements(){
        ArrayList<Constant> constants = new ArrayList<>();
        // 一维
        if(elements.get(0).getType() instanceof IntType){
            constants.addAll(elements);
        }
        // 多维
        else {
            for (Constant element : elements){
                constants.addAll(((ConstArray) element).getFlattenElements());
            }
        }
        return constants;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(Constant element : elements){
            stringBuilder.append(element.getType()).append(" ").append(element).append(", ");
        }
        // 删除最后的“, “
        IrTool.cutSBTailComma(stringBuilder);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
