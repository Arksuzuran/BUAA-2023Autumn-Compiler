package ir.values;

import ir.analyze.Loop;
import ir.types.LabelType;
import ir.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @Description 基本块
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class BasicBlock extends Value{
    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }

    /**
     * 指令序列
     */
    private final LinkedList<Instruction> instructions = new LinkedList<>();

    /**
     * 构造函数
     * @param name      基本块名称
     * @param parent    所属的Function
     */
    public BasicBlock(String name, Value parent) {
        super("%b" + name, new LabelType(), parent);
    }

    /**
     * 返回该基本块所属的函数
     */
    public Function getParentFunction(){
        return (Function) super.getParent();
    }

    /**
     * 在指令序列末尾添加一条指令
     * @param instruction   要添加的指令
     */
    public void addInstruction(Instruction instruction){
        instructions.add(instruction);
    }

    public void delInstruction(Instruction instruction) {
        instructions.remove(instruction);
    }
    public void delAllInstruction() {
        instructions.clear();
    }
    /**
     * 在指令序列头部添加一条指令
     * @param instruction   要添加的指令
     */
    public void addInstructionAtHead(Instruction instruction){
        instructions.addFirst(instruction);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName().substring(1)).append(":\n");
        for (Instruction instruction : instructions){
            stringBuilder.append("\t").append(instruction).append("\n");
        }
//        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    @Override
    public void buildMips(){
        for(Instruction instruction : instructions){
            instruction.buildMips();
        }
    }

    // =========== IR analyze ===============
    /**
     * 前驱与后继块
     */
    private final HashSet<BasicBlock> preBlocks = new HashSet<>();
    private final HashSet<BasicBlock> sucBlocks = new HashSet<>();
    public void addPreBlock(BasicBlock preBlock) {
        preBlocks.add(preBlock);
    }
    public void addSucBlock(BasicBlock sucBlock) {
        sucBlocks.add(sucBlock);
    }
    public HashSet<BasicBlock> getSucBlocks() {
        return sucBlocks;
    }
    public HashSet<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    // =========================================
    /**
     * 支配者块
     */
    private final ArrayList<BasicBlock> domers = new ArrayList<>();
    /**
     * 直接支配的基本块
     */
    private final ArrayList<BasicBlock> idomees = new ArrayList<>();
    /**
     * 直接支配基本块
     */
    private BasicBlock Idomer;
    /**
     * 在支配树中的深度
     */
    private int domLevel;

    /**
     * 支配边际，即刚好不被当前基本块支配的基本块
     */
    private final HashSet<BasicBlock> dominanceFrontier = new HashSet<>();
    public ArrayList<BasicBlock> getDomers()
    {
        return domers;
    }
    /**
     * 当前块是否是另一个块的支配者
     */
    public boolean isDominating(BasicBlock other) {
        return other.domers.contains(this);
    }

    public ArrayList<BasicBlock> getIdomees()
    {
        return idomees;
    }

    public void setIdomer(BasicBlock idomer)
    {
        Idomer = idomer;
    }

    public void setDomLevel(int domLevel) {
        this.domLevel = domLevel;
    }

    public int getDomLevel()
    {
        return domLevel;
    }

    public HashSet<BasicBlock> getDominanceFrontier() {
        return dominanceFrontier;
    }
    public BasicBlock getIdomer()
    {
        return Idomer;
    }

    // =========================================
    /**
     * 当前块直属的循环
     */
    private Loop loop = null;
    /**
     * 获得循环深度
     * 如果不在循环中，则深度为 1
     * @return 循环深度
     */
    public int getLoopDepth() {
        if (loop == null) {
            return 0;
        }
        return loop.getLoopDepth();
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }

    public Loop getLoop() {
        return loop;
    }
}
