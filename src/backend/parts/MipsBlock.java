package backend.parts;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;

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
    private final ArrayList<MipsInstruction> instructions = new ArrayList<>();

    private int loopDepth = 0;

    public MipsBlock(String name, int loopDepth) {
        this.name = name + "_" + getNameCnt();
        this.loopDepth = loopDepth;
    }
    public void addInstruction(MipsInstruction instruction){
        instructions.add(instruction);
    }

    public String getName() {
        return name;
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    public int getLoopDepth() {
        return loopDepth;
    }
    public static int getNameCnt(){
        return nameCnt++;
    }
}
