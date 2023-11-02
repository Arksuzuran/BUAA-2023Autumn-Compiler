package ir.values.constants;

import ir.types.ArrayType;
import ir.types.ValueType;
import ir.values.Value;

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
//    public ArrayList<Constant> getElements(Boolean flatten){
//        if(!flatten){
//            return elements;
//        }
//        // 一维数组
//        if(elements.get(0) instanceof ConstInt){
//            return elements;
//        }
//        // 二维数组
//        else {
//            return elements;
//        }
//    }
}
