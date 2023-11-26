package error;

import utils.IO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @Description 记录错误 单例模式
 * @Author
 * @Date 2023/10/13
 **/
public class ErrorHandler {
    // 存储错误们
    private final ArrayList<Error> errors;

    // 记录当前
    // 全局单例模式
    private ErrorHandler(){
        this.errors = new ArrayList<>();
    }
    private static final ErrorHandler instance = new ErrorHandler();
    public static ErrorHandler getInstance(){
        return instance;
    }

    // 记录错误到全局单例
    public static void addError(Error error){
        if(!instance.errors.contains(error)){
            instance.errors.add(error);
        }
    }
    public static boolean hasError(){
        return !instance.errors.isEmpty();
    }
    // 获取输出到文件的字符串
    public static String getOutputString(){
        instance.errors.sort(new Comparator<Error>() {
            @Override
            public int compare(Error o1, Error o2) {
                if(o1.getLineNum() ==  o2.getLineNum()){
                    return o1.getType().toString().compareTo(o2.getType().toString());
                }
                return o1.getLineNum() - o2.getLineNum();
            }
        });
        StringBuilder stringBuilder = new StringBuilder();
        for(Error error : getInstance().errors){
            stringBuilder.append(error.getOutputString());
        }
        return stringBuilder.toString();
    }
    // 将错误打印输出到文件
    public static void printErrors(){
        IO.write(IO.IOType.CHECKER, getOutputString(), false, false);
    }

}
