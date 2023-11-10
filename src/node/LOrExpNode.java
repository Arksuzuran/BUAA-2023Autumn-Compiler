package node;

import ir.IrBuilder;
import ir.Irc;
import ir.values.BasicBlock;
import token.Token;

/**
 * @Description 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
 * @Author
 * @Date 2023/9/20
 **/
public class LOrExpNode extends Node{
   private LAndExpNode lAndExpNode;
   private Token opToken;
   private LOrExpNode lOrExpNode;

    public void setTrueBranch(BasicBlock trueBranch) {
        this.trueBranch = trueBranch;
    }
    public void setFalseBranch(BasicBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    /**
     * 满足条件为真时跳转到的分支
     */
    private BasicBlock trueBranch;
    /**
     * 满足条件为假时跳转到的分支
     */
    private BasicBlock falseBranch;

    public LOrExpNode(LAndExpNode lAndExpNode, Token opToken, LOrExpNode lOrExpNode) {
        super(NodeType.LOrExp);
        this.lAndExpNode = lAndExpNode;
        this.opToken = opToken;
        this.lOrExpNode = lOrExpNode;
    }

    // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    public void print(){
        if(opToken == null){
            lAndExpNode.print();
        } else {
            lOrExpNode.print();
            opToken.print();
            lAndExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            lAndExpNode.check();
        } else {
            lOrExpNode.check();
            lAndExpNode.check();
        }
    }

    /**
     * 短路求值：or
     * 在短路求值中，所有条件判断都被拆分，并被转化为基本块间的跳转关系。
     * 因此LOrExp的作用是为下层的LAnd构建跳转块。
     *
     * 对于LOrExp || LAndExp来说：
     * 如果LOrExp为真，那么直接跳转到trueBranch即可。
     * 如果LOrExp为假，那么应该创建并跳转到一个新的块(Short-circuit evaluation)，在该块内退化为单LAndExp的情形。
     */
    @Override
    public void buildIr() {
        // LOrExp → LAndExp
        if(opToken == null){
            handleSingleLAndExp();
        }
        // LOrExp → LOrExp '||' LAndExp
        else{
            // 新建一个块，作为LOrExp的falseBranch
            BasicBlock scBranch = IrBuilder.buildBasicBlock(Irc.curFunction);
            lOrExpNode.setTrueBranch(trueBranch);
            lOrExpNode.setFalseBranch(scBranch);
            lOrExpNode.buildIr();

            // 切换到新建的scBranch块内，在这个块内构建LAndExp
            Irc.curBlock = scBranch;
            handleSingleLAndExp();
        }
    }

    /**
     * LOrExp → LAndExp
     * 处理单LAndExp
     * 设置好branch后递归下降处理即可
     */
    private void handleSingleLAndExp(){
        lAndExpNode.setTrueBranch(trueBranch);
        lAndExpNode.setFalseBranch(falseBranch);
        lAndExpNode.buildIr();
    }
}
