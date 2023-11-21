package node;

import ir.IrBuilder;
import ir.Irc;
import ir.values.Value;
import ir.values.constants.ConstInt;
import ir.values.instructions.Mul;
import ir.values.instructions.Sdiv;
import token.Token;
import token.TokenType;

/**
 * @Description 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 * @Author
 * @Date 2023/9/21
 **/
public class MulExpNode extends Node{
    private UnaryExpNode unaryExpNode;
    private Token opToken;
    private MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.MulExp);
        this.unaryExpNode = unaryExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    @Override
    public void print(){
        if(opToken == null){
            unaryExpNode.print();
        } else {
            mulExpNode.print();
            opToken.print();
            unaryExpNode.print();
        }
        printNodeType();
    }

    @Override
    public void check() {
        if(opToken == null){
            unaryExpNode.check();
        } else {
            mulExpNode.check();
            unaryExpNode.check();
        }
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    @Override
    public void buildIr() {
        // 常量
        if(Irc.isBuildingConstExp){
            if(opToken == null){
                unaryExpNode.buildIr();
            }
            else{
                mulExpNode.buildIr();
                int ans = Irc.synInt;
                unaryExpNode.buildIr();
                if(opToken.type == TokenType.MULT){
                    ans *= Irc.synInt;
                }
                else if(opToken.type == TokenType.DIV){
                    ans /= Irc.synInt;
                }
                else if(opToken.type == TokenType.MOD){
                    ans %= Irc.synInt;
                }
                Irc.synInt = ans;
            }
        }
        // 非常量
        else{
            if(opToken == null){
                unaryExpNode.buildIr();
            }
            else{
                mulExpNode.buildIr();
                Value opValue1 = Irc.synValue;
                unaryExpNode.buildIr();
                Value opValue2 = Irc.synValue;
                if(opToken.type == TokenType.MULT){
                    Irc.synValue = IrBuilder.buildMulInstruction(opValue1, opValue2, Irc.curBlock);
                }
                else if(opToken.type == TokenType.DIV){
                    Irc.synValue = IrBuilder.buildSdivInstruction(opValue1, opValue2, Irc.curBlock);
                }
                // 取模可以替换为以下公式
                // x % y = x - ( x / y ) * y
                else if(opToken.type == TokenType.MOD){
                    // y为正的常数（因为负常数会在UnaryExp中联合符号而被解析为Sub指令的Value） 那么可以进一步优化
                    // y是1，应该直接取模运算, 交由后端优化
                    if(opValue2 instanceof ConstInt && ((ConstInt) opValue2).getValue() == 1){
                        Irc.synValue = IrBuilder.buildSremInstruction(opValue1, opValue2, Irc.curBlock);
                    }
                    // 不能优化，则直接替换为公式
                    else{
                        Sdiv div = IrBuilder.buildSdivInstruction(opValue1, opValue2, Irc.curBlock);
                        Mul mul = IrBuilder.buildMulInstruction(div, opValue2, Irc.curBlock);
                        Irc.synValue = IrBuilder.buildSubInstruction(opValue1, mul, Irc.curBlock);
                    }
                }
            }
        }
    }

    public int getDim() {
        return unaryExpNode.getDim();
    }
}
