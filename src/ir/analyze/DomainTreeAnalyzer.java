package ir.analyze;

import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Stack;

/**
 * @Description 构造支配树
 * @Author  HIKARI
 * @Date 2023/11/18
 **/
public class DomainTreeAnalyzer {
    /**
     * 入口方法 开始进行分析
     */
    public static void analyze(){
        ArrayList<Function> functions = Module.getFunctions();
        for(Function function : functions){
            if (!function.isLibFunc()) {
                analyzeDominanceInfo(function);
                analyzeDominanceFrontier(function);
            }
        }
    }

    /**
     * 计算支配信息
     * @param function 待分析的函数
     */
    public static void analyzeDominanceInfo(Function function) {
        // 入口块
        BasicBlock entryBlock = function.getHeadBlock();
        // 基本块的数目
        int blockNum = function.getBasicBlocks().size();
        // 每个基本块都有一个 bitSet，用于表示这个块的 domer
        ArrayList<BitSet> domers = new ArrayList<>(blockNum);
        ArrayList<BasicBlock> blocks = function.getBasicBlocks();

        // 作为 block 的索引
        int index = 0;
        for(BasicBlock block : blocks){
//            block.getDomers().clear();
//            block.getIdomees().clear();
            domers.add(new BitSet());
            // 入口块的支配者是自己
            if(block == entryBlock){
                domers.get(index).set(index);
            }
            else{
                domers.get(index).set(0, blockNum);
            }
            index++;
        }

        // 不动点算法 计算domer
        boolean changed = true;
        while (changed) {
            changed = false;
            index = 0;
            // 遍历基本块
            for(BasicBlock curBlock : blocks){
                // 对于非入口块
                if(curBlock != entryBlock){
                    // temp 初始全置1
                    BitSet temp = new BitSet();
                    temp.set(0, blockNum);
                    for (BasicBlock preBlock : curBlock.getPreBlocks()){
                        int preIndex = blocks.indexOf(preBlock);
                        temp.and(domers.get(preIndex));
                    }
                    // 自己也是自己的domer
                    temp.set(index);
                    // 若temp更新，则
                    if(!temp.equals(domers.get(index))){
                        domers.get(index).clear();
                        domers.get(index).or(temp);
                        changed = true;
                    }
                }
                index++;
            }
        }
        // 将domer信息存入所有基本块
        for (int i = 0; i < blockNum; i++) {
            BasicBlock curBlock = blocks.get(i);
            BitSet domerInfo = domers.get(i);
            // 遍历bitset，找到并记录每一个支配者
            for (int domerIndex = domerInfo.nextSetBit(0); domerIndex >= 0; domerIndex = domerInfo.nextSetBit(domerIndex + 1)) {
                BasicBlock domerBlock = blocks.get(domerIndex);
                curBlock.getDomers().add(domerBlock);
            }
        }

        // 计算所有基本块的：直接支配者 直接被支配者
        for(BasicBlock curBlock : blocks){
            // 遍历当前块的支配者
            for (BasicBlock domer1 : curBlock.getDomers()) {
                // 排除自身
                if (domer1 != curBlock) {
                    boolean isIdom = true;
                    for (BasicBlock domer2 : curBlock.getDomers()) {
                        // domer1支配了domer2，domer2支配curBlock，表明并不是直接的支配者
                        if (domer2 != curBlock && domer2 != domer1 && domer2.getDomers().contains(domer1)) {
                            isIdom = false;
                            break;
                        }
                    }
                    // 是直接支配块，则双方都要互相等级
                    if (isIdom) {
                        curBlock.setIdomer(domer1);
                        domer1.getIdomees().add(curBlock);
                        break;
                    }
                }
            }
        }
        // 计算支配树深度
        analyzeDominanceLevel(entryBlock, 0);
    }
    /**
     * DFS计算支配树深度
     * 支配树由直接支配关系组成
     * @param block 基本块
     * @param domLevel 当前深度
     */
    public static void analyzeDominanceLevel(BasicBlock block, int domLevel) {
        block.setDomLevel(domLevel);
        for (BasicBlock succ : block.getIdomees()) {
            analyzeDominanceLevel(succ, domLevel + 1);
        }
    }

    /**
     * 计算一个函数中所有基本块的支配边界
     * @param function 当前函数
     */
    public static void analyzeDominanceFrontier(Function function) {
        ArrayList<BasicBlock> blocks = function.getBasicBlocks();
        // 清空原来的支配边界
//        for(BasicBlock block : blocks){
//            block.getDominanceFrontier().clear();
//        }
        for(BasicBlock block : blocks){
            for(BasicBlock sucBlock : block.getSucBlocks()){
                // curBlock沿支配者链进行迭代
                BasicBlock curBlock = block;
                while(curBlock == sucBlock || !sucBlock.getDomers().contains(curBlock)){
                    curBlock.getDominanceFrontier().add(sucBlock);
                    curBlock = curBlock.getIdomer();
                }
            }
        }
    }

    /**
     * 获得指定函数支配树的后序遍历序列
     */
    public static ArrayList<BasicBlock> analyzeDominanceTreePostOrder(Function function) {
        // 后序序列
        ArrayList<BasicBlock> result = new ArrayList<>();
        // 已经被访问过的结点
        HashSet<BasicBlock> visited = new HashSet<>();
        Stack<BasicBlock> stack = new Stack<>();
        // 头块一定是支配树的根节点
        stack.add(function.getHeadBlock());
        // dfs
        while (!stack.isEmpty()) {
            BasicBlock parent = stack.peek();
            // 该节点已被访问过
            if (visited.contains(parent)) {
                result.add(parent);
                stack.pop();
                continue;
            }
            // 加入子节点
            for (BasicBlock idomee : parent.getIdomees()) {
                stack.push(idomee);
            }
            visited.add(parent);
        }
        return result;
    }
}
