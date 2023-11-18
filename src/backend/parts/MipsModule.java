package backend.parts;

import ir.values.GlobalVariable;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsModule {
    private MipsModule(){
    }
    public static MipsModule instance = new MipsModule();
    public static MipsModule getInstance(){
        return instance;
    }
    private final ArrayList<MipsFunction> functions = new ArrayList<>();
    private final ArrayList<MipsGlobalVariable> globalVariables = new ArrayList<>();

    public static void addGlobalVariable(MipsGlobalVariable globalVariable){
        instance.globalVariables.add(globalVariable);
    }

    public static void addFunction(MipsFunction mipsFunction){
        instance.functions.add(mipsFunction);
    }

    public static MipsFunction getMainFunction(){
        return instance.functions.get(instance.functions.size() - 1);
    }
}
