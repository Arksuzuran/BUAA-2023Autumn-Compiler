package backend;

import backend.parts.MipsGlobalVariable;
import backend.parts.MipsModule;
import ir.values.Module;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/17
 **/
public class MipsBuilder {
    private Module irModule;

    public MipsBuilder(Module irModule) {
        this.irModule = irModule;
    }

    public void doMipsBuilding(){
        irModule.buildMips();
    }

    public static void buildGlobalVariable(MipsGlobalVariable globalVariable){
        MipsModule.addGlobalVariable(globalVariable);
    }
}
