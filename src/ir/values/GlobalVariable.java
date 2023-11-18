package ir.values;

import backend.MipsBuilder;
import backend.parts.MipsGlobalVariable;
import ir.types.PointerType;
import ir.values.constants.*;

import java.util.ArrayList;

/**
 * @Description 全局变量
 * @Author
 * @Date 2023/10/30
 **/
public class GlobalVariable extends User{
    private final boolean isConst;

    public Constant getInitValue() {
        return initValue;
    }

    private Constant initValue = null;

    /**
     * 常量声明一定带有初始化
     * 全局变量本身以指针形式存在
     * 全局变量以@开头
     */
    public GlobalVariable(String name, boolean isConst, Constant initValue) {
        super("@" + name,
                new PointerType(initValue.getType()),
                Module.getInstance(),
                new ArrayList<>(){{add(initValue);}});
        this.isConst = isConst;
        this.initValue = initValue;
    }

    // @a = dso_local global [6 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5, i32 6]
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        // 头部
        stringBuilder.append(getName())
                .append(" = dso_local ");
        // 常量或变量
        if(isConst){
            stringBuilder.append("constant ");
        } else {
            stringBuilder.append("global ");
        }
        // 变量类型应当为指针指向的类型
        stringBuilder.append(getOperands().get(0).getType())
                .append(" ")
                .append(getOperands().get(0))
                .append("\n");
        return stringBuilder.toString();
    }

    @Override
    public void buildMips(){
        MipsGlobalVariable mipsGlobalVariable = null;
        // 无初始值错误
        if(initValue == null){
            System.out.println("GlobalVariable：initValue == null");
        }
        // 未初始化的int数组
        else if(initValue instanceof ZeroInitializer){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), initValue.getType().getSize());
        }
        // 常量字符串
        else if(initValue instanceof ConstString){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ((ConstString) initValue).getContent());
        }
        // int变量
        else if(initValue instanceof ConstInt){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), new ArrayList<>(){{
                add(((ConstInt) initValue).getValue());
            }});
        }
        // int数组
        else if(initValue instanceof ConstArray){
            ArrayList<Integer> ints = new ArrayList<>();
            for (Constant element : ((ConstArray) initValue).getFlattenElements()){
                ints.add(((ConstInt) element).getValue());
            }
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ints);
        }
        MipsBuilder.buildGlobalVariable(mipsGlobalVariable);
    }
}
