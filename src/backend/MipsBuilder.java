package backend;

import backend.instructions.*;
import backend.operands.*;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import utils.MipsMath;
import utils.Pair;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsBuilder {
    private Module irModule;

    public MipsBuilder(Module irModule) {
        this.irModule = irModule;
    }

    public void doMipsBuilding(){
        irModule.buildMips();
    }

    // ================= 指令的简单构造方法 ===================
    // 主要作用是构建指令并将其挂载在对应block下
    /**
     * 构建move指令
     */
    public static MipsMove buildMove(MipsOperand dst, MipsOperand src, BasicBlock irBlock){
        MipsMove move = new MipsMove(dst, src);
        Mc.b(irBlock).addInstruction(move);
        return move;
    }

    /**
     * 构建双操作数指令
     * @param type  指令类型
     */
    public static MipsBinary buildBinary(MipsBinary.BinaryType type, MipsOperand dst, MipsOperand src1, MipsOperand src2, BasicBlock irBlock){
        MipsBinary binary = new MipsBinary(type, dst, src1, src2);
        Mc.b(irBlock).addInstruction(binary);
        return binary;
    }

    /**
     * 构建位移指令
     * @param type  指令类型，逻辑左/右，算数右
     */
    public static MipsShift buildShift(MipsShift.ShiftType type, MipsOperand dst, MipsOperand src1, int shift, BasicBlock irBlock){
        MipsShift shift1 = new MipsShift(type, dst, src1, shift);
        Mc.b(irBlock).addInstruction(shift1);
        return shift1;
    }

    /**
     * 构建HI寄存器的存取指令
     * @param type  MTHI 或 MFHI
     */
    public static MipsMoveHI buildMoveHI(MipsMoveHI.MoveHIType type, MipsOperand op, BasicBlock irBlock){
        MipsMoveHI moveHI;
        // MTHI: dst为null (HI)
        if(type == MipsMoveHI.MoveHIType.MTHI){
            moveHI = new MipsMoveHI(type, null, op);
        }
        // MFHI: src为null (HI)
        else {
            moveHI = new MipsMoveHI(type, op, null);
        }
        Mc.b(irBlock).addInstruction(moveHI);
        return moveHI;
    }

    /**
     * 构建有条件跳转的分支指令
     */
    public static MipsBranch buildBranch(MipsCondType condType, MipsOperand src1, MipsOperand src2, MipsBlock target, BasicBlock irBlock){
        MipsBranch branch = new MipsBranch(condType, src1, src2, target);
        Mc.b(irBlock).addInstruction(branch);
        return branch;
    }

    /**
     * 构建无条件跳转的分支指令
     */
    public static MipsBranch buildBranch(MipsBlock target, BasicBlock irBlock){
        MipsBranch branch = new MipsBranch(target);
        Mc.b(irBlock).addInstruction(branch);
        return branch;
    }

    /**
     * 构建比较指令
     */
    public static MipsCompare buildCompare(MipsCondType condType, MipsOperand dst, MipsOperand src1, MipsOperand src2, BasicBlock irBlock){
        MipsCompare compare = new MipsCompare(condType, dst, src1, src2);
        Mc.b(irBlock).addInstruction(compare);
        return compare;
    }

    /**
     * 构建存储字指令，将字从寄存器存储到内存的指定位置
     * @param src       保存有要存储的字 的寄存器
     * @param base      目的内存的基地址
     * @param offset    目的内存相对于基地址的偏移
     */
    public static MipsStore buildStore(MipsOperand src, MipsOperand base, MipsOperand offset, BasicBlock irBlock){
        MipsStore store = new MipsStore(src, base, offset);
        Mc.b(irBlock).addInstruction(store);
        return store;
    }

    /**
     * 构建加载字指令，将字从内存指定位置加载到寄存器dst
     * @param dst           要加载到的寄存器
     * @param base          目的内存的基地址
     * @param offset        目的内存相对于基地址的偏移
     */
    public static MipsLoad buildLoad(MipsOperand dst, MipsOperand base, MipsOperand offset, BasicBlock irBlock){
        MipsLoad load = new MipsLoad(dst, base, offset);
        Mc.b(irBlock).addInstruction(load);
        return load;
    }

    /**
     * 构建返回指令
     * @param function  从该函数返回
     */
    public static MipsRet buildRet(Function function, BasicBlock irBlock){
        MipsRet ret = new MipsRet(Mc.f(function));
//        ret.addUseReg(null, MipsRealReg.V0);
        Mc.b(irBlock).addInstruction(ret);
        return ret;
    }


    // ================== 操作数的分析方法 ====================
    // 主要作用是给定irValue，将其转换为我们需要的MipsOperand类型操作数
    /**
     * MipsOperand的工厂模式方法
     * 将irValue转换为MipsOperand对象
     * @param irValue       irValue对象
     * @param isImm         当前是否需要立即数类型的op
     * @return              生成的MIPSOperand
     */
    public static MipsOperand buildOperand(Value irValue, boolean isImm, Function irFunction, BasicBlock irBlock){
        MipsOperand op = Mc.op(irValue);
        // 如果该ir已经被解析过了
        if(op != null){
            // 不需要立即数，且解析过的op是立即数，则需要move到寄存器内
            if(!isImm && op instanceof MipsImm){
                // 0
                if(((MipsImm) op).getValue() == 0){
                    return MipsRealReg.ZERO;
                }
                // 非0，move
                else{
                    MipsOperand tmpVR = allocVirtualReg(irFunction);
                    buildMove(tmpVR, op, irBlock);
                    return tmpVR;
                }
            }
            else{
                return op;
            }
        }
        // 该ir没有被解析过, 则根据其类型进行解析
        else{
            // 函数形参
            if(irValue.isArg() && irFunction.getArgValues().contains(irValue)){
                return buildArgOperand(irValue, irFunction);
            }
            // 全局变量
            else if(irValue instanceof GlobalVariable){
                return buildGlobalOperand((GlobalVariable) irValue, irFunction, irBlock);
            }
            // 如果是整型常数
            else if (irValue instanceof ConstInt) {
                return buildImmOperand(((ConstInt) irValue).getValue(), isImm, irFunction, irBlock);
            }
            // 如果是指令，那么需要生成一个目的寄存器
            else {
                return allocVirtualReg(irValue, irFunction);
            }
        }
    }

    /**
     * 根据irValue 生成MipsOperand对象
     * @param irArg         参数的Value对象
     * @param irFunction
     * @return
     */
    public static MipsOperand buildArgOperand(Value irArg, Function irFunction){
        MipsFunction mipsFunction = Mc.f(irFunction);
        int argNumber = irArg.getArgNumber();   // 第几个参数
        MipsBlock firstBlock = Mc.b(irFunction.getHeadBlock()); // 头块

        // 分配虚拟寄存器, 构建映射
        MipsVirtualReg vr = allocVirtualReg(irArg, irFunction);

        // 存入a0-a3
        if (argNumber < 4) {
            // 创建move指令，移动至寄存器$ai，该指令应当放到函数头部的基本块内
            MipsMove move = new MipsMove(vr, new MipsRealReg("a" + argNumber));
            firstBlock.addInstructionHead(move);
        }
        // 存入栈上
        else {
            // 记录栈偏移量，这并不是真实的偏移值，后续会在function内进行修改
            int stackPos = argNumber - 4;
            MipsImm offset = new MipsImm(stackPos * 4);
            mipsFunction.addArgOffset(offset);
            // 创建加载指令
            MipsLoad load = new MipsLoad(vr, MipsRealReg.SP, offset);
            // 插入函数的头部块
            firstBlock.addInstructionHead(load);
        }
        return vr;
    }

    public static MipsOperand buildGlobalOperand(GlobalVariable irValue, Function irFunction, BasicBlock irBlock){
        MipsBlock objBlock = Mc.b(irBlock);
        MipsVirtualReg dst = allocVirtualReg(irFunction);
        // move指令
        MipsMove move = new MipsMove(dst, new MipsLabel(irValue.getNameCnt()));
        objBlock.addInstructionHead(move);
        return dst;
    }

    /**
     * 根据给定的(ir)立即数创建对象
     * @param imm           立即数的值
     * @param isImm         是否要创建Mips的立即数对象
     */
    public static MipsOperand buildImmOperand(int imm, boolean isImm, Function irFunction, BasicBlock irBlock){
        MipsImm mipsImm = new MipsImm(imm);
        // 允许返回立即数，则返回立即数对象
        if(isImm && MipsMath.is16BitImm(imm, true)){
            return mipsImm;
        }
        // 不允许返回立即数，那么返回寄存器
        else{
            // 立即数为0，返回0寄存器
             if(imm == 0){
                return MipsRealReg.ZERO;
            }
            // 非0， 需要使用li指令进行加载, 返回加载完成后的那个寄存器
            else{
                MipsVirtualReg vr = allocVirtualReg(irFunction);
                buildMove(vr, mipsImm, irBlock);
                return vr;
            }
        }
    }

    // =============== 虚拟寄存器生成 ================
    /**
     * 生成一个虚拟寄存器，并绑定到对应的MipsFunction里
     * 该寄存器与Value存在映射，记录该映射
     */
    public static MipsVirtualReg allocVirtualReg(Value irValue, Function irFunction){
        MipsVirtualReg vr = allocVirtualReg(irFunction);
        if(irValue != null){
            Mc.addOperandMapping(irValue, vr);
        }
        return vr;
    }

    /**
     * 生成一个虚拟寄存器，并绑定到对应的MipsFunction里
     * @return  虚拟寄存器
     */
    public static MipsVirtualReg allocVirtualReg(Function irFunction){
        MipsVirtualReg vr = new MipsVirtualReg();
        Mc.f(irFunction).addUsedVirtualReg(vr);
        return vr;
    }

    // ================== 构建手动实现的优化乘法 =======================
    public static void buildOptMul(MipsOperand dst, Value op1, Value op2, Function irFunction, BasicBlock irBlock){
        Boolean isOp1ConstInt = op1 instanceof ConstInt;
        Boolean isOp2ConstInt = op2 instanceof ConstInt;

        MipsOperand src1, src2;
        // 如果有常数，那么可以尝试进行优化
        if ( isOp1ConstInt || isOp2ConstInt) {
            int imm;
            // 取出常数，送入imm
            if (isOp1ConstInt) {
                src1 = buildOperand(op2, false, Mc.curIrFunction, irBlock);
                imm = ((ConstInt) op1).getValue();
            }
            // op1非ConstInt
            else {
                src1 = buildOperand(op1, false, Mc.curIrFunction, irBlock);
                imm = ((ConstInt) op2).getValue();
            }
            // 根据常数imm获取优化操作序列
            ArrayList<Pair<Boolean, Integer>> mulOptItems = MipsMath.getMulOptItems(imm);
            // 无法优化
            if (mulOptItems == null) {
                // 此时src2应该是已经确定为常数的op1或op2
                if (isOp1ConstInt) {
                    src2 = buildOperand(op1, false, Mc.curIrFunction, irBlock);
                } else {
                    src2 = buildOperand(op2, false, Mc.curIrFunction, irBlock);
                }
            }
            // 可以优化
            else {
                if (mulOptItems.size() == 1) {
                    doOptMulStep1(mulOptItems.get(0), dst, src1, irBlock);
                }
                else {
                    MipsOperand at = MipsRealReg.AT;
                    doOptMulStep1(mulOptItems.get(0), at, src1, irBlock);
                    // 中间运算的结果存储在 at 中
                    for (int i = 1; i < mulOptItems.size() - 1; i++) {
                        doOptMulStep(mulOptItems.get(i), at, at, src1, irBlock);
                    }
                    doOptMulStep(mulOptItems.get(mulOptItems.size() - 1), dst, at, src1, irBlock);
                }
                // 至此优化已经完成，dst内存入了结果，不必再向下进行
                return;
            }
        }
        // 没有常数，无法进行优化
        else {
            src1 = buildOperand(op1, false, Mc.curIrFunction, irBlock);
            src2 = buildOperand(op2, false, Mc.curIrFunction, irBlock);
        }
        // 对于优化失败的场合，直接构造乘法即可
        buildBinary(MipsBinary.BinaryType.MUL, dst, src1, src2, irBlock);
    }

    /**
     * 进行乘法累加位移优化的中间步骤
     * 具体来说，tmp = src1 << mulOptItems.get(0).getSecond()
     *          dst = tmpDst + tmp(optItem.getFirst())
     *              = tmpDst - tmp(!optItem.getFirst())
     * @param optItem   累加优化的信息
     * @param dst       要累加到的寄存器
     * @param tmpDst    上一步结果暂存到的寄存器
     * @param src1      乘法的第一操作数
     */
    private static void doOptMulStep(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand tmpDst, MipsOperand src1, BasicBlock irBlock){
        if (optItem.getSecond() == 0) {
            // at
            if(optItem.getFirst()){
                buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, src1, irBlock);
            } else{
                buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, src1, irBlock);
            }
        }
        // 需要位移
        else {
            // 生成周转用的虚拟寄存器 tmp = src1 << mulOptItems.get(0).getSecond()
            MipsOperand tmp = allocVirtualReg(Mc.curIrFunction);
            buildShift(MipsShift.ShiftType.SLL, tmp, src1, optItem.getSecond(), irBlock);
            if(optItem.getFirst()){
                buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, tmp, irBlock);
            } else{
                buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, tmp, irBlock);
            }
        }
    }

    /**
     * 进行累加位移优化的第一步
     * 具体来说    dst =   src1 << mulOptItems.get(0).getSecond()(optItem.getFirst())
     *               = - src1 << mulOptItems.get(0).getSecond() (!optItem.getFirst())
     */
    private static void doOptMulStep1(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand src1, BasicBlock irBlock){
        // dst = src1 << mulOptItems.get(0).getSecond()
        buildShift(MipsShift.ShiftType.SLL, dst, src1, optItem.getSecond(), irBlock);
        // dst = -dst
        if (!optItem.getFirst()) {
            buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsRealReg.ZERO, dst, irBlock);
        }
    }
}
