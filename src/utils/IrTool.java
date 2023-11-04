package utils;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

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
        System.out.println("尝试获取非数组指针所指向的数组元素类型");
        return new VoidType();
    }

    public static BasicBlock getHeadBlockOfParentFunction(BasicBlock basicBlock){
        return basicBlock.getParentFunction().getHeadBlock();
    }
}
