package ir.values;

import ir.types.LabelType;
import ir.types.ValueType;
import ir.values.instructions.Instruction;

import java.util.LinkedList;

/**
 * @Description 基本块
 * @Author  H1KARI
 * @Date 2023/10/30
 **/
public class BasicBlock extends Value{
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
        super("block" + name, new LabelType(), parent);
    }

    /**
     * 在指令序列末尾添加一条指令
     * @param instruction   要添加的指令
     */
    public void addInstruction(Instruction instruction){
        instructions.add(instruction);
    }

    /**
     * 在指令序列头部添加一条指令
     * @param instruction   要添加的指令
     */
    public void addInstructionAtHead(Instruction instruction){
        instructions.addFirst(instruction);
    }
}
