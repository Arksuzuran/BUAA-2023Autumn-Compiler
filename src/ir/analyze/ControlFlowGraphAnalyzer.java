package ir.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;
import ir.values.instructions.Br;
import ir.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @Description 控制流图分析
 * 控制流的产生源于块之间的跳转关系，即末尾的Br指令
 * 透过Br指令中带有的跳转参数，即可构筑各个块之间的跳转关系
 * @Author H1KARI
 * @Date 2023/11/18
 **/
public class ControlFlowGraphAnalyzer {
    /**
     * 入口方法 开始进行分析
     */
    public static void analyze() {
        ArrayList<Function> functions = Module.getFunctions();
        for (Function function : functions) {
            if (!function.isLibFunc()) {
                buildBlockPreAndSuc(function);
            }
        }
    }

    /**
     * 构建指定函数的控制流
     */
    public static void buildBlockPreAndSuc(Function function) {
        // 重置函数内 所有块的前驱和后继关系
//        for(BasicBlock block : function.getBasicBlocks()){
//            block.getSucBlocks().clear();
//            block.getPreBlocks().clear();
//        }
        // 从首个块开始dfs，因为该dfs一定可以到达所有的控制块（仅考虑连通性）
        BasicBlock entryBlock = function.getHeadBlock();
        dfsBlock(entryBlock);
        // 删除无用的基本块
        delUnreachableBlock(function, entryBlock);
    }

    private static final HashSet<BasicBlock> visited = new HashSet<>();

    /**
     * DFS
     * 构建指定块的控制流
     */
    private static void dfsBlock(BasicBlock curBlock) {
        visited.add(curBlock);
        Instruction instruction = curBlock.getInstructions().getLast();
        BasicBlock sucBlock;
        if (instruction instanceof Br br) {
            if (br.isConditional()) {
                // true
                sucBlock = (BasicBlock) br.getOp(2);
                addEdgeAndVisit(curBlock, sucBlock);
                // false
                sucBlock = (BasicBlock) br.getOp(3);
                addEdgeAndVisit(curBlock, sucBlock);
            } else {
                sucBlock = (BasicBlock) br.getOp(1);
                addEdgeAndVisit(curBlock, sucBlock);
            }
        }
    }

    /**
     * 确立前驱和后继的关系，并尝试访问后继
     */
    private static void addEdgeAndVisit(BasicBlock preBlock, BasicBlock sucBlock) {
        preBlock.addSucBlock(sucBlock);
        sucBlock.addPreBlock(preBlock);
        if (!visited.contains(sucBlock)) {
            dfsBlock(sucBlock);
        }
    }

    /**
     * 清除指定函数内不可到达的基本块
     */
    private static void delUnreachableBlock(Function function, BasicBlock entryBlock) {
        ArrayList<BasicBlock> newBlocks = new ArrayList<>();

        // 遍历所有基本块
        for (BasicBlock block : function.getBasicBlocks()) {
            // 找到了没有前驱且不是入口的基本块
            if (block.getPreBlocks().isEmpty() && block != entryBlock) {
                // 删除其后继结点与自己的关系
                for (BasicBlock sucBlock : block.getSucBlocks()) {
                    sucBlock.getPreBlocks().remove(block);
                }
                // 清除指令内的所有user引用
                for (Instruction instruction : block.getInstructions()) {
                    instruction.dropAllOperands();
                }
                block.delAllInstruction();
            }
            // 是不需要删除的块
            else {
                newBlocks.add(block);
            }
        }
        // 为函数设置新的基本块列表
        function.setBasicBlocks(newBlocks);
    }
}
