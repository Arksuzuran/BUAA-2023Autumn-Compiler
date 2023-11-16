package node;

import ir.IrBuilder;
import ir.Irc;
import ir.types.PointerType;
import ir.values.constants.ConstInt;
import token.Token;

/**
 * @Description 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
 * @Author
 * @Date 2023/9/20
 **/
public class PrimaryExpNode extends Node{
    private Token lparentToken;
    private ExpNode expNode;
    private Token rparentToken;
    private LValNode lValNode;
    private NumberNode numberNode;

    public PrimaryExpNode(Token lparentToken, ExpNode expNode, Token rparentToken, LValNode lValNode, NumberNode numberNode) {
        super(NodeType.PrimaryExp);
        this.lparentToken = lparentToken;
        this.expNode = expNode;
        this.rparentToken = rparentToken;
        this.lValNode = lValNode;
        this.numberNode = numberNode;
    }

    @Override
    public void print(){
        if(lparentToken!=null){
            lparentToken.print();
            expNode.print();
            rparentToken.print();
        } else if(lValNode != null) {
            lValNode.print();
        } else {
            numberNode.print();
        }
        printNodeType();
    }
    // 基本表达式   PrimaryExp → '(' Exp ')' | LVal | Number
    @Override
    public void check() {
        if(lparentToken!=null){
            expNode.check();
        } else if(lValNode != null) {
            lValNode.check();
        }
    }

    // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number

    /**
     * 如果当前不在计算指针实参，则会使用load指令加载指针所指变量的值
     */
    @Override
    public void buildIr() {
        // 常量
        if(Irc.isBuildingConstExp){
            if(lparentToken!=null){
                expNode.buildIr();
            } else if(lValNode != null) {
                lValNode.buildIr();
            } else {
                numberNode.buildIr();
            }
        }
        // 非常量
        else{
            // LVal
            // 指针只能从左值中得出
            if(lValNode != null) {
                // 正在加载函数参数，且要求指针类型的value，则不进行load
                // 需要消除标记，因为后续还可能再进入primaryExp
                if(Irc.isBuildingPointerRParam){
                    Irc.isBuildingPointerRParam = false;
                    lValNode.buildIr();
                }
                // 要求int类型的value，此处应该检查load
                // 如果是指针类型 那么进行加载
                // 指针类型在通常状态下的加载，即在此实现
                else{
                    lValNode.buildIr();
                    if(Irc.synValue.getType() instanceof PointerType){
                        Irc.synValue = IrBuilder.buildLoadInstruction(Irc.synValue, Irc.curBlock);
                    }
                }
            }
            // '(' Exp ')'
            else if(lparentToken!=null){
                expNode.buildIr();
            }
            // Number
            else {
                numberNode.buildIr();
            }
        }
    }

    public int getDim(){
        // '(' Exp ')'
        if(lparentToken!=null){
            return expNode.getDim();
        }
        // LVal
        else if(lValNode != null) {
            return lValNode.getDim();
        }
        // Number
        else {
            return 0;
        }
    }
}
