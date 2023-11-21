package utils;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;

import java.util.ArrayList;

/**
 * @Description 中间代码生成的工具类
 * @Author
 * @Date 2023/11/1
 **/
public class IrTool {

    /**
     * 返回一个指针类型的Value 所指向的类型
     * @param pointer   指针Value对象
     * @return  该指针指向的ValueType类型对象。如果该Value非指针则
     */
    public static ValueType getPointingTypeOfPointer(Value pointer){
        ValueType type = pointer.getType();
        if(type instanceof PointerType){
            return ((PointerType) type).pointingType;
        }
        System.out.println("尝试获取非指针对象所指向的类型");
        return new VoidType();
    }

    /**
     * 给定一个指向数组的指针类型Value
     * 返回其所指向的数组的elementType
     * 即抽离一维[]，并返回该维打开后的基本元素类型
     * @param pointer   指向数组的指针
     * @return          其所指向的数组的elementType
     */
    public static ValueType getElementTypeOfArrayPointer(Value pointer){
        ValueType type = getPointingTypeOfPointer(pointer);
        if(type instanceof ArrayType){
            return ((ArrayType) type).getElementType();
        }
        System.out.println("尝试获取非数组指针所指向的数组元素类型" + pointer + pointer.getType());
//        throw new RuntimeException();
        return new VoidType();
    }

    /**
     * 获取父亲函数对象中的第一个基本块，用于将alloca指令放在头部
     */
    public static BasicBlock getHeadBlockOfParentFunction(BasicBlock basicBlock){
        return basicBlock.getParentFunction().getHeadBlock();
    }

    public static int getValueOfConstInt(Value value){
        return ((ConstInt) value).getValue();
    }

    /**
     * 获取type + " " + name形式的value字符串
     */
    public static String tnstr(Value value){
        return value.getType() + " " + value.getName();
    }

    /**
     * 去除stringBuilder末尾的", "
     */
    public static void cutSBTailComma(StringBuilder stringBuilder){
        int len = stringBuilder.length();
        if(len >= 2 && stringBuilder.charAt(len-2) == ',' && stringBuilder.charAt(len-1) == ' '){
            stringBuilder.delete(len-2, len);
        }
    }

    /**
     * 为stringBuilder添加一个type + " " + name形式的参数列表
     * 例如：[3 x i32]* %13, i32 0, i32 1
     */
    public static void appendSBParamList(StringBuilder stringBuilder, ArrayList<Value> args){
        if(!args.isEmpty()){
            for (Value arg : args){
                stringBuilder.append(IrTool.tnstr(arg)).append(", ");
            }
            cutSBTailComma(stringBuilder);
        }
    }

    /**
     * 将字符串拆解为printf需要分开输出的字符串数组
     * @param s 字符串
     */
    public static ArrayList<String> spiltFormatString(String s){
        ArrayList<String> strings = new ArrayList<>();
        // 将\n替换为\0a
        s = s.replace("\\n", "\\0a");
        int front = 1, len = s.length() - 1;
        for(int i = 1; i < len; i++){
            if(i + 1 < len && s.charAt(i) == '%' && s.charAt(i+1) == 'd'){
                // 如果不是两个%d相连的情形
                if(front != i){
                    strings.add(s.substring(front, i));
                }
                // %d
                strings.add(s.substring(i, i + 2));
                front = i + 2;
                i++;
            }
        }
        // 如果不以%d结尾
        if(front < len){
            strings.add(s.substring(front, len));
        }
        return strings;
    }

    public static int getFormatStringLen(String s){
        String rs = s.replace("\\0a", "r");
        return rs.length();
    }

    public static void main(String[] args) {
        String s = "5929\\0a";
//        s.replace("\\n", "666");
        System.out.println(getFormatStringLen(s));
        System.out.println(s);
    }
}
