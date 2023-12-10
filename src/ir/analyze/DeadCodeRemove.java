package ir.analyze;

import config.Config;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.instructions.Br;
import ir.values.instructions.Instruction;
import ir.values.instructions.Ret;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Description 删除基本块中，ret和Br后面的死代码
 * @Author
 * @Date 2023/12/11
 **/
public class DeadCodeRemove {

    public static void analyze() {
        ArrayList<Function> functions = Module.getFunctions();
        for (Function function : functions) {
            if (!function.isLibFunc()) {
                for(BasicBlock block : function.getBasicBlocks()){
                    delDeadCode(block);
                }
            }
        }
    }
    public static void delDeadCode(BasicBlock block){
        LinkedList<Instruction> instructions = new LinkedList<>(block.getInstructions());
        boolean flag = false;
        for(Instruction instruction : instructions){
            if(flag){
                instruction.dropAllOperands();
                instruction.eraseFromParent();
            }
            else if(instruction instanceof Ret || instruction instanceof Br){
                flag = true;
            }
        }
    }
}
