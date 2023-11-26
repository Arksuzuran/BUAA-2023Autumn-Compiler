package backend.reg;

import backend.instructions.MipsInstruction;
import backend.instructions.MipsLoad;
import backend.instructions.MipsMove;
import backend.instructions.MipsStore;
import backend.operands.*;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import backend.parts.MipsModule;
import utils.MipsTool;
import utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 寄存器分配
 * @Author HIKARI
 * @Date 2023/11/22
 **/
public class RegBuilder {

    private final MipsModule mipsModule = MipsModule.getInstance();

    private MipsFunction curFunction;
    /**
     * 在保存现场时需要保存的寄存器数量
     */
    private final int K = RegType.regsCanAlloca.size();
    /**
     * 每个基本块对应的活跃变量分析信息
     */
    private HashMap<MipsBlock, BlockLiveVarInfo> blockLiveInfoMap;
    /**
     * 根据一个节点查询与之相关的节点组
     **/
    private HashMap<MipsOperand, HashSet<MipsOperand>> adjList;
    /**
     * 边的集合
     */
    private HashSet<Pair<MipsOperand, MipsOperand>> adjSet;
    /**
     * 当一条传送指令 (u,v) 被合并，且 v 已经被放入 coalescedNodes 中，alias(v) = u
     */
    private HashMap<MipsOperand, MipsOperand> alias;
    /**
     * 从一个节点到与该节点相关的 mov 指令之间的映射
     */
    private HashMap<MipsOperand, HashSet<MipsMove>> moveList;
    private HashSet<MipsOperand> simplifyWorklist;
    /**
     * 低度数的，传送有关的节点表
     */
    private HashSet<MipsOperand> freezeWorklist;
    /**
     * 高度数的节点表
     */
    private HashSet<MipsOperand> spillWorklist;
    /**
     * 本轮中要被溢出的节点的集合
     */
    private HashSet<MipsOperand> spilledNodes;
    /**
     * 已合并的节点的集合，比如将 u 合并到 v，那么将 u 加入这里，然后 v 加入其他集合
     */
    private HashSet<MipsOperand> coalescedNodes;
    /**
     * 包含删除的点
     */
    private Stack<MipsOperand> selectStack;
    /**
     * 有可能合并的move指令集合
     */
    private HashSet<MipsMove> worklistMoves;
    /**
     * 还未做好合并准备的传送指令集合
     */
    private HashSet<MipsMove> activeMoves;
    /**
     * 已经合并的传送指令集合
     */
    private HashSet<MipsInstruction> coalescedMoves;
    /**
     * 源操作数和目标操作数冲突的传送指令集合
     */
    private HashSet<MipsMove> constrainedMoves;
    /**
     * 不考虑合并的传送指令集合
     */
    private HashSet<MipsMove> frozenMoves;
    /**
     * 节点的度
     */
    private HashMap<MipsOperand, Integer> degree;
    /**
     * 新的虚拟寄存器，用来处理溢出解决时引入的新的虚拟寄存器
     */
    private MipsVirtualReg vReg = null;
    /**
     * 存储操作数和所在的基本块对应的循环深度
     */
    private final HashMap<MipsOperand, Integer> loopDepths = new HashMap<>();

    /**
     * 初始化记录状态的数据结构
     */
    private void initStatus() {
        blockLiveInfoMap = BlockLiveVarInfo.liveAnalysis(curFunction);
        adjList = new HashMap<>();
        adjSet = new HashSet<>();
        alias = new HashMap<>();
        moveList = new HashMap<>();
        simplifyWorklist = new HashSet<>();
        freezeWorklist = new HashSet<>();
        spillWorklist = new HashSet<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        selectStack = new Stack<>();

        worklistMoves = new HashSet<>();
        activeMoves = new HashSet<>();
        // 下面这三个变量不一定用得到，但是 coalescedMoves 考虑删掉里面所有的 move，似乎是之前代码没有办到的
        coalescedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();

        degree = new HashMap<>();
        // 物理寄存器需要度无限大
        for (int i = 0; i < 32; i++) {
            degree.put(new MipsRealReg(i), Integer.MAX_VALUE);
        }
    }

    /**
     * 在冲突图上添加无向边
     *
     * @param u 第一个节点
     * @param v 第二个节点
     */
    private void addEdge(MipsOperand u, MipsOperand v) {
        // 如果没有这条边而且这个边不是自环
        // 从上面就可以看出，adjSet 是个边的集合，边是用 pair 模拟的
        if (!adjSet.contains(new Pair<>(u, v)) && !u.equals(v)) {
            // 无向边的加法
            adjSet.add(new Pair<>(u, v));
            adjSet.add(new Pair<>(v, u));

            // 操作条件都是没有被预着色
            if (!u.isPrecolored()) {
                // 从这里看，adjList 是一个可以用节点查询所连接的所有节点的一个结构
                adjList.putIfAbsent(u, new HashSet<>());
                adjList.get(u).add(v);
                // degree.putIfAbsent(u, 0);
                // degree 则是用来表示节点的度的
                degree.compute(u, (key, value) -> value == null ? 0 : value + 1);
            }
            if (!v.isPrecolored()) {
                adjList.putIfAbsent(v, new HashSet<>());
                adjList.get(v).add(u);
                degree.compute(v, (key, value) -> value == null ? 0 : value + 1);
            }
        }
    }

    /**
     * 通过逆序遍历函数中的所有指令, 生成冲突图
     * live 是每条指令的冲突变量集合
     */
    private void build() {
        // 倒序遍历 block
        ArrayList<MipsBlock> blocks = curFunction.getMipsBlocks();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            MipsBlock block = blocks.get(i);
            // 假设出口活跃
            // live 是一个很有意思的东西，他看似一个 block 只有一个，但是因为每条指令都更新它，所以它本质是一个指令颗粒度的东西
            // 根据 live 的内容去构建冲突图
            HashSet<MipsOperand> live = new HashSet<>(blockLiveInfoMap.get(block).getLiveOut());

            // 倒序遍历 Instruction
            LinkedList<MipsInstruction> instructions = block.getInstructions();
            for (int j = instructions.size() - 1; j >= 0; j--) {
                MipsInstruction instruction = instructions.get(j);
                ArrayList<MipsOperand> regDef = instruction.getDefRegs();
                ArrayList<MipsOperand> regUse = instruction.getUseRegs();

                // 对于 mov 指令，需要特殊处理
                if (instruction instanceof MipsMove move) {
                    MipsOperand src = move.getSrc(1);
                    MipsOperand dst = move.getDst();

                    if (src.needsColor() && dst.needsColor()) {
                        live.remove(src);

                        moveList.putIfAbsent(src, new HashSet<>());
                        moveList.get(src).add(move);

                        moveList.putIfAbsent(dst, new HashSet<>());
                        moveList.get(dst).add(move);
                        // 此时是有可能被合并的
                        worklistMoves.add(move);
//                        System.out.println("特殊处理move指令：" + move + " " + dst);
                    }
                }

                regDef.stream().filter(MipsOperand::needsColor).forEach(live::add);

                // 构建冲突边的时候，只是构建了 def 与 live 的冲突，这样似乎不够
                // 但是其实，是够得，因为在一个个指令的遍历中，能增加边的，只有 def 导致的活跃
                regDef.stream().filter(MipsOperand::needsColor).forEach(d -> live.forEach(l -> addEdge(l, d)));

                // 启发式算法的依据，用于后面挑选出溢出节点
                for (MipsOperand reg : regDef) {
                    loopDepths.put(reg, block.getLoopDepth() + 1);
                }
                for (MipsOperand reg : regUse) {
                    loopDepths.put(reg, block.getLoopDepth() + 1);
                }

                // 这里的删除是为了给前一个指令一个交代（倒序遍历），说明这个指令不再存活了（因为在这个指令被遍历了）
                regDef.stream().filter(MipsOperand::needsColor).forEach(live::remove);
                // 这里代表着又活了一个指令
                regUse.stream().filter(MipsOperand::needsColor).forEach(live::add);
            }
        }
    }

    /**
     * 遍历所有的节点（错，只是非预着色点）, 把这些节点分配加入不同的 workList
     * 但是预着色点依然在这里面存在
     */
    private void makeWorklist() {
        // 把一个函数中用到的所有虚拟寄存器都提出来
        for (MipsVirtualReg virReg : curFunction.getUsedVirtualRegs()) {
            // 度大于等于 K，加入 spillWorklist
            if (degree.getOrDefault(virReg, 0) >= K) {
                spillWorklist.add(virReg);
            }
            // 说白了就是跟 mov 指令相关的操作数，那么就加入 freezeWorklist
            else if (moveRelated(virReg)) {
                freezeWorklist.add(virReg);
            }
            // 否则就要加到 simplifyWorklist 中，就是可以进行化简的
            else {
                simplifyWorklist.add(virReg);
            }
        }
    }

    /**
     * 判断一个寄存器是否为move的操作数
     */
    private boolean moveRelated(MipsOperand u) {
        return !nodeMoves(u).isEmpty();
    }

    /**
     * 取出 activeMoves 和 workListMoves 中的，带有u寄存器的所有move指令
     *
     * @param u 待检测的节点
     * @return mov 的集合
     */
    private Set<MipsMove> nodeMoves(MipsOperand u) {
        return moveList.getOrDefault(u, new HashSet<>()).stream()
                .filter(move -> activeMoves.contains(move) || worklistMoves.contains(move))
                .collect(Collectors.toSet());
    }

    /**
     * 从 adjList 中取出与u相连的，不在 selectStack 和 coalesceNode 里的结点
     * 因为是没有删除边的操作的, 所以对于一些节点, 比如已经删掉或者合并的, 就需要从这里去掉
     *
     * @return 与这个节点相连的节点组
     */
    private Set<MipsOperand> getAdjacent(MipsOperand u) {
        return adjList.getOrDefault(u, new HashSet<>()).stream()
                .filter(v -> !(selectStack.contains(v) || coalescedNodes.contains(v)))
                .collect(Collectors.toSet());
    }

    /**
     * 这里进行了一个节点 u 和其相连的节点将 activeMoves 删去，然后加入到 workListMoves 的操作
     * 也就是将这个节点和与其相连的 mov 节点都从“不能合并”状态转换为“能合并”状态
     * 从这里可以看出，能合并要求度是 K - 1 以下
     *
     * @param u 节点
     */
    private void enableMoves(MipsOperand u) {
        nodeMoves(u).stream()
                .filter(activeMoves::contains)
                .forEach(m ->
                {
                    activeMoves.remove(m);
                    worklistMoves.add(m);
                });

        getAdjacent(u).forEach(a -> nodeMoves(a).stream()
                .filter(activeMoves::contains)
                .forEach(m ->
                {
                    activeMoves.remove(m);
                    worklistMoves.add(m);
                }));
    }

    /**
     * 当简化一个节点的时候, 与其相连的节点都需要进行一定的改动
     * 最简单的就是降低度,
     * 此外, 随着度的降低, 有些节点会从某个 list 移动到另一个 list
     *
     * @param u 相连的节点
     */
    private void decreaseDegree(MipsOperand u) {
//        System.out.println("decreaseDegree " + u + " get:" + degree.get(u));
        int d = degree.get(u);
        degree.put(u, d - 1);

        // 当未修改的度是 K 的时候，那么修改过后就是 K - 1， 那么此时就需要特殊处理
        if (d == K) {
            enableMoves(u);
            spillWorklist.remove(u);
            if (moveRelated(u)) {
                freezeWorklist.add(u);
            } else {
                simplifyWorklist.add(u);
            }
        }
    }

    /**
     * 选择能够进行着色的点
     * 这个函数会从 simplifyWorklist 中节点，然后加入到 selectStack 中
     * 与此同时，需要修改与这个节点相关的节点的度
     */
    private void simplify() {
        MipsOperand n = simplifyWorklist.iterator().next();
        // 从可以简化的列表中取出一个节点
        simplifyWorklist.remove(n);
        // selectStack 就是图着色时用的栈
        selectStack.push(n);
//        System.out.println("选择结点 " + n);
        // 把与这个删掉的点有关的点的度都降低
        getAdjacent(n).forEach(this::decreaseDegree);
    }

    /**
     * 对于一个合并的节点，他可以有两个名字，所以可以检索它的另一个名字
     *
     * @param u 被合并节点
     * @return 被合并的另一个节点
     */
    private MipsOperand getAlias(MipsOperand u) {
        while (coalescedNodes.contains(u)) {
            u = alias.get(u);
        }
        return u;
    }

    /**
     * 这个函数实现的是将一个节点从 freezeWorklist 移动到 simplifyWorklist 中
     * 这是一个 coalesce 过程的子方法，主要用于合并
     *
     * @param u 待合并的节点
     */
    private void addWorklist(MipsOperand u) {
        if (!u.isPrecolored() && !moveRelated(u) && degree.getOrDefault(u, 0) < K) {
            freezeWorklist.remove(u);
            simplifyWorklist.add(u);
        }
    }

    /**
     * 这是用来判断 v，u 是否可以合并的
     * 判断方法是考虑 v 的临边关系，这种判断方法被称为 George
     *
     * @param v 一定是虚拟寄存器
     * @param u 可能是物理寄存器
     * @return 可以合并则为 true
     */
    private boolean adjOk(MipsOperand v, MipsOperand u) {
        return getAdjacent(v).stream().allMatch(t -> ok(t, u));
    }

    /**
     * 合并预着色寄存器的时候用到的启发式函数
     * 其中 t 是待合并的虚拟寄存器的邻接点，r 是待合并的预着色寄存器
     * 这三个条件满足一个就可以合并
     *
     * @param t 合并的虚拟寄存器的邻接点
     * @param r 待合并的预着色寄存器
     * @return 可以合并就是 true
     */
    private boolean ok(MipsOperand t, MipsOperand r) {
        return degree.get(t) < K || t.isPrecolored() || adjSet.contains(new Pair<>(t, r));
    }

    /**
     * 这是另一种保守地判断可不可以合并的方法，有一说一，我没看出为啥要用两种方法来
     * 被称为 briggs 法
     *
     * @param u 待合并的节点 1
     * @param v 待合并的节点 2
     * @return 可以合并就是 true
     */
    private boolean conservative(MipsOperand u, MipsOperand v) {
        Set<MipsOperand> uAdjacent = getAdjacent(u);
        Set<MipsOperand> vAdjacent = getAdjacent(v);
        uAdjacent.addAll(vAdjacent);
        long count = uAdjacent.stream().filter(n -> degree.get(n) >= K).count();
        return count < K;
    }

    /**
     * 这是合并操作
     *
     * @param u 待合并的节点 1
     * @param v 待合并的节点 2
     */
    // TODO bug here
    private void combine(MipsOperand u, MipsOperand v) {
        // 这里做的是把他们从原有的 worklist 中移出
        if (freezeWorklist.contains(v)) {
            freezeWorklist.remove(v);
        } else {
            spillWorklist.remove(v);
        }
//        System.out.println("合并结点" + u + ", " + v);
        coalescedNodes.add(v);
        // 这里没有问题，相当于 alias 的 key 是虚拟寄存器，而 value 是物理寄存器
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));
        // enableMoves.accept(v);
        getAdjacent(v).forEach(t ->
        {
            addEdge(t, u);
            decreaseDegree(t);
        });

        if (degree.getOrDefault(u, 0) >= K && freezeWorklist.contains(u)) {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }

    /**
     * 用于合并节点
     */
    private void coalesce() {
        MipsMove objMove = worklistMoves.iterator().next();
        MipsOperand u = getAlias(objMove.getDst());
        MipsOperand v = getAlias(objMove.getSrc(1));

        // 如果 v 是物理寄存器，那么就需要交换一下，最后的结果就是如果有物理寄存器的话，那么一定是 u
        // 之所以这么操作，是因为合并也是一种着色，我们需要让合并后剩下的那个节点，是预着色点
        if (v.isPrecolored()) {
            MipsOperand tmp = u;
            u = v;
            v = tmp;
//            System.out.println("coalesce合并且交换" + objMove);
        }

        worklistMoves.remove(objMove);

        // 这个对应可以要进行合并了
        if (u.equals(v)) {
            coalescedMoves.add(objMove);
            addWorklist(u);
//            System.out.println("执行coalesce合并1：" + objMove);
        }
        // 对应源操作数和目的操作数冲突的情况，此时的 mov 就是受到抑制的，也就是
        else if (v.isPrecolored() || adjSet.contains(new Pair<>(u, v))) {
            constrainedMoves.add(objMove);
            addWorklist(u);
            addWorklist(v);
//            System.out.println("执行coalesce合并2：" + objMove);
        }
        // TODO HERE
        else if ((u.isPrecolored() && adjOk(v, u)) ||
                (!u.isPrecolored() && conservative(u, v))) {
            coalescedMoves.add(objMove);
            combine(u, v);
            addWorklist(u);
//            System.out.println("执行coalesce合并3：" + objMove + (u.isPrecolored() && adjOk(v, u)) + " " + (!u.isPrecolored() && conservative(u, v)));
        } else {
            activeMoves.add(objMove);
//            System.out.println("执行coalesce合并4：" + objMove);
        }
    }

    /**
     * 这个会遍历每一条与 u 有关的 mov 指令，然后将这些 mov 指令从 active 和 worklist 中移出
     * 这就意味着他们不会再被考虑合并
     *
     * @param u 待冻结的节点
     */
    private void freezeMoves(MipsOperand u) {
        for (MipsMove move : nodeMoves(u)) {
            if (activeMoves.contains(move)) {
                activeMoves.remove(move);
            } else {
                worklistMoves.remove(move);
            }

            frozenMoves.add(move);
            MipsOperand v = getAlias(move.getDst()).equals(getAlias(u)) ? getAlias(move.getSrc(1)) : getAlias(move.getDst());

            if (!moveRelated(v) && degree.getOrDefault(v, 0) < K) {
                freezeWorklist.remove(v);
                simplifyWorklist.add(v);
            }
        }
    }

    /**
     * 当 simplify 无法进行：没有低度数的，无关 mov 的点了
     * 当 coalesce 无法进行：没有符合要求可以合并的点了
     * 那么进行 freeze，就是放弃一个低度数的 mov 的点，这样就可以 simplify 了
     */
    private void freeze() {
        MipsOperand u = freezeWorklist.iterator().next();
        freezeWorklist.remove(u);
        simplifyWorklist.add(u);
        freezeMoves(u);
    }

    /**
     * 这里用到了启发式算法，因为没有 loopDepth 所以只采用一个很简单的方式，调出一个需要溢出的节点，
     * 这个节点的性质是溢出后边会大幅减少
     */
    private void selectSpill() {
        double magicNum = 1.414;
        MipsOperand m = spillWorklist.stream().max((l, r) ->
        {
            double value1 = degree.getOrDefault(l, 0).doubleValue() / Math.pow(magicNum, loopDepths.getOrDefault(l, 0));
            double value2 = degree.getOrDefault(r, 0).doubleValue() / Math.pow(magicNum, loopDepths.getOrDefault(l, 0));

            return Double.compare(value1, value2);
        }).get();
        // MipsOperand m = spillWorklist.iterator().next();
        simplifyWorklist.add(m);
        freezeMoves(m);
        spillWorklist.remove(m);
    }

    private void assignColors(MipsFunction func) {
        // colored 是记录虚拟寄存器到物理寄存器的映射关系的
        HashMap<MipsOperand, MipsOperand> colored = new HashMap<>();

        while (!selectStack.isEmpty()) {
            // 从栈上弹出一个节点
            MipsOperand n = selectStack.pop();
//            System.out.println("assign color："+n);
            // 这里做了一个包含所有待分配颜色的数组，可以看到是对于每个弹出节点，都会有这样的一个集合，表示这个节点可能的颜色
            // 这个集合会通过与其邻接点比对而不断缩小
            HashSet<RegType> okColors = new HashSet<>(RegType.regsCanAlloca);
            // 遍历与这个弹出的节点
            for (MipsOperand w : adjList.getOrDefault(n, new HashSet<>())) {
                MipsOperand a = getAlias(w);
                // 如果这个邻接点是物理寄存器，那么就要移除掉
                if (a.isAllocated() || a.isPrecolored()) {
                    okColors.remove(((MipsRealReg) a).getType());
                }
                // 如果邻接点是一个虚拟寄存器，而且已经被着色了
                else if (a instanceof MipsVirtualReg) {
                    if (colored.containsKey(a)) {
                        MipsOperand color = colored.get(a);
                        okColors.remove(((MipsRealReg) color).getType());
                    }
                }
            }
            // 如果没有备选颜色，那么就发生实际溢出
            if (okColors.isEmpty()) {
                spilledNodes.add(n);
//                System.out.println("发生溢出" + n);
            } else {
                RegType color = okColors.iterator().next();
                colored.put(n, new MipsRealReg(color, true));
//                System.out.println("添加映射1: " + n + " - " + color);
            }
        }

        if (!spilledNodes.isEmpty()) {
            return;
        }
        // 当处理完 stack 后如果还没有问题，那么就可以处理合并节点了
        // 这里的原理相当于在一开始 stack 中只压入部分点（另一些点由栈中的点代表）
        for (MipsOperand coalescedNode : coalescedNodes) {
            MipsOperand alias = getAlias(coalescedNode);
            // 如果合并的节点里有物理寄存器，而且还是一个预着色寄存器
            if (alias.isPrecolored()) {
                colored.put(coalescedNode, alias);
                System.out.println("添加映射2: " + coalescedNode + " - " + alias);
            }
            // 如果全是虚拟寄存器
            else {
                colored.put(coalescedNode, colored.get(alias));
                System.out.println("添加映射3: "+ coalescedNode + " - " + colored.get(alias));
            }
        }

        // 完成替换
        for(MipsBlock block : func.getMipsBlocks()){
            for(MipsInstruction instruction : block.getInstructions()){
//                System.out.println("准备进行替换: " + func.getName() + " 指令: " + instruction);
                ArrayList<MipsOperand> defRegs = new ArrayList<>(instruction.getDefRegs());
                ArrayList<MipsOperand> useRegs = new ArrayList<>(instruction.getUseRegs());

                for (MipsOperand def : defRegs) {
//                    System.out.println("defReg " + def);
                    if (colored.containsKey(def)) {
//                        System.out.println("完成def替换: " + "原寄存器: " + def + " ，新寄存器: " + colored.get(def));
                        instruction.replaceReg(def, colored.get(def));
                    }
                }
                for (MipsOperand use : useRegs) {
                    if (colored.containsKey(use)) {
//                        System.out.println("完成use替换: " + "原寄存器: " + use + " ，新寄存器: " + colored.get(use));
                        instruction.replaceReg(use, colored.get(use));
                    }
                }
            }

        }
    }


    /**
     * 寄存器溢出替换：首次使用
     */
    private MipsInstruction firstUseStore = null;
    /**
     * 寄存器溢出替换：最后一次定义
     */
    private MipsInstruction lastDefLoad = null;
    /**
     * 替换者所在的基本块的指令序列
     */
    private LinkedList<MipsInstruction> srcInstructions = new LinkedList<>();
    // TODO 草率修改代码，认定load无作用，但事实上其承担着溢出寄存器在栈上的实现
    private void checkPoint(MipsFunction func) throws Exception{
        int offset = func.getAllocaSize();
        MipsImm offsetImm = new MipsImm(offset);
        // 在使用之前，插入从栈中读取的指令
        if (firstUseStore != null) {
            MipsLoad load = new MipsLoad(vReg, MipsRealReg.SP, offsetImm);
            MipsTool.insertBefore(srcInstructions, firstUseStore, load);
//            System.out.println("成功替换溢出者:" + firstUseStore + ", 新的指令：" + load);
            firstUseStore = null;
        }
        // 在定义之后，插入存栈的指令
        if (lastDefLoad != null) {
            MipsStore store = new MipsStore(vReg, MipsRealReg.SP, offsetImm);
            MipsTool.insertAfter(srcInstructions, lastDefLoad, store);
//            System.out.println("成功替换溢出者:" + lastDefLoad + ", 新的指令：" + store);
            lastDefLoad = null;
        }
        vReg = null;
    }

    // TODO 还是的采用边遍历边修改数组的老方法：直接采用int i=0进行遍历
    /**
     * 处理寄存器不够用（溢出），转而要在栈中存储变量的场合
     */
    private void rewriteProgram(MipsFunction func) throws Exception{
        for (MipsOperand n : spilledNodes) {
            System.out.println("处理溢出的结点：" + n);

            // 遍历所有基本块
            ArrayList<MipsBlock> blocks = func.getMipsBlocks();
            for (MipsBlock block : blocks) {
                vReg = null;
                firstUseStore = null;
                lastDefLoad = null;

                srcInstructions = block.getInstructions();
                // 遍历所有指令
                int cntInstr = 0;
                for(int i = 0; i < srcInstructions.size(); i++){
                    MipsInstruction instruction = srcInstructions.get(i);

                    // 遍历该指令内的所有 use，如果使用过当前溢出的寄存器 n，那么取消该寄存器分配，转而换为虚拟寄存器
                    for (MipsOperand use : instruction.getUseRegs()) {
                        if (use.equals(n)) {
                            if (vReg == null) {
                                vReg = new MipsVirtualReg();
                                func.addUsedVirtualReg(vReg);
                            }
                            instruction.replaceReg(use, vReg);

                            if (firstUseStore == null && lastDefLoad == null) {
                                firstUseStore = instruction;
                            }
                        }
                    }
                    // 遍历该指令内的所有 def，如果定义过当前溢出的寄存器 n，那么取消该寄存器分配，转而换为虚拟寄存器
                    for (MipsOperand def : instruction.getDefRegs()) {
                        if (def.equals(n)) {
                            if (vReg == null) {
                                vReg = new MipsVirtualReg();
                                func.addUsedVirtualReg(vReg);
                            }
                            instruction.replaceReg(def, vReg);
                            lastDefLoad = instruction;
                        }
                    }
                    if (cntInstr > 30) {
                        if(firstUseStore != null){
                            i++;
                        }
                        checkPoint(func);
                    }
                    cntInstr++;
                }
                checkPoint(func);
            }
            // 为这个临时变量在栈上分配空间
            func.addAllocaSize(4);
        }
    }

    /**
     * 重置所有物理寄存器为未分配状态
     */
    private void clearRealRegState() {
        for (MipsFunction function : mipsModule.getFunctions()) {
            for(MipsBlock block : function.getMipsBlocks()){
                for(MipsInstruction instruction : block.getInstructions()){

                    for (MipsOperand reg : instruction.getDefRegs()) {
                        if (reg instanceof MipsRealReg) {
                            ((MipsRealReg) reg).setAllocated(false);
                        }
                    }

                    for (MipsOperand reg : instruction.getUseRegs()) {
                        if (reg instanceof MipsRealReg) {
                            ((MipsRealReg) reg).setAllocated(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * 进行寄存器分配
     * 入口方法
     */
    public void buildRegs() {
        // 遍历所有函数
        for (MipsFunction function : mipsModule.getFunctions()) {
            if (function.isLibFunc()) {
//                System.out.println("规避内联函数:" + function.getName());
                continue;
            }
//            System.out.println("开始分析函数 " + function.getName());
            curFunction = function;
            // 对于每个函数，进行活跃变量分析直至不再变化
            boolean finished = false;
            while (!finished) {
                initStatus();
                build();
                makeWorklist();
                do {
                    if (!simplifyWorklist.isEmpty()) {
                        simplify();
                    }
//                    if (!worklistMoves.isEmpty()) {
//                        coalesce();
//                    }
                    if (!freezeWorklist.isEmpty()) {
                        freeze();
                    }
                    if (!spillWorklist.isEmpty()) {
                        selectSpill();
                    }
                } while (!(simplifyWorklist.isEmpty() && worklistMoves.isEmpty() &&
                        freezeWorklist.isEmpty() && spillWorklist.isEmpty()));
                assignColors(function);

                // 实际溢出的节点
                if (spilledNodes.isEmpty()) {
                    finished = true;
                }
                // 存在实际溢出的点
                else {
                    try{
                        rewriteProgram(function);
                    } catch (Exception e){
                        System.out.println("[RewriteProgramException] " + e);
                    }
                }
            }
//            System.out.println("寄存器分配执行完成 " + function.getName());
        }

        // 因为在 color 的时候，会把 isAllocated 设置成 true，这个函数的功能就是设置成 false
        // 应该是为了避免物理寄存器在判定 equals 时的错误
        clearRealRegState();

        for (MipsFunction function1 : mipsModule.getFunctions()) {
            if (function1.isLibFunc()) {
                continue;
            }
            function1.rebuildStack();
        }
    }

}
