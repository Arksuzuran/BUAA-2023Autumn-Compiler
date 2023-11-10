package frontend;

import error.ErrorHandler;
import node.CompUnitNode;

/**
 * @Description 在这一遍中构建符号表并进行错误处理
 * @Author
 * @Date 2023/10/13
 **/
public class Checker {
    private CompUnitNode compUnitNode;

    public Checker(CompUnitNode compUnitNode) {
        this.compUnitNode = compUnitNode;
    }

    public void doCheck(){
        compUnitNode.check();
    }
    public void outputError(){
        ErrorHandler.printErrors();
    }
}
