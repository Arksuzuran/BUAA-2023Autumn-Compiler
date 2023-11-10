package ir.values.instructions;

import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 寻址指令 计算目标地址 对应value为指针类型
 * <result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
 * @Author
 * @Date 2023/10/30
 **/
public class GetElementPtr extends Instruction{
    /**
     * 基地址指针ptrval所指向的类型
     */
    private final ValueType ptrPointingType;
    /**
     * 双参数寻址指令 用于数组寻址
     * 所得value类型为指针 指向剥离一层[]后的基本元素类型
     *
     * 例如 ptrval为[2 * [3 * i32]]*，那么返回的指针类型应该指向一维数组，即[3 * i32]*
     * 例如 ptrval为[3 * i32]*，那么返回的指针类型应该指向0维数组int，即i32*
     * @param ptrval    基地址的指针。
     * @param index1    本维偏移（here * [][]）
     * @param index2    高维偏移（[here][]）
     */
    public GetElementPtr(String name, BasicBlock parent, Value ptrval, Value index1, Value index2) {
        // 需要抽取
        super(name,
              new PointerType(IrTool.getElementTypeOfArrayPointer(ptrval)),
                parent, ptrval, index1, index2);
        this.ptrPointingType = IrTool.getPointingTypeOfPointer(ptrval);
    }
    /**
     * 单参数寻址指令 处理函数传参
     * 所得value类型为指针 与传入ptrval类型一致
     * 实质为指针的加减
     * @param ptrval    基地址的指针。本instruction和基地址的类型相同
     * @param index1    本维度偏移（here * [][]）
     */
    public GetElementPtr(String name, BasicBlock parent, Value ptrval, Value index1) {
        super(name, ptrval.getType(), parent, ptrval, index1);
        this.ptrPointingType = IrTool.getPointingTypeOfPointer(ptrval);
    }

    // %1 = getelementptr [5 x [7 x i32]], [5 x [7 x i32]]* @a, i32 0, i32 3
    // %6 = getelementptr i32, i32* %5, i32 4
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName());
        stringBuilder.append(" = getelementptr ");
        stringBuilder.append(ptrPointingType).append(", ");   // 取出的类型
        IrTool.appendSBParamList(stringBuilder, getOperands()); // 参数列表

        return stringBuilder.toString();
    }
}
