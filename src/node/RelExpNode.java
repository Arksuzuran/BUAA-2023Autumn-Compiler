package node;

import ir.IrBuilder;
import ir.Irc;
import ir.values.Value;
import ir.values.instructions.Icmp;
import token.Token;

/**
 * @Description 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddEx
 * @Author
 * @Date 2023/9/21
 **/
public class RelExpNode extends Node{
    private AddExpNode addExpNode;
    private Token opToken;
    private RelExpNode relExpNode;

    public RelExpNode(AddExpNode addExpNode, Token opToken, RelExpNode relExpNode) {
        super(NodeType.RelExp);
        this.addExpNode = addExpNode;
        this.opToken = opToken;
        this.relExpNode = relExpNode;
    }

    // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddEx
    @Override
    public void print(){
        if(opToken == null){
            addExpNode.print();
        } else {
            relExpNode.print();
            opToken.print();
            addExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            addExpNode.check();
        } else {
            relExpNode.check();
            addExpNode.check();
        }
    }

    /**
     * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
     * 构建大小比较关系的指令
     * @synValue 本层比较或下层的结果。可能为i1或i32
     */
    @Override
    public void buildIr() {
        if(opToken == null){
            addExpNode.buildIr();
        }
        // RelExp ('<' | '>' | '<=' | '>=') AddExp
        else{
            relExpNode.buildIr();
            Value opValue1 = Irc.synValue;
            addExpNode.buildIr();
            Value opValue2 = Irc.synValue;

            opValue1 = IrBuilder.buildZextInstructionIfI1(opValue1, Irc.curBlock);
            opValue2 = IrBuilder.buildZextInstructionIfI1(opValue2, Irc.curBlock);

            Icmp.CondType condType;
            switch (opToken.type){
                case LSS -> condType = Icmp.CondType.LSS;    // <
                case LEQ -> condType = Icmp.CondType.LEQ;    // <=
                case GRE -> condType = Icmp.CondType.GRE;    // >
                default -> condType = Icmp.CondType.GEQ;    // >=
            }
            Irc.synValue = IrBuilder.buildIcmpInstruction(opValue1, opValue2, condType, Irc.curBlock);
        }
    }
}
