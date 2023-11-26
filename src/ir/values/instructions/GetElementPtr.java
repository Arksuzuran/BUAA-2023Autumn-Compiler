package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.values.constants.ConstInt;
import utils.IrTool;

import java.util.ArrayList;

/**
 * @Description 寻址指令 计算目标地址 对应value为指针类型
 * <result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
 * @Author
 * @Date 2023/10/30
 **/
public class GetElementPtr extends Instruction {
    /**
     * 基地址指针ptrval所指向的类型
     */
    private final ValueType ptrPointingType;

    /**
     * 双参数寻址指令 用于数组寻址
     * 所得value类型为指针 指向剥离一层[]后的基本元素类型
     * <p>
     * 例如 ptrval为[2 * [3 * i32]]*，那么返回的指针类型应该指向一维数组，即[3 * i32]*
     * 例如 ptrval为[3 * i32]*，那么返回的指针类型应该指向0维数组int，即i32*
     *
     * @param ptrval 基地址的指针。
     * @param index1 本维偏移（here * [][]）
     * @param index2 高维偏移（[here][]）
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
     *
     * @param ptrval 基地址的指针。本instruction和基地址的类型相同
     * @param index1 本维度偏移（here * [][]）
     */
    public GetElementPtr(String name, BasicBlock parent, Value ptrval, Value index1) {
        super(name, ptrval.getType(), parent, ptrval, index1);
        this.ptrPointingType = IrTool.getPointingTypeOfPointer(ptrval);
    }

    // %1 = getelementptr [5 x [7 x i32]], [5 x [7 x i32]]* @a, i32 0, i32 3
    // %6 = getelementptr i32, i32* %5, i32 4
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName());
        stringBuilder.append(" = getelementptr ");
        stringBuilder.append(ptrPointingType).append(", ");   // 取出的类型
        IrTool.appendSBParamList(stringBuilder, getOperands()); // 参数列表

        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        // 本维基地址
        Value irBase = getOp(1);
        // 本维偏移
        Value irOffset1 = getOp(2);

        // 获得数组的基地址的MipsOp
        MipsOperand base = MipsBuilder.buildOperand(irBase, false, Mc.curIrFunction, getParent());
        // 本value的MipsOp
        MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());

        // 根据操作数个数（降维次数）来讨论
        int opNumber = getOperands().size();
        // 单参数本维寻址，其类型与原指针类型相同
        if (opNumber == 2) {
            // 本维偏移
            handleDim(dst, dst, base, irOffset1, ptrPointingType, true);
        }
        // 指向数组
        else if (opNumber == 3) {
            // 获得数组元素类型/偏移Value
            Value irOffset2 = getOp(3);
            ValueType elementType = ((ArrayType) ptrPointingType).getElementType();

            // 本维偏移
            handleDim(dst, dst, base, irOffset1, ptrPointingType, false);
            // 低一维偏移
            handleDim(dst, MipsRealReg.AT, dst, irOffset2, elementType, false);
        }
    }

    /**
     * 计算某一维的偏移
     * @param dst           要将结果存入的寄存器
     * @param mid           中转寄存器
     * @param base          存有基地址的寄存器
     * @param irOffset      Offset的irValue
     * @param elementType   该维基本元素的irType
     * @param dim1Opt       是否为opNumber == 2的场合
     */
    private void handleDim(MipsOperand dst, MipsOperand mid, MipsOperand base, Value irOffset, ValueType elementType, boolean dim1Opt){
        int elementSize = elementType.getSize();

        // 为常数
        if (irOffset instanceof ConstInt) {
            int offsetIndex = IrTool.getValueOfConstInt(irOffset);
            // 低一维偏移的具体值
            int totalOffset = elementSize * offsetIndex;
            // 不存在偏移，直接将this映射到base的op即可
            if (dim1Opt && totalOffset == 0){
                Mc.addOperandMapping(this, base);
            }
            else{
                MipsOperand totalOffsetOperand = MipsBuilder.buildImmOperand(totalOffset, true, Mc.curIrFunction, getParent());
                // dst = dst + offset
                MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, totalOffsetOperand, getParent());
            }
        }
        // 非常数
        else {
            // 利用mid寄存器周转
            // mid = offset = elementSize * offset
            MipsBuilder.buildOptMul(mid, irOffset, new ConstInt(32, elementSize), Mc.curIrFunction, getParent());
            // dst = base + mid
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, mid, getParent());
        }
    }

// 非封装实现
    //            // 如果偏移是常数，则可以进行计算优化
//            if (irOffset1 instanceof ConstInt) {
//                // totalOffset = base_size1 * base_offset1
//                // 存在偏移
//                if (totalOffset1 != 0) {
//                    // offset的MipsOp
//                    MipsOperand totalOffsetOperand = MipsBuilder.buildImmOperand(totalOffset1, true, Mc.curIrFunction, getParent());
//                    // dst = base + offset
//                    MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, totalOffsetOperand, getParent());
//                }
//                // 不存在偏移，直接将this映射到base的op即可
//                else {
//                    Mc.addOperandMapping(this, base);
//                }
//            }
//            // 如果偏移是变量，那么偏移值的计算需要用到乘法
//            else {
//                // dst = offset1 = baseSize * offset1
//                MipsBuilder.buildOptMul(dst, irOffset1, new ConstInt(32, baseSize), Mc.curIrFunction, getParent());
//                // dst = base + offset
//                MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, dst, dst, base, getParent());
//            }
}
