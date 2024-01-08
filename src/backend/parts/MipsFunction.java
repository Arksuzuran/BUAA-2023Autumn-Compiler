package backend.parts;

import backend.MipsBuilder;
import backend.instructions.MipsBranch;
import backend.instructions.MipsCondType;
import backend.instructions.MipsInstruction;
import backend.operands.*;
import utils.Pair;

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
//        System.out.println("重建函数栈" + getName() + ", stackRegSize:" + stackRegSize + ", allocaSize:" + allocaSize);
    }

    // 已被遍历过的block
    private final HashSet<MipsBlock> serializedBlocks = new HashSet<>();

    /**
     * DFS所有块，构建跳转关系
     * 无PHI版本
     * @param curBlock 当前块
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
            if (!serializedBlocks.contains(falseSucBlock)) {
                blockSerialize(falseSucBlock);
            }
            if (!serializedBlocks.contains(trueSucBlock)) {
                blockSerialize(trueSucBlock);
            }
        }
    }

    // TODO 以下方法涉及PHI
    /**
     * 这个函数用于处理非直接后继块（就是这个后继块我们不打算放在当前块的正后面），在这里是处理 true 后继
     * 对于这种块，我们不能把 copy 指令放到 cur 里，所以要么插入 Suc，要么再做一个块
     * 之所以不能的原因是他不会直接放在 cur 的后面
     *
     * @param curBlock  当前块
     * @param SucBlock 间接后继
     * @param phiCopys  copy
     */
    private void handleTrueCopys(MipsBlock curBlock, MipsBlock SucBlock, ArrayList<MipsInstruction> phiCopys) {
        // 如果没有 copy 的话，就不用费事了
        if (!phiCopys.isEmpty()) {
            // 如果后继块前只有一个前驱块（当前块），那么就可以直接插入到后继块的最开始
            if (SucBlock.getPreBlocks().size() == 1) {
                SucBlock.insertPhiCopysHead(phiCopys);
            }
            // 如果后继块前有多个前驱块（无法确定从哪个块来），那么就应该新形成一个块
            else {
                // 新做出一个中转块
                MipsBlock transferBlock = new MipsBlock(curBlock.getLoopDepth());

                // 把 phiMov 指令放到这里
                transferBlock.insertPhiCopysHead(phiCopys);

                // 做出一个中转块跳转到的指令
                MipsBranch transferJump = new MipsBranch(SucBlock);
                transferBlock.addInstruction(transferJump);

                // transfer 登记前驱后继
                transferBlock.setTrueSucBlock(SucBlock);
                transferBlock.addPreBlock(curBlock);

                // Suc 登记前驱后继
                SucBlock.removePreBlock(curBlock);
                SucBlock.addPreBlock(transferBlock);

                // cur 登记前驱后继
                curBlock.setTrueSucBlock(transferBlock);
                // 修改 cur 的最后一条指令
                MipsBranch tailInstr = (MipsBranch) curBlock.getTailInstruction();
                tailInstr.setTarget(transferBlock);
            }
        }
    }

    /**
     * 这里处理的是直接后继块，我们不会做一个新块，而是将 copy 直接插入 cur 的末尾
     * 如果后继还没有放置，那么就放置后继
     * 如果已经放置，那么就再做一个 jump
     *
     * @param curBlock  当前块
     * @param SucBlock 后继块
     * @param phiCopys  copy
     */
    private void handleFalseCopys(MipsBlock curBlock, MipsBlock SucBlock, ArrayList<MipsInstruction> phiCopys) {
        for (MipsInstruction phiCopy : phiCopys) {
            curBlock.addInstruction(phiCopy);
        }
        // 如果已经序列化了，那么还需要增加一条 branch 指令，跳转到已经序列化的后继块上
        if (hasSerial.contains(SucBlock)) {
            MipsBranch MipsBranch = new MipsBranch(SucBlock);
            curBlock.addInstruction(MipsBranch);
        }
    }

    /**
     * 这个函数用于交换当前块的两个后继，交换操作很简单
     * 是为了让 false 块是未序列化块的几率更大，
     * 或者让 false 与 curBlock 间有 copys (在 false 和 true 均被序列化后)
     *
     * @param curBlock     当前块
     * @param phiWaitLists phi copy
     */
    private void swapSucBlock(MipsBlock curBlock, HashMap<Pair<MipsBlock, MipsBlock>, ArrayList<MipsInstruction>> phiWaitLists) {
        MipsBlock trueSuc = curBlock.getTrueSucBlock();
        MipsBlock falseSuc = curBlock.getFalseSucBlock();
        Pair<MipsBlock, MipsBlock> falseLookUp = new Pair<>(curBlock, falseSuc);
        if (!hasSerial.contains(trueSuc) ||
                hasSerial.contains(trueSuc) && hasSerial.contains(falseSuc) && (!phiWaitLists.containsKey(falseLookUp) || phiWaitLists.get(falseLookUp).isEmpty())) {
            curBlock.setTrueSucBlock(falseSuc);
            curBlock.setFalseSucBlock(trueSuc);
            MipsBranch tailBranch = (MipsBranch) curBlock.getTailInstruction();
            MipsCondType cond = tailBranch.getCondType();
            tailBranch.setCondType(MipsCondType.getOppCondType(cond));
            // 这里注意，不能直接用 trueBlock
            tailBranch.setTarget(curBlock.getTrueSucBlock());
        }
    }

    /**
     *是一个辅助量，用于在 DFS 序列化的时候作为 visit
     */
    private final HashSet<MipsBlock> hasSerial = new HashSet<>();
    /**
     * 本质是一个 DFS
     * 当存在两个后继块的时候，优先放置 false 块
     *
     * @param curBlock     当前块
     * @param phiWaitLists 记录 phi 的表
     */
    public void blockSerialPHI(MipsBlock curBlock, HashMap<Pair<MipsBlock, MipsBlock>, ArrayList<MipsInstruction>> phiWaitLists) {
//        System.out.println("blockSerialPHI: " + curBlock.getName() + "\n[phiWaitLists]\n" + phiWaitLists);
        // 登记
        hasSerial.add(curBlock);
        // 插入当前块
        blocks.add(curBlock);

        // 没有后继，遍历终止
        if (curBlock.getTrueSucBlock() == null && curBlock.getFalseSucBlock() == null) {
            return;
        }

        // 如果没有false后继块,说明只有一个后继块，那么就应该考虑与当前块合并
        if (curBlock.getFalseSucBlock() == null) {
            MipsBlock SucBlock = curBlock.getTrueSucBlock();
            // 这个前驱后继关系用于查询有多少个 phiMove 要插入，一个后继块，直接将这些指令插入到跳转之前即可
            Pair<MipsBlock, MipsBlock> trueLookup = new Pair<>(curBlock, SucBlock);
//            System.out.println("Lookup PHIS: " + curBlock.getName() +" "+ SucBlock.getName() + "\n[phiWaitLists]\n" + phiWaitLists.getOrDefault(trueLookup, new ArrayList<>()));
            curBlock.insertPhiMovesTail(phiWaitLists.getOrDefault(trueLookup, new ArrayList<>()));

            // 合并的条件是后继块还未被序列化，此时只需要将当前块最后一条跳转指令移除掉就好了
            if (!hasSerial.contains(SucBlock)) {
                curBlock.removeInstruction();
                blockSerialPHI(SucBlock, phiWaitLists);
            }
            // 但是不一定能够被合并成功，因为又可以后继块已经被先序列化了，那么就啥都不需要干了
        }
        // 如果有两个后继块
        else {
            // 交换块的目的是让处理更加快捷
            swapSucBlock(curBlock, phiWaitLists);

            MipsBlock trueSucBlock = curBlock.getTrueSucBlock();
            MipsBlock falseSucBlock = curBlock.getFalseSucBlock();
            Pair<MipsBlock, MipsBlock> trueLookup = new Pair<>(curBlock, trueSucBlock);
            Pair<MipsBlock, MipsBlock> falseLookup = new Pair<>(curBlock, falseSucBlock);

            handleTrueCopys(curBlock, trueSucBlock, phiWaitLists.getOrDefault(trueLookup, new ArrayList<>()));

            handleFalseCopys(curBlock, falseSucBlock, phiWaitLists.getOrDefault(falseLookup, new ArrayList<>()));

            if (!hasSerial.contains(curBlock.getFalseSucBlock())) {
                blockSerialPHI(curBlock.getFalseSucBlock(), phiWaitLists);
            }
            if (!hasSerial.contains(curBlock.getTrueSucBlock())) {
                blockSerialPHI(curBlock.getTrueSucBlock(), phiWaitLists);
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
