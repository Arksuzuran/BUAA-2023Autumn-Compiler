package error;

import java.util.ArrayList;

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


}
