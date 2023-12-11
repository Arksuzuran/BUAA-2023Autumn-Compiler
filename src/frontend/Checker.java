package frontend;

import config.Config;
import error.ErrorHandler;
import node.CompUnitNode;
import utils.CompilePhase;

/**
 * @Description 在这一遍中构建符号表并进行错误处理
 * @Author
 * @Date 2023/10/13
 **/
public class Checker implements CompilePhase {
    private CompUnitNode compUnitNode;

    public Checker(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
    }

    @Override
    public void process() {
        compUnitNode.check();
    }

    @Override
    public void outputResult() {
        if(Config.outputErrors){
            ErrorHandler.printErrors();
        }
    }
    public boolean hasError(){
        return ErrorHandler.hasError();
    }
}
