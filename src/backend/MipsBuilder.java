package backend;

import backend.instructions.MipsLoad;
import backend.instructions.MipsMove;
import backend.operands.*;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import utils.MipsTool;

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

    /**
     * 构建move指令
     * @param dst
     * @param src
     * @param irBlock
     * @return
     */
    public static MipsMove buildMove(MipsOperand dst, MipsOperand src, BasicBlock irBlock){
        MipsMove move = new MipsMove(dst, src);
        Mc.b(irBlock).addInstruction(move);
        return move;
    }

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
            // 不需要立即数，而解析过的op是立即数，则需要move到寄存器内
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
            // 需要的就是立即数
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
        if(isImm && MipsTool.is16BitImm(imm, true)){
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
                MipsMove move = new MipsMove(vr, mipsImm);
                Mc.b(irBlock).addInstruction(move);
                return vr;
            }
        }
    }

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
    private static MipsVirtualReg allocVirtualReg(Function irFunction){
        MipsVirtualReg vr = new MipsVirtualReg();
        Mc.f(irFunction).addUsedVirtualReg(vr);
        return vr;
    }
}
