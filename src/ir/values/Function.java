package ir.values;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsInstruction;
import backend.instructions.MipsMove;
import backend.operands.MipsImm;
import backend.operands.MipsOperand;
import backend.operands.MipsVirtualReg;
import backend.parts.MipsBlock;
import backend.parts.MipsFunction;
import config.Config;
import ir.IrSymbolTable;
import ir.analyze.Loop;
import ir.types.ValueType;
import ir.values.constants.ConstInt;
import ir.values.instructions.Instruction;
import ir.values.instructions.Phi;
import utils.IrTool;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * @Description TODO
 * @Author
 * @Date 2023/10/30
 **/
public class Function extends Value {
    public ArrayList<Value> getArgValues() {
        return argValues;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void setBasicBlocks(ArrayList<BasicBlock> newBlocks) {
        basicBlocks.clear();
        basicBlocks.addAll(newBlocks);
    }

    public ValueType getReturnType() {
        return returnType;
    }

    public IrSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(IrSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    public static Function putstr = null;   // declare void @putstr(i8*)
    public static Function putint = null;   // declare void @putint(i32)
    public static Function putch = null;   // declare void @putch(i32)
    public static Function getint = null;   // declare i32 @getint()

    public boolean isLibFunc() {
        return isLibFunc;
    }

    /**
     * 是否是链接来的库函数
     */
    private boolean isLibFunc = false;
    /**
     * 形参列表
     */
    private final ArrayList<Value> argValues = new ArrayList<>();
    /**
     * 下属基本块
     */
    private final ArrayList<BasicBlock> basicBlocks = new ArrayList<>();
    /**
     * 函数返回类型
     */
    private final ValueType returnType;


    /**
     * 下属符号表
     */
    private IrSymbolTable symbolTable;

    /**
     * @param name       函数名
     * @param returnType 函数返回类型
     * @param argTypes   函数参数的类型
     */
    public Function(String name, ValueType returnType, ArrayList<ValueType> argTypes, Boolean isLibFunc) {
        // 全局符号前+@
        super("@" + name, returnType, Module.getInstance());
        this.returnType = returnType;
        this.isLibFunc = isLibFunc;
        for (int i = 0; i < argTypes.size(); i++) {
            addArgByValueType(argTypes.get(i), i);
        }
    }

    /**
     * 给函数添加新参数
     * 会将新参数加入符号表 构建对应Value并加入参数表
     *
     * @param valueType 参数的类型
     */
    public void addArgByValueType(ValueType valueType, int argNumber) {
        Value arg = new Value("%arg" + argValues.size(), valueType, this, argNumber);
        argValues.add(arg);
//        addSymbol(arg);
    }

    /**
     * 向符号表中添加符号
     *
     * @param value 要添加的符号
     */
    public void addSymbol(String name, Value value) {
        symbolTable.addSymbol(name, value);
    }

    /**
     * 向基本块列表的尾部添加基本块
     */
    public void addBlock(BasicBlock block) {
        basicBlocks.add(block);
    }

    /**
     * 获取函数头部的第一个基本块(入口块)
     */
    public BasicBlock getHeadBlock() {
        return basicBlocks.get(0);
    }

    // define dso_local i32 @a2(i32 %0, i32* %1) {
    //    %3 = alloca i32*
    //    ret i32 %9
    //}
    // declare void @putint(i32)
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        // 头部
        if (!isLibFunc) {
            stringBuilder.append("define dso_local ");
        } else {
            stringBuilder.append("declare ");
        }
        stringBuilder.append(getReturnType())
                .append(" ")
                .append(getName());

        // 非库函数：完整的函数参数列表以及函数主体
        if (!isLibFunc) {
            // 参数列表
            stringBuilder.append("(");
            IrTool.appendSBParamList(stringBuilder, argValues);
            stringBuilder.append(")");
            // 函数主体
            stringBuilder.append(" {\n");
            for (BasicBlock block : basicBlocks) {
                stringBuilder.append(block);
            }
            stringBuilder.append("}");
        }
        // 库函数：仅有带类型的参数列表
        else {
            stringBuilder.append("(");
            for (Value arg : argValues) {
                stringBuilder.append(arg.getType()).append(", ");
            }
            IrTool.cutSBTailComma(stringBuilder);
            stringBuilder.append(")");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }


    // ========== 循环分析 ==========
    /**
     * 函数内的所有Loop
     */
    private ArrayList<Loop> loops;
    /**
     * 函数内深度为1的loop
     */
    private ArrayList<Loop> loopsAtTop;

    public ArrayList<Loop> getLoops() {
        return loops;
    }

    public void setLoops(ArrayList<Loop> loops) {
        this.loops = loops;
    }

    public ArrayList<Loop> getLoopsAtTop() {
        return loopsAtTop;
    }

    public void setLoopsAtTop(ArrayList<Loop> loopsAtTop) {
        this.loopsAtTop = loopsAtTop;
    }

    // ========== 中间代码生成 ==========
    /**
     * key 是基本块的 <前驱，后继> 关系，查询出来的 Arraylist 是需要插入到两个块之间的指令（一般是插入到前驱块尾部），这样可以实现 phi 的选择功能
     */
    private final HashMap<Pair<MipsBlock, MipsBlock>, ArrayList<MipsInstruction>> phiCopysLists = new HashMap<>();

    @Override
    public void buildMips() {
        // 非内建函数才需要解析
        if (!isLibFunc) {
            Mc.curIrFunction = this;
            // 解析块
            for (BasicBlock basicBlock : basicBlocks) {
                basicBlock.buildMips();
            }
            // 将块加入函数，并完善跳转关系
            MipsFunction mipsFunction = Mc.f(this);
            MipsBlock firstMipsBlock = Mc.b(getHeadBlock());
            // 开启Mem2Reg以及PHI优化
            if (Config.openMem2RegOpt) {
                parsePhis();
//                System.out.println("[字典]\n" + phiCopysLists + "\n[进入Serial]\n");
                mipsFunction.blockSerialPHI(firstMipsBlock, phiCopysLists);
//                mipsFunction.blockSerialize(firstMipsBlock);
            }
            // 关闭Mem2Reg以及PHI优化
            else {
                mipsFunction.blockSerialize(firstMipsBlock);
            }
        }
    }

    private void parsePhis() {
        // 遍历函数中的每个块
        for (BasicBlock block : basicBlocks) {
            MipsBlock mipsBlock = Mc.b(block);

            HashSet<BasicBlock> predBlocks = block.getPreBlocks();
//            System.out.println("基本块:" + block + "[preBlocks]" + predBlocks);
            int predNum = predBlocks.size();
            if (predNum <= 1) {
                continue;
            }
            // 收集基本块中的 phi 指令
            ArrayList<Phi> phis = new ArrayList<>();
            for (Instruction instruction : block.getInstructions()) {
                if (instruction instanceof Phi) {
                    phis.add((Phi) instruction);
                } else {
                    break;
                }
            }


            for (BasicBlock preBlock : predBlocks) {
                // 前驱-后继
                Pair<MipsBlock, MipsBlock> pair = new Pair<>(Mc.b(preBlock), mipsBlock);
//                Pair<MipsBlock, MipsBlock> pair2 = new Pair<>(Mc.b(preBlock), mipsBlock);
                phiCopysLists.put(pair, genPhiCopys(phis, preBlock, block));
//                System.out.println("[放入字典]\n" + Mc.b(preBlock).getName() + " " + mipsBlock.getName());
//                System.out.println("[放入后当场查找]\n" + phiCopysLists.getOrDefault(pair2, new ArrayList<>()));
            }
//            System.out.println("==========================================");
//            System.out.println("基本块:" + block + "\n[phiCopysLists]\n" + phiCopysLists);
//            System.out.println("==========================================");
        }

    }

    /**
     * 这个函数会根据当前块和其某个前驱块，生成要插入这个前驱块的 mov 指令（通过 phi 翻译获得，我们称为 copy）
     * 因为 phi 具有并行的特性，所以在排列顺序的时候，我们需要注意
     *
     * @param phis  当前块的 phi 指令集合
     * @param block 当前块
     * @return 待插入的 copy 指令
     */
    private ArrayList<MipsInstruction> genPhiCopys(ArrayList<Phi> phis, BasicBlock irPreBlock, BasicBlock block) {
        MipsFunction mipsFunction = Mc.f(this);
        // 通过构建一个图来检验是否成环
        HashMap<MipsOperand, MipsOperand> graph = new HashMap<>();

        ArrayList<MipsInstruction> copys = new ArrayList<>();

        // 构建一个图
        for (Phi phi : phis) {
            MipsOperand phiTarget = MipsBuilder.buildOperand(phi, false, this, block);
            // 该preBlock对应的phi里的Value
            Value inputValue = phi.getInputValForBlock(irPreBlock);
            // 这里进行了一个复杂的讨论，这是因为一般的 parseOperand 在分析立即数的时候，可能会引入
            // 其他指令，而这些指令会跟在当前块上，而不是随意移动的（我们需要他们随意移动）
            MipsOperand phiSrc;
            if (inputValue instanceof ConstInt) {
                phiSrc = new MipsImm(((ConstInt) inputValue).getValue());
            } else {
                phiSrc = MipsBuilder.buildOperand(inputValue, true, this, block);
            }
            graph.put(phiTarget, phiSrc);
        }

        while (!graph.isEmpty()) {
            Stack<MipsOperand> path = new Stack<>();
            MipsOperand cur;
            // 对这个图进行 DFS 遍历来获得成环信息, DFS 发生了不止一次，而是每次检测到一个环就会处理一次
            for (cur = graph.entrySet().iterator().next().getKey(); graph.containsKey(cur); cur = graph.get(cur)) {
                // 这就说明成环了，也就是会有 swap 问题
                if (path.contains(cur)) {
                    break;
                } else {
                    path.push(cur);
                }
            }
            if (!graph.containsKey(cur)) {
                handleNoCyclePath(path, cur, copys, graph);
            } else {
                handleCyclePath(mipsFunction, path, cur, copys, graph);
                handleNoCyclePath(path, cur, copys, graph);
            }
        }
        return copys;
    }

    private void handleNoCyclePath(Stack<MipsOperand> path, MipsOperand begin, ArrayList<MipsInstruction> copys, HashMap<MipsOperand, MipsOperand> graph) {
        MipsOperand phiSrc = begin;
        while (!path.isEmpty()) {
            MipsOperand phiTarget = path.pop();
            MipsInstruction move = new MipsMove(phiTarget, phiSrc);
            copys.add(0, move);
            phiSrc = phiTarget;
            graph.remove(phiTarget);
        }
    }

    private void handleCyclePath(MipsFunction MipsFunction, Stack<MipsOperand> path, MipsOperand begin, ArrayList<MipsInstruction> copys, HashMap<MipsOperand, MipsOperand> graph) {
        MipsVirtualReg tmp = new MipsVirtualReg();
        MipsFunction.addUsedVirtualReg(tmp);

        MipsMove move = new MipsMove(tmp, null);
        while (path.contains(begin)) {
            MipsOperand r = path.pop();
            move.setSrc(r);
            copys.add(move);
            move = new MipsMove(r, null);
            graph.remove(r);
        }
        move.setSrc(tmp);
    }
}
