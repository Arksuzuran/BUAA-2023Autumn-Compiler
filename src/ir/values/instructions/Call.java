package ir.values.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.*;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;
import utils.IrTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 函数调用指令
 * <result> = call [ret attrs] <ty> <fnptrval>(<function args>)
 * @Author
 * @Date 2023/10/30
 **/
public class Call extends Instruction {

    private Function function;
    /**
     * call指令的ValueType为函数返回值的ValueType
     * @param parent   一定是Block
     * @param function op1 函数对象
     * @param rArgs    op2 3 ...函数的实参
     */
    public Call(String name, BasicBlock parent, Function function, ArrayList<Value> rArgs) {
        super(name, function.getReturnType(), parent, new ArrayList<Value>() {{
            add(function);
            addAll(rArgs);
        }}.toArray(new Value[0]));
        this.function = function;
    }

    /**
     * 注意！因为phi的处理，这里rArgs随时会有变化，因此不再记录rArgs，而是去父类的op里找
     * @return call指令传递给函数的参数
     */
    public ArrayList<Value> getArgs() {
        ArrayList<Value> args = new ArrayList<>();
        for (int i = 0; i < ((Function) getOp(1)).getArgValues().size(); i++) {
            args.add(getOp(i + 2));
        }
        return args;
    }
    //  %7 = call i32 @aaa(i32 %5, i32 %6)
    //  call void @putint(i32 %7)
    @Override
    public String toString() {
        ArrayList<Value> ops = new ArrayList<>(getOperands());
        Function function = (Function) ops.get(0);
        StringBuilder stringBuilder = new StringBuilder();
        // int返回值
        if (!(function.getReturnType() instanceof VoidType)) {
            stringBuilder.append(getName()).append(" = ");
        }
        stringBuilder.append("call ").append(function.getReturnType()).append(" ").append(function.getName()).append("(");
        // 实参列表
        if (ops.size() >= 1) {
            ops.remove(0);
            IrTool.appendSBParamList(stringBuilder, ops);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        MipsBlock mipsBlock = Mc.b(getParent());
        MipsFunction mipsFunction = Mc.f(function);
        // 先构建出call指令，后续要记录该指令用到的A寄存器
        // ！这也是唯一一次使用野生未封装的new MipsInstruction
        MipsInstruction call;
        // 内建函数，需要宏调用
        if (function.isLibFunc()) {
            call = new MipsMacro(mipsFunction.getName());
            // 系统调用必然改变 v0, v0加入def
            call.addDefReg(MipsRealReg.V0); // TODO: addDefReg 双参数修改为单参数
        }
        // 非内建函数，直接构建jal指令即可
        else {
            call = new MipsCall(mipsFunction);
        }

        // 进行传参, 遍历所有irValue参数
        int argc = getArgs().size();
        for (int i = 0; i < argc; i++) {
            Value irArg = getArgs().get(i);
            MipsOperand src;
            // 前四个参数存储在a0-3内
            if (i < 4) {
                src = MipsBuilder.buildOperand(irArg, true, Mc.curIrFunction, getParent());
                MipsMove move = MipsBuilder.buildMove(new MipsRealReg("a" + i), src, getParent());
                // 加入use，保护寄存器分配时不消除move
                call.addUseReg(move.getDst());
            }
            // 后面的参数先存进寄存器里，再store进内存
            else {
                // 要求存入寄存器
                src = MipsBuilder.buildOperand(irArg, false, Mc.curIrFunction, getParent());
                // 存入 SP - 4 * nowNum 处
                MipsImm offsetOperand = new MipsImm(-(argc - i) * 4);
                MipsBuilder.buildStore(src, MipsRealReg.SP, offsetOperand, getParent());
            }
        }

        // 栈的生长
        if (argc > 4) {
            // 向下生长4 * allNum: SP = SP - 4 * allNum
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }

        // 参数准备妥当后，再执行jal指令
        mipsBlock.addInstruction(call);

        // 这条语句执行完成的场合，恰是从函数中返回
        // 栈的恢复 与生长相反，做加法即可
        if (argc > 4) {
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }

        // 因为寄存器分配是以函数为单位的，所以相当于 call 指令只需要考虑在调用者函数中的影响
        // 那么 call 对应的 bl 指令会修改 lr 和 r0 (如果有返回值的话)
        // 此外，r0 - r3 是调用者保存的寄存器，这会导致可能需要额外的操作 mov ，所以这边考虑全部弄成被调用者保存
        for (int i = 0; i < 4; i++) {
            call.addDefReg(new MipsRealReg("a" + i));
        }
        // 非内建函数需要保存返回地址 ra
        if (!function.isLibFunc()) {
            call.addDefReg(MipsRealReg.RA);
        }
        // 处理返回值
        // 调用者应当保存 v0，无论有没有返回值
        ValueType returnType = function.getReturnType();
        call.addDefReg(MipsRealReg.V0);
        // 带有返回值，则需要记录该返回值
        if (!(returnType instanceof VoidType)) {
            MipsOperand dst = MipsBuilder.buildOperand(this, false, Mc.curIrFunction, getParent());
            MipsBuilder.buildMove(dst, MipsRealReg.V0, getParent());
        }
    }
}
