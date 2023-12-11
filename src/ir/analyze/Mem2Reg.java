package ir.analyze;

import config.Config;
import ir.IrBuilder;
import ir.values.*;
import ir.values.Module;
import ir.values.constants.ConstInt;
import ir.values.instructions.*;
import utils.Pair;

import java.util.*;

/**
 * @Description TODO
 * @Author
 * @Date 2023/12/11
 **/
public class Mem2Reg {
    /**
     * 当前分析的 alloca 唯一一个 store 指令
     */
    private Store uniqueAllocaStore;
    /**
     * 如果 store 和 load 在同一个块
     */
    private BasicBlock singleBlock;
    /**
     * 当前函数的入口块
     */
    private BasicBlock entryBlock;
    /**
     * 对于特定的 alloca 指令，其 load 所在的块
     */
    private final HashSet<BasicBlock> loadUsingBlocks = new HashSet<>();
    /**
     * 对于特定的 alloca 指令，其 store 所在的块
     */
    private final HashSet<BasicBlock> storeDefiningBlocks = new HashSet<>();
    private Function curFunction;
    /**
     * 这里面记录当前函数可以被提升的 allocas
     */
    private final ArrayList<Alloca> promotableAllocas = new ArrayList<>();
    private final HashMap<Phi, Alloca> phi2AllocaMap = new HashMap<>();

    public void analyze() {
        if (Config.openMem2RegOpt) {
            for (Function function : Module.getFunctions()) {
                if (!function.isLibFunc()) {
                    this.curFunction = function;
                    processFunction();
                }
//                System.out.println("遍历函数并生成phi " + function.getName());
            }
        }
    }

    private void processFunction() {
        entryBlock = curFunction.getHeadBlock();
        // 寻找可以被消去的alloca
        for(Instruction instruction : entryBlock.getInstructions()){
            if (instruction instanceof Alloca && ((Alloca) instruction).canPromotable()) {
                promotableAllocas.add((Alloca) instruction);
            }
        }

        // llvm doesn't include this step, I add it to make things simple
        for (BasicBlock block : curFunction.getBasicBlocks()){
            sweepBlock(block);
        }

        // promote alloca one by one
        // 遍历 alloca 数组
        Iterator<Alloca> iterator = promotableAllocas.iterator();
        while (iterator.hasNext()) {
            Alloca alloca = iterator.next();
            // is alloca is never used, we can safely delete it
            // 之前的 sweep 会导致 alloca 的使用者减少，那么就可能出现 alloca 没有使用者的情况，此时就可以将其删除了
            if (alloca.getUsers().isEmpty()) {
                alloca.dropAllOperands();
                alloca.eraseFromParent();
                // 将指令从节点中移出
                iterator.remove();
                // 也不用分析了
                continue;
            }

            // onlyStore
            analyzeAllocaInfo(alloca);

            // prune optimization
            // no store，那么就删除这个 alloca 和与之相关的 load
            if (storeDefiningBlocks.size() == 0) {
                // 如果没有 store 那么使用者只有 load 了，那么删掉 load 就好了，因为不能读没有的值
                // 之所以在这里 clone， 是因为不然 alloca - load 的关系也没了
                ArrayList<User> loadClone = new ArrayList<>(alloca.getUsers());
                for (User user : loadClone) {
                    Load load = (Load) user;
                    load.replaceAllUsesWith(ConstInt.ZERO());
                    load.dropAllOperands();
                    load.eraseFromParent();
                }
                alloca.dropAllOperands();
                alloca.eraseFromParent();
                iterator.remove();
                // 不用再讨论了
                continue;
            }

            // onlyStore
            if (uniqueAllocaStore != null && handleOnlyStore(alloca)) {
                iterator.remove();
                continue;
            }

            // store / load in one block
            if (singleBlock != null) {
                handleSingleBlock(alloca);
                iterator.remove();
                continue;
            }

            // 所有 definingBlocks 的递归支配边界（递归边界的闭包）都是需要插入 phi 节点的
            HashSet<BasicBlock> phiBlocks = calculateIDF(storeDefiningBlocks);
            // 去掉不需要插入的节点
            phiBlocks.removeIf(block -> !isPhiAlive(alloca, block));

            // insert phi node
            insertPhi(alloca, phiBlocks);
        }

        // 如果 alloca 都被删除了（一般表示不需要插入 phi 就可以结束战斗）
        if (promotableAllocas.isEmpty()) {
            return;
        }

        // rename phi node and add incoming <value, block>
        renamePhi();

        for (Alloca ai : promotableAllocas) {
            ai.dropAllOperands();
            ai.eraseFromParent();
        }

        promotableAllocas.clear();
        phi2AllocaMap.clear();
    }

    /**
     * 利用不动点法求解支配边界的闭包
     * 也就是支配边界，支配边界的支配边界，支配边界的支配边界的支配边界....
     *
     * @param definingBlocks 拥有 store 的点
     * @return 支配边界的闭包
     */
    private HashSet<BasicBlock> calculateIDF(HashSet<BasicBlock> definingBlocks) {
        HashSet<BasicBlock> ans = new HashSet<>();
        for (BasicBlock definingBlock : definingBlocks) {
            ans.addAll(definingBlock.getDominanceFrontier());
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            HashSet<BasicBlock> newAns = new HashSet<>(ans);
            for (BasicBlock block : ans) {
                newAns.addAll(block.getDominanceFrontier());
            }
            if (newAns.size() > ans.size()) {
                changed = true;
                ans = newAns;
            }
        }
        return ans;
    }

    /**
     * 填写
     * definingBlocks, usingBlocks, onlyStore, onlyBlock
     *
     * @param alloca 当前分析的 alloca
     */
    private void analyzeAllocaInfo(Alloca alloca) {
        // 清空
        storeDefiningBlocks.clear();
        loadUsingBlocks.clear();
        singleBlock = null;
        uniqueAllocaStore = null;

        // 使用 alloca 的 store 的指令个数
        int storeCnt = 0;
        // 遍历使用 alloca 的指令（就是 load 和 store）
        for (Value user : alloca.getUsers()) {
            // 如果是 store
            if (user instanceof Store) {
                storeDefiningBlocks.add(((Store) user).getParent());
                if (storeCnt == 0) {
                    uniqueAllocaStore = (Store) user;
                }
                storeCnt++;
            } else if (user instanceof Load) {
                loadUsingBlocks.add(((Load) user).getParent());
            }
        }

        if (storeCnt > 1) {
            uniqueAllocaStore = null;
        }

        if (storeDefiningBlocks.size() == 1 && storeDefiningBlocks.equals(loadUsingBlocks)) {
            singleBlock = storeDefiningBlocks.iterator().next();
        }
    }

    /**
     * 处理只有一个 store 的情况
     *
     * @param alloca 当前 alloca
     * @return usingBlock 是否为空，本质上是是否需要进行下一步处理，如果是 false 那么就需要继续处理
     */
    private boolean handleOnlyStore(Alloca alloca) {
        // construct later
        loadUsingBlocks.clear();
        // replaceValue 是 store 向内存写入的值
        Value replaceValue = uniqueAllocaStore.getValue();
        ArrayList<User> users = new ArrayList<>(alloca.getUsers());
        // 只有一个 store ，其他都是 load
        for (User user : users) {
            if (user instanceof Store) {
                if (!user.equals(uniqueAllocaStore)) {
                    throw new AssertionError("ai has store user different from onlyStore in dealOnlyStore");
                }
            } else {
                Load load = (Load) user;
                // 如果 store 所在的块是 load 的支配者，那么就将用到 load 读入值的地方换成 store
                if (uniqueAllocaStore.getParent() != load.getParent() && uniqueAllocaStore.getParent().isDominating(load.getParent())) {
                    load.replaceAllUsesWith(replaceValue);
                    load.dropAllOperands();
                    load.eraseFromParent();
                }
                // 没有这么好的条件的话，就加入 usingBlocks
                else {
                    loadUsingBlocks.add(load.getParent());
                }
            }
        }

        boolean result = loadUsingBlocks.isEmpty();
        // 如果没有比较差的 load，那么 store 就可以删除了，因为都被 replace 了
        if (result) {
            uniqueAllocaStore.dropAllOperands();
            uniqueAllocaStore.eraseFromParent();
            alloca.dropAllOperands();
            alloca.eraseFromParent();
        }

        return result;
    }

    private void handleSingleBlock(Alloca alloca) {
        boolean meetStore = false;
        // 遍历所有的指令
        LinkedList<Instruction> instructions = new LinkedList<>(singleBlock.getInstructions());
        for(Instruction instruction : instructions){
            // 遇到了还没有 store 就先 load 的情况，直接删掉
            if (isAllocLoad(instruction, alloca) && !meetStore) {
                instruction.replaceAllUsesWith(ConstInt.ZERO());
                instruction.dropAllOperands();
                instruction.eraseFromParent();
            }
            else if (isAllocStore(instruction, alloca)) {
                if (meetStore) {
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                } else {
                    meetStore = true;
                }
            }
        }
        alloca.dropAllOperands();
        alloca.eraseFromParent();
    }

    /**
     * 这是一种指令删除，指的是这种情况
     * 在一个基本块中
     * 对于 store -> alloca -> load 这样的逻辑链条（这样的 store 和 load 一定是在对非数组读写，不然就是 store - gep - load）
     * 用 store 的值代替所有的 load 出的值，然后将 load 删除
     * 之后再对同一个 alloca 的多次 store，简化为最后一次
     *
     * @param block 当前块
     */
    private void sweepBlock(BasicBlock block) {
        HashMap<Alloca, Store> alloca2store = new HashMap<>();

        LinkedList<Instruction> instructions = new LinkedList<>(block.getInstructions());
        for (Instruction instruction : instructions){
            // 如果当前指令是 store 指令，而且地址是 alloca 分配的，那么就存到 alloca2store 中
            if (isAllocStore(instruction)) {
                alloca2store.put((Alloca) instruction.getOp(2), (Store) instruction);
            }
            // 如果当前指令是 load，而且地址是 alloca
            else if (isAllocLoad(instruction)) {
                Alloca alloca = (Alloca) instruction.getOp(1);
                // 如果将 store 取出来
                Store store = alloca2store.get(alloca);
                // 对应的是没有赋初值就 load 的情况，不用考虑这种特殊情况
                if (store == null && block == entryBlock) {
                    instruction.replaceAllUsesWith(ConstInt.ZERO());
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
                // 这里才是正文
                else if (store != null) {
                    // 这里首先用 store 的要存入的值代替了 load 要读入的值
                    instruction.replaceAllUsesWith(store.getValue());
                    // 将这条 load 指令删除
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
            }
        }
        // 清空对应关系
        alloca2store.clear();

        instructions = new LinkedList<>(block.getInstructions());
        int len = instructions.size();
        // 进行倒序遍历
        for(int i = len - 1; i >= 0; i--){
            Instruction instruction = instructions.get(i);
            // 如果是 store 指令
            if (isAllocStore(instruction)) {
                Alloca alloca = (Alloca) instruction.getOp(2);
                Store store = alloca2store.get(alloca);
                // 这不是最后一条对于 alloca 这个内存的写，那么就删除
                if (store != null) {
                    instruction.dropAllOperands();
                    instruction.eraseFromParent();
                }
                // 说明之前没有，那么记录并加入
                else {
                    alloca2store.put(alloca, (Store) instruction);
                }
            }
        }
    }

    /**
     * 有插入 phi 的必要：也就是有与 alloca 相关的 load 或者 store
     *
     * @param alloca alloca
     * @param block  当前块
     * @return 需要插入则为 true
     */
    private boolean isPhiAlive(Alloca alloca, BasicBlock block) {
        for (Instruction instruction : block.getInstructions()){
            if (isAllocLoad(instruction, alloca)) {
                return true;
            }
            if (isAllocStore(instruction, alloca)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将 phi 节点插入基本块之前
     * 并且填写 phi2Alloca 为后续的重命名做准备
     *
     * @param alloca    当前 alloca
     * @param phiBlocks 需要插入 phi 的基本块
     */
    private void insertPhi(Alloca alloca, HashSet<BasicBlock> phiBlocks) {
        for (BasicBlock phiBlock : phiBlocks) {
            Phi phi = IrBuilder.buildPhi(alloca.getAllocaedType(), phiBlock);
            phi2AllocaMap.put(phi, alloca);
        }
    }

    /**
     * 重命名，完成 phi 的嵌入
     */
    private void renamePhi() {
        HashMap<BasicBlock, Boolean> visitMap = new HashMap<>();
        HashMap<Alloca, Value> variableVersion = new HashMap<>();
        
        for(BasicBlock block : curFunction.getBasicBlocks()){
            visitMap.put(block, false);
        }

        for (Alloca alloca : promotableAllocas) {
            // default undef is 0
            variableVersion.put(alloca, ConstInt.ZERO());
        }
        // 手动 dfs
        Stack<Pair<BasicBlock, HashMap<Alloca, Value>>> bbStack = new Stack<>();
        bbStack.push(new Pair<>(entryBlock, variableVersion));

        while (!bbStack.isEmpty()) {
            Pair<BasicBlock, HashMap<Alloca, Value>> tmp = bbStack.pop();
            BasicBlock currentBlock = tmp.getFirst();
            variableVersion = tmp.getSecond();
            if (visitMap.get(currentBlock)) {
                continue;
            }

            // main logic
            // 遍历当前块的所有指令
            int i = 0;
            LinkedList<Instruction> instructions = new LinkedList<>(currentBlock.getInstructions());
            while (instructions.get(i) instanceof Phi) {
                variableVersion.put(phi2AllocaMap.get((Phi) instructions.get(i)), instructions.get(i));
                i++;
            }
            while (i < instructions.size()) {
                Instruction instruction = instructions.get(i);
                if (instruction instanceof Load load) {
                    if (isAllocLoad(load)) {
                        instruction.replaceAllUsesWith(variableVersion.get((Alloca) ((Load) instruction).getPointer()));
                        instruction.dropAllOperands();
                        instruction.eraseFromParent();
                    }
                } else if (instruction instanceof Store store) {
                    if (isAllocStore(store)) {
                        variableVersion.put((Alloca) store.getPointer(), store.getValue());
                        instruction.dropAllOperands();
                        instruction.eraseFromParent();
                    }
                }
                i++;
            }

            for (BasicBlock successor : currentBlock.getSucBlocks()) {
                instructions = successor.getInstructions();
                i = 0;
                while (instructions.get(i) instanceof Phi) {
                    Phi phi = (Phi) (instructions.get(i));
                    Alloca ai = phi2AllocaMap.get(phi);
                    phi.addIncoming(variableVersion.get(ai), currentBlock);
                    i++;
                }
                if (!visitMap.get(successor)) {
                    bbStack.push(new Pair<>(successor, new HashMap<>(variableVersion)));
                }
            }

            visitMap.put(currentBlock, true);
        }
    }

    /**
     * 如果当前指令是 store，而且地址是 alloca
     * @param instruction   当前指令
     */
    private boolean isAllocStore(Instruction instruction){
        return instruction instanceof Store && ((Store) instruction).getPointer() instanceof Alloca;
    }

    /**
     * 如果当前指令是 load，而且地址是 alloca
     * @param instruction   当前指令
     */
    private boolean isAllocLoad(Instruction instruction){
        return instruction instanceof Load && ((Load) instruction).getPointer() instanceof Alloca;
    }
    /**
     * 如果当前指令是 store，而且地址是指定的 alloca
     * @param instruction   当前指令
     */
    private boolean isAllocStore(Instruction instruction, Alloca alloca){
        return instruction instanceof Store && ((Store) instruction).getPointer() == alloca;
    }

    /**
     * 如果当前指令是 load，而且地址是指定的 alloca
     * @param instruction   当前指令
     */
    private boolean isAllocLoad(Instruction instruction, Alloca alloca){
        return instruction instanceof Load && ((Load) instruction).getPointer() == alloca;
    }
}
