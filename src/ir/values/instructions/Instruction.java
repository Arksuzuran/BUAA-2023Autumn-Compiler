package ir.values.instructions;

import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.User;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 指令类。本质上来说Instruction其实是各种具体指令的返回值的Value对象,因此只需要记录操作数
 * @Author
 * @Date 2023/10/30
 **/
public class Instruction extends User {

    /**
     *
     * @param name  指令Value的名称（虚拟寄存器名）
     * @param type  指令Value的类型
     * @param parent    parent一定是BasicBlock
     * @param operands  所属操作数列表
     */
    public Instruction(String name, ValueType type, BasicBlock parent, Value... operands) {
        super(name, type, parent, new ArrayList<>(){{
            addAll(List.of(operands));
        }});
    }
}
