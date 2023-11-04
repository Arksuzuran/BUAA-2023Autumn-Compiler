package node;

import ir.IrBuilder;
import ir.Irc;
import ir.values.Value;
import token.Token;
import token.TokenType;

/**
 * @Description 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
 * @Author
 * @Date 2023/9/20
 **/
public class AddExpNode extends Node{
    private MulExpNode mulExpNode;
    private Token opToken;
    private AddExpNode addExpNode;

    public AddExpNode(MulExpNode mulExpNode, Token opToken, AddExpNode addExpNode) {
        super(NodeType.AddExp);
        this.mulExpNode = mulExpNode;
        this.opToken = opToken;
        this.addExpNode = addExpNode;
    }

    // 加减表达式 AddExp → MulExp | MulExp ('+' | '−') AddExp
    // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    @Override
    public void print(){
        if(opToken == null){
            mulExpNode.print();
        } else {
            addExpNode.print();
            opToken.print();
            mulExpNode.print();
        }
        printNodeType();    // 紧跟在后输出
    }

    @Override
    public void check() {
        if(opToken == null){
            mulExpNode.check();
        } else {
            addExpNode.check();
            mulExpNode.check();
        }
    }

    /**
     * 带有常数优化
     * 即如果是从constExp进入，那么需要直接计算出结果并通过synValue上传
     */
    @Override
    public void buildIr() {
        // 需要计算出常数 不过无需传递synValue
        if(Irc.inConstExp){
            // 单操作数 MulExp
            // synInt在下层进一步计算
            if(opToken == null){
                mulExpNode.buildIr();
            }
            // 双操作数 AddExp ('+' | '−') MulExp
            else{
                addExpNode.buildIr();
                int ans = Irc.synInt;
                mulExpNode.buildIr();
                // 减法或者加法
                if(opToken.type == TokenType.MINU){
                    ans -= Irc.synInt;
                }
                else{
                    ans += Irc.synInt;
                }
                Irc.synInt = ans;
            }
        }
        // 非常数表达式
        else{
            // 单操作数 MulExp
            // synInt在下层进一步计算
            if(opToken == null){
                mulExpNode.buildIr();
            }
            // 双操作数 AddExp ('+' | '−') MulExp
            else{
                addExpNode.buildIr();
                Value opValue1 = Irc.synValue;
                mulExpNode.buildIr();
                Value opValue2 = Irc.synValue;
                // 减法或者加法
                if(opToken.type == TokenType.MINU){
                    Irc.synValue = IrBuilder.buildAddInstruction(opValue1, opValue2, Irc.curBlock);
                }
                else{
                    Irc.synValue = IrBuilder.buildSubInstruction(opValue1, opValue2, Irc.curBlock);
                }
            }
        }
    }

    public int getDim() {
        return mulExpNode.getDim();
    }
}
