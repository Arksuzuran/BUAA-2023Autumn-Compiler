package backend.parts;

import ir.values.GlobalVariable;

import java.util.ArrayList;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsModule {

    private final ArrayList<MipsFunction> functions = new ArrayList<>();
    private final ArrayList<GlobalVariable> globalVariables = new ArrayList<>();

    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariables.add(globalVariable);
    }

    public void addFunction(MipsFunction mipsFunction){
        functions.add(mipsFunction);
    }

    public MipsFunction getMainFunction(){
        return functions.get(functions.size() - 1);
    }
}
