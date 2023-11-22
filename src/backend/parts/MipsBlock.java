package backend.parts;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsBlock {
    private static int nameCnt = 0;
    private String name;
    /**
     * 指令列表
     */
    private final LinkedList<MipsInstruction> instructions = new LinkedList<>();

    private int loopDepth = 0;
    // 如果最后一条指令是有条件跳转指令，直接后继块。false指条件跳转中不满足条件下继续执行的基本块
    private MipsBlock falseSucBlock = null;
    // 一个基本块最多两个后继块，如果基本块只有一个后继，那么falseSucBlock是null，trueSucBlock不是null
    private MipsBlock trueSucBlock = null;
    // 前驱块
    private final ArrayList<MipsBlock> preBlocks = new ArrayList<>();

    public MipsBlock(String name, int loopDepth) {
        // 需要去除开头的b
        this.name = name.substring(1) + "_" + getNameCnt();
        this.loopDepth = loopDepth;
    }
    public static int getNameCnt(){
        return nameCnt++;
    }
    public String getName() {
        return name;
    }
    public int getLoopDepth() {
        return loopDepth;
    }

    // ===== 前驱与后继块 =======
    public void addPreBlock(MipsBlock preBlock) {
        this.preBlocks.add(preBlock);
    }
    public void removePreBlock(MipsBlock preBlock) {
        this.preBlocks.remove(preBlock);
    }
    public ArrayList<MipsBlock> getPreBlocks() {
        return preBlocks;
    }
    public void setFalseSucBlock(MipsBlock falseSucBlock) {
        this.falseSucBlock = falseSucBlock;
    }
    public void setTrueSucBlock(MipsBlock trueSucBlock) {
        this.trueSucBlock = trueSucBlock;
    }
    public MipsBlock getFalseSucBlock() {
        return falseSucBlock;
    }
    public MipsBlock getTrueSucBlock() {
        return trueSucBlock;
    }
    // ======= 指令序列 ========
    public LinkedList<MipsInstruction> getInstructions() {
        return instructions;
    }
    public void addInstruction(MipsInstruction instruction){
        instructions.add(instruction);
    }
    public void addInstructionHead(MipsInstruction instruction) {
        instructions.addFirst(instruction);
    }
    public void removeInstruction() {
        instructions.removeLast();
    }
    public MipsInstruction getTailInstruction() {
        return instructions.getLast();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 块标签
        sb.append(name).append(":\n");

        for(MipsInstruction instruction : instructions){
            sb.append("\t").append(instruction);
        }

        return sb.toString();
    }
}
