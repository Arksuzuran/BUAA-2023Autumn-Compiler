package ir.values.instructions;

import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description TODO
 * ret <type> <value>   或者
 * ret void
 * @Author
 * @Date 2023/10/30
 **/
public class Ret extends Instruction{
    /**
     * 无返回值 ret void
     */
    public Ret(BasicBlock parent) {
        super("", new VoidType(), parent);
    }

    /**
     *  带返回值 ret <type> <value>
     * @param returnValue   返回值
     */
    public Ret(BasicBlock parent, Value returnValue){
        super("", returnValue.getType(), parent, returnValue);
    }

    // ret i32 %24
    // ret void
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ret ");  // "ret "
        if(getType() instanceof VoidType){
            stringBuilder.append("void");         // "void"
        } else {
            stringBuilder.append(IrTool.tnstr(getOperands().get(0)));          // "i32 %24"
        }

        return stringBuilder.toString();
    }
}
