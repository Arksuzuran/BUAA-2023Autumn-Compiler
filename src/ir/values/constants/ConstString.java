package ir.values.constants;

import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.ValueType;
import utils.IrTool;

/**
 * @Description TODO
 * * char s[10] = "Hello.";
 * \@s = dso_local global [10 x i8] c"Hello.\00\00\00\00", align 1
 * @Author
 * @Date 2023/11/16
 **/
public class ConstString extends Constant{
    /**
     * 构建Mips代码时调用
     * 将llvm中的\0a转换为\n
     */
    public String getContent() {
        return content.replace("\\0a", "\\n");
    }

    private String content;
    public ConstString(String content) {
        super(new ArrayType(new IntType(8), IrTool.getFormatStringLen(content) + 1));
        this.content = content;
    }

    @Override
    public String toString(){
        return " c\"" + content + "\\00\"";
    }
}
