package ir.values.instructions;

import ir.types.IntType;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Value;
import utils.IrTool;

/**
 * @Description 有符号除法
 * <result> = sdiv <ty> <op1>, <op2>
 * @Author
 * @Date 2023/10/30
 **/
public class Sdiv extends AresInstruction{

    public Sdiv(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }

    // %6 = div i32 5, %5
    @Override
    public String toString(){
        return getAresIrString("sdiv");
    }
}
