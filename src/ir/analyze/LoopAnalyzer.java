package ir.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/18
 **/
public class LoopAnalyzer {

    private static ArrayList<Loop> loops;
    private static ArrayList<Loop> loopsAtTop;

    public static void analyze(){
        for(Function function : Module.getFunctions()){
            if(!function.isLibFunc()){
                loops = new ArrayList<>();
                loopsAtTop = new ArrayList<>();
                function.setLoops(loops);
                function.setLoopsAtTop(loopsAtTop);
                // 分析函数内的循环
                analyzeLoopInFunction(function);
            }
        }
    }

    private static void analyzeLoopInFunction(Function function){
        // 与当前块存在循环关系的块
        ArrayList<BasicBlock> latchBlocks = new ArrayList<>();
        // 按后序遍历支配树的方式，获得从内循环到外循环的基本块列表
        ArrayList<BasicBlock> blocks = DomainTreeAnalyzer.analyzeDominanceTreePostOrder(function);

        // 遍历所有块
        // 判断当前块是不是循环头块
        for (BasicBlock block : blocks) {
            // 遍历前驱节点, 如果支配前驱结点则存在循环
            for (BasicBlock predecessor : block.getPreBlocks()) {
                if (block.isDominating(predecessor)) {
                    latchBlocks.add(predecessor);
                }
            }
            // latchBlock 不为空，则存在循环
            if (!latchBlocks.isEmpty()) {
                // 制作出一个新的 loop
                // 从这里可以看出，此时的 block 就是入口块的意思
                Loop loop = new Loop(block, latchBlocks);
                completeLoop(latchBlocks, loop);
                // 为下一次计算做准备
                latchBlocks.clear();
            }
        }
        // 建立循环与子循环的关系
        addLoopSons(function.getHeadBlock());
    }

    /**
     * 将循环体的块加入循环中
     * 采用的是反转 CFG 图的方式
     * @param latchBlocks 栓块集合
     * @param loop 当前循环
     */
    private static void completeLoop(ArrayList<BasicBlock> latchBlocks, Loop loop) {
        // bfs，将所有的闩块加入队列
        ArrayList<BasicBlock> queue = new ArrayList<>(latchBlocks);

        while (!queue.isEmpty()) {
            // 出队
            BasicBlock block = queue.remove(0);
            // subloop 是当前块所在的循环，最终是目的是 subloop 是最外层循环
            Loop subloop = block.getLoop();
            // 当前没有子循环
            if (subloop == null) {
                // 设置为传入循环
                block.setLoop(loop);
                if (block == loop.getEntryBlock()) {
                    continue;
                }
                // 这里加入了所有前驱，应该是循环体，除了头块以外，其他的循环体的前驱也是循环体
                queue.addAll(block.getPreBlocks());
            }
            // 当前有子循环
            else {
                // parent 是 subloop 的外循环
                Loop parent = subloop.getParentLoop();
                // 一直让 subloop 为最外层循环
                while (parent != null) {
                    subloop = parent;
                    parent = parent.getParentLoop();
                }
                // loop 是最外层
                if (subloop == loop) {
                    continue;
                }
                subloop.setParentLoop(loop);
                // 遍历内循环的头块的前驱，有一部分是在子循环的循环体中的（闩），其他的在外层循环体中
                for (BasicBlock predecessor : subloop.getEntryBlock().getPreBlocks()) {
                    // 不是闩
                    if (predecessor.getLoop() != subloop) {
                        queue.add(predecessor);
                    }
                }
            }
        }
    }

    /**
     * 建立外循环对内循环的关系
     * 登记所有的循环
     * 登记循环深度
     * @param root 入口块
     */
    private static void addLoopSons(BasicBlock root) {
        Stack<BasicBlock> stack = new Stack<>();
        HashSet<BasicBlock> visited = new HashSet<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            BasicBlock currentBlock = stack.pop();
            visited.add(currentBlock);
            // 是 Header
            Loop subloop = currentBlock.getLoop();

            // currentBlock 是循环头块
            if (subloop != null && currentBlock == subloop.getEntryBlock()) {
                Loop parentLoop = subloop.getParentLoop();
                // subloop 是内层的
                if (parentLoop != null) {
                    parentLoop.addSubLoop(subloop);
                    loops.add(subloop);
                }
                // 如果没有父循环，说明是顶端循环
                else {
                    loopsAtTop.add(subloop);
                    loops.add(subloop);
                }

                // 登记循环深度
                int depth = 1;
                Loop tmp = subloop.getParentLoop();
                while (tmp != null) {
                    tmp = tmp.getParentLoop();
                    depth++;
                }
                subloop.setLoopDepth(depth);
            }

            while (subloop != null) {
                subloop.addBlock(currentBlock);
                subloop = subloop.getParentLoop();
            }

            for (BasicBlock successor : currentBlock.getSucBlocks()) {
                if (!visited.contains(successor)) {
                    stack.push(successor);
                }
            }
        }
    }
}
