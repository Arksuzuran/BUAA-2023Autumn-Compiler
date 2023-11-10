package node;

import ir.IrBuilder;
import ir.Irc;
import ir.values.Value;
import ir.values.instructions.Icmp;
import token.Token;
import token.TokenType;

/**
 * @Description 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
 * @Author
 * @Date 2023/9/21
 **/
public class EqExpNode extends Node{
    private RelExpNode relExpNode;
    private Token opToken;
    private EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode, Token opToken, EqExpNode eqExpNode) {
        super(NodeType.EqExp);
        this.relExpNode = relExpNode;
        this.opToken = opToken;
        this.eqExpNode = eqExpNode;
    }

    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
    @Override
    public void print(){
        if(opToken == null){
            relExpNode.print();
        } else {
            eqExpNode.print();
            opToken.print();
            relExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            relExpNode.check();
        } else {
            eqExpNode.check();
            relExpNode.check();
        }
    }

    /**
     *  EqExp → RelExp | EqExp ('==' | '!=') RelExp
     *  构建相等关系的指令
     * @synValue 该层EqExp比较或下层的结果。可能为i32或i1
     */
    @Override
    public void buildIr() {
        if(opToken == null){
            relExpNode.buildIr();
        }
        // EqExp → EqExp ('==' | '!=') RelExp
        // 需添加比较指令
        else{
            // 获取操作数
            eqExpNode.buildIr();
            Value opValue1 = Irc.synValue;
            relExpNode.buildIr();
            Value opValue2 = Irc.synValue;

            // 将opValue都转换为i32类型
            opValue1 = IrBuilder.buildZextInstructionIfI1(opValue1, Irc.curBlock);
            opValue2 = IrBuilder.buildZextInstructionIfI1(opValue2, Irc.curBlock);

            // 比较类型
            Icmp.CondType condType = opToken.type == TokenType.EQL ? Icmp.CondType.EQL : Icmp.CondType.NEQ;
            // 返回值为该次比较的结果
            Irc.synValue = IrBuilder.buildIcmpInstruction(opValue1, opValue2, condType, Irc.curBlock);
        }
    }
}
