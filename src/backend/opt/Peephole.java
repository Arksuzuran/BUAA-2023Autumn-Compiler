package backend.opt;

import backend.instructions.*;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import backend.parts.MipsModule;

import java.util.*;

/**
 * @Description 窥孔优化 对于相邻的部分少量指令进行优化
 * @Author  H1KARI
 * @Date 2023/12/10
 **/
public class Peephole {
    private final MipsModule mipsModule = MipsModule.getInstance();

    /**
     * 上一轮中有没有进行优化
     * 如果有，则表明下一轮还有可能继续优化，因此优化不会停止
     */
    private boolean hasOpt;

    /**
     * 窥孔优化入口
     */
    public void doPeephole() {
        hasOpt = true;
        while (hasOpt) {
            hasOpt = false;
            // 以基本块为单位进行优化
            // 对于块内的指令序列，分别用不同的方式进行优化
            for(MipsFunction function : mipsModule.getFunctions()){
                for(MipsBlock block : function.getMipsBlocks()){
                    optAddSub(block);
                    optMoveToSelf(block);
                    optMoveCover(block);
                    optStoreLoad(block);
                }
            }
        }
    }

    /**
     * 处理无意义的加减法，即对于如下形式的指令：
     * add/sub r0, r0, 0 -> del
     * add/sub r0, r1, 0 -> move r0, r1
     */
    private void optAddSub(MipsBlock block) {
        // 需要避免遍历时删除，因此我们可以直接新建一个指令列表，修改后再放回
        LinkedList<MipsInstruction> newInstructions = new LinkedList<>();
        // 遍历指令序列
        for (MipsInstruction instruction : block.getInstructions()){
            // 是否进行了优化。如果进行了，则当场插入新指令。如果没有进行，则在最后会进行插入
            boolean opt = false;

            if(instruction instanceof MipsBinary){
                MipsBinary.BinaryType type = ((MipsBinary) instruction).getType();
                // 加法或减法
                if(type == MipsBinary.BinaryType.SUBU || type == MipsBinary.BinaryType.ADDU){
                    MipsOperand src2 = instruction.getSrc(2);
                    // 且最后一个操作数是0
                    if(src2 instanceof MipsImm && ((MipsImm) src2).getValue() == 0){
                        MipsOperand dst = instruction.getDst();
                        MipsOperand src1 = instruction.getSrc(1);
                        opt = true;
                        // dst和src1不同
                        // add/sub r0, r1, 0 -> move r0, r1
                        if(!dst.equals(src1)){
                            MipsMove move = new MipsMove(dst, src1);
                            newInstructions.add(move);
                        }
                        // add/sub r0, r0, 0 -> del, 直接不做插入即可，相当于删除
                    }
                }
            }
            // 如果没有进行优化，那么原样插入
            if(!opt){
                newInstructions.add(instruction);
            }
            // 否则则记录已经优化了的事实
            else{
                hasOpt = true;
            }
        }
        // 最后将基本块的指令列表替换为新的指令列表
        block.setInstructions(newInstructions);
    }

    /**
     * 处理无意义的mov，即对于如下形式的指令：
     * mov r0, r0 -> del
     */
    private void optMoveToSelf(MipsBlock block) {
        // 需要避免遍历时删除，因此我们可以直接新建一个指令列表，修改后再放回
        LinkedList<MipsInstruction> newInstructions = new LinkedList<>();
        // 遍历指令序列
        for (MipsInstruction instruction : block.getInstructions()){
            // 是否进行了优化。如果进行了，则当场插入新指令。如果没有进行，则在最后会进行插入
            boolean opt = false;
            // move指令，且dst == src
            if(instruction instanceof MipsMove){
                MipsOperand dst = instruction.getDst();
                MipsOperand src = instruction.getSrc(1);
                // 直接不做插入即可，相当于删除
                if(dst.equals(src)){
                    opt = true;
                }
            }
            // 如果没有进行优化，那么原样插入
            if(!opt){
                newInstructions.add(instruction);
            }
            // 否则则记录已经优化了的事实
            else{
                hasOpt = true;
            }
        }
        // 最后将基本块的指令列表替换为新的指令列表
        block.setInstructions(newInstructions);
    }

    /**
     * 处理对同一个dst的连续mov，即对于如下形式的指令：
     * mov r0, r1
     * mov r0, r2
     * ...
     * mov r0, rk
     * -> mov r0, rk
     * 在前一步中，我们已经消除了move r0, r0的情况，因此这里可以放心地直接保留最后一条move指令
     */
    private void optMoveCover(MipsBlock block) {
        // 需要避免遍历时删除，因此我们可以直接新建一个指令列表，修改后再放回
        LinkedList<MipsInstruction> newInstructions = new LinkedList<>();
        LinkedList<MipsInstruction> oldInstructions = block.getInstructions();
        int len = oldInstructions.size();
        // 遍历指令序列
        for(int i = 0; i < len; i++){
            // 是否进行了优化。如果进行了，则当场插入新指令。如果没有进行，则在最后会进行插入
            boolean opt = false;

            MipsInstruction instruction = oldInstructions.get(i);
            // i < len - 1时才可能进行优化
            if(i < len - 1){
                // 找出一堆连续的，dst相同的，move指令
                if(instruction instanceof MipsMove){
                    // q是最后一个满足dst(q) == dst(i)的move指令的下标
                    int q = i + 1;
                    MipsOperand dst = oldInstructions.get(i).getDst();
                    MipsInstruction tail = oldInstructions.get(q);
                    while(q < len && tail instanceof MipsMove && tail.getDst().equals(dst)){
                        q++;
                        tail = oldInstructions.get(q);
                    }
                    q--;    // q超量了1
                    // q > i，则至少找到了一对儿
                    if(q > i){
                        opt = true;
                        // 仅需插入最后一条move即可
                        newInstructions.add(oldInstructions.get(q));
                        // 注意！在优化了的场合，i也应当更新，因为从i到q-1的所有指令都被删除了，只留下了q
                        // 循环结束时还有i++
                        i = q;
                    }
                }
            }
            // 如果没有进行优化，那么原样插入
            if(!opt){
                newInstructions.add(instruction);
            }
            // 否则则记录已经优化了的事实
            else{
                hasOpt = true;
            }
        }
        // 最后将基本块的指令列表替换为新的指令列表
        block.setInstructions(newInstructions);
    }

    /**
     * 处理向同一个地址的存储+加载的store+load对，即对于如下形式的指令：
     *   store a, memory
     *   load b, sameMemory
     *   ->
     *   move b, a
     */
    private void optStoreLoad(MipsBlock block) {
        // 需要避免遍历时删除，因此我们可以直接新建一个指令列表，修改后再放回
        LinkedList<MipsInstruction> newInstructions = new LinkedList<>();
        LinkedList<MipsInstruction> oldInstructions = block.getInstructions();
        int len = oldInstructions.size();
        // 两个两个地遍历指令序列
        for (int i = 0; i < len; i++) {
            // 是否进行了优化。如果进行了，则当场插入新指令。如果没有进行，则在最后会进行插入
            boolean opt = false;

            // i < len - 1时才可能进行优化
            if(i < len - 1){
                MipsInstruction instruction1 = oldInstructions.get(i);
                MipsInstruction instruction2 = oldInstructions.get(i + 1);
                // store + load
                if (instruction1 instanceof MipsStore && instruction2 instanceof MipsLoad) {
                    MipsOperand addr1 = ((MipsStore) instruction1).getBase();
                    MipsOperand offset1 = ((MipsStore) instruction1).getOffset();
                    MipsOperand addr2 = ((MipsLoad) instruction2).getBase();
                    MipsOperand offset2 = ((MipsLoad) instruction2).getOffset();
                    // 地址完全相同
                    if (addr1.equals(addr2) && offset1.equals(offset2)) {
                        opt = true;
                        MipsOperand src = ((MipsStore) instruction1).getSrc();
                        MipsOperand dst = instruction2.getDst();
                        MipsMove move = new MipsMove(dst, src);
                        newInstructions.add(move);
                        // 注意！在优化了的场合，i也应当更新，因为i和i+1都被移除了，换为了move
                        // i := i + 1
                        // 循环结束时还有i++
                        i++;
                    }
                }
            }
            // 如果没有进行优化，那么原样插入
            if (!opt) {
                newInstructions.add(oldInstructions.get(i));
            }
            // 否则则记录已经优化了的事实
            else {
                hasOpt = true;
            }
        }
        // 最后将基本块的指令列表替换为新的指令列表
        block.setInstructions(newInstructions);
    }
}
