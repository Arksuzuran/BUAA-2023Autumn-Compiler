package backend.parts;

import backend.MipsBuilder;
import backend.instructions.MipsInstruction;
import backend.operands.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsFunction {
    private String name;

    public boolean isLibFunc() {
        return isLibFunc;
    }

    private boolean isLibFunc;

    public MipsFunction(String name, boolean isLibFunc) {
        // 需要去除开头的@
        this.name = name.substring(1);
        this.isLibFunc = isLibFunc;
        this.allocaSize = 0;
    }

    /**
     * 虚拟整型寄存器
     */
    private final HashSet<MipsVirtualReg> usedVirtualRegs = new HashSet<>();
    /**
     * 函数在栈上已经分配出的空间
     * 包括 2 个部分，alloca 和 spill
     */
    private int allocaSize = 0;
    /**
     * 函数在栈上应当分配的总空间
     */
    private int totalStackSize;
    private final ArrayList<MipsBlock> blocks = new ArrayList<>();
    /**
     * 函数需要在调用前保存的寄存器
     */
    private final TreeSet<RegType> regsNeedSaving = new TreeSet<>();

    /**
     * 该函数需要使用栈上的参数的时候使用到的 mov 指令，来控制 offset
     */
    private final HashSet<MipsImm> argOffsets = new HashSet<>();

    public String getName() {
        return name;
    }

    public void addUsedVirtualReg(MipsVirtualReg MipsVirtualReg) {
        usedVirtualRegs.add(MipsVirtualReg);
    }

    public void addArgOffset(MipsImm MipsOffset) {
        argOffsets.add(MipsOffset);
    }

    public int getTotalStackSize() {
        return totalStackSize;
    }

    public HashSet<MipsVirtualReg> getUsedVirtualRegs() {
        return usedVirtualRegs;
    }

    /**
     * 在函数栈上分配出指定空间
     *
     * @param size 要分配的空间
     */
    public void addAllocaSize(int size) {
        allocaSize += size;
    }

    /**
     * 获得当前函数已经在栈上分配出的空间
     */
    public int getAllocaSize() {
        return allocaSize;
    }

    public TreeSet<RegType> getRegsNeedSaving() {
        return regsNeedSaving;
    }

    public ArrayList<MipsBlock> getMipsBlocks() {
        return blocks;
    }

    /**
     * 栈上的空间从上到下依次为：
     * 1.调用者保存的寄存器
     * 2.其他alloca
     * 3.参数alloca
     */
    public void rebuildStack() {
        // 遍历下属所有语句，记录所有用过的寄存器，作为函数调用前要保存的现场
        for (MipsBlock block : blocks) {
            for (MipsInstruction instruction : block.getInstructions()) {
                // 保存写过的寄存器(的类型)
                for (MipsOperand defReg : instruction.getDefRegs()) {
                    if (defReg instanceof MipsRealReg) {
                        RegType regType = ((MipsRealReg) defReg).getType();
                        if (RegType.regsNeedSaving.contains(regType)) {
                            regsNeedSaving.add(regType);
                        }
                    } else {
                        System.out.println("[MipsFunction] defReg中混入了非物理寄存器！");
                    }
                }
            }
        }
        // 需要分配的用于保存现场的空间
        int stackRegSize = 4 * regsNeedSaving.size();
        // 总的空间大小：alloca空间 + 保存现场的空间
        totalStackSize = stackRegSize + allocaSize;
        // 更新先前记录的 保存在栈上的参数 的位移
        for (MipsImm argOffset : argOffsets) {
            int newOffset = argOffset.getValue() + totalStackSize;
            argOffset.setValue(newOffset);
        }
    }

    // 已被遍历过的block
    private final HashSet<MipsBlock> serializedBlocks = new HashSet<>();

    /**
     * DFS所有块，构建跳转关系
     * @param curBlock     当前块
     */
    public void blockSerialize(MipsBlock curBlock) {
        // 已遍历
        serializedBlocks.add(curBlock);
        // 插入当前块
        blocks.add(curBlock);

        // 没有后继，遍历终止
        if (curBlock.getTrueSucBlock() == null && curBlock.getFalseSucBlock() == null) {
            return;
        }

        // 没有错误后继块,说明只有一个后继块，考虑与当前块合并
        if (curBlock.getFalseSucBlock() == null) {
            MipsBlock sucBlock = curBlock.getTrueSucBlock();

            // 如果后继块还未被序列化，则进行合并
            // 只需要移除当前块最后一条跳转指令
            // 然后对sucBlock进行序列化
            if (!serializedBlocks.contains(sucBlock)) {
                curBlock.removeInstruction();
                blockSerialize(sucBlock);
            }
        }
        // 如果有两个后继块
        else {
            MipsBlock trueSucBlock = curBlock.getTrueSucBlock();
            MipsBlock falseSucBlock = curBlock.getFalseSucBlock();

            // 在先前的Br翻译过程中，我们将trueBlock作为了间接跳转块，这里falseBlock即是紧随其后的块
            // 如果已经序列化，还需要增加一条 branch 指令，跳转到已经序列化的后继块上
            if (serializedBlocks.contains(falseSucBlock)) {
                MipsBuilder.buildBranch(falseSucBlock, curBlock);
            }
            // 对两个后继块进行序列化
            if (!serializedBlocks.contains(curBlock.getFalseSucBlock())) {
                blockSerialize(falseSucBlock);
            }
            if (!serializedBlocks.contains(curBlock.getTrueSucBlock())) {
                blockSerialize(trueSucBlock);
            }
        }
    }

    /**
     * 需要打印：
     * 函数 label
     * 保存被调用者寄存器
     * 移动栈指针 sp
     * 基本块的mips代码
     */
    @Override
    public String toString() {
        if (isLibFunc) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        // 非主函数需要保存寄存器
        if (!name.equals("main")) {
            // 保存现场
            int stackOffset = -4;
            for (RegType regType : regsNeedSaving) {
                // 保存位置：-stackOffset($SP)
                sb.append("\t").append("sw\t").append(regType).append(",\t")
                        .append(stackOffset).append("($sp)\n");
                // 继续向下生长
                stackOffset -= 4;
            }
        }
        // $SP = $SP - totalStackSize
        if (totalStackSize != 0) {
            sb.append("\tadd\t$sp,\t$sp,\t").append(-totalStackSize).append("\n");
        }
//        System.out.println(blocks);
        // 生成基本块的mips
        for (MipsBlock block : blocks) {
            sb.append(block);
        }

        return sb.toString();
    }
}
