package utils;

/**
 * @Description 为了解决&&防止莫名其妙的RE，对所有编译阶段的主分析函数添加try-catch。虽然不知道为什么，但这的确有用
 * @Author  H1KARI
 * @Date 2023/12/11
 **/
public interface CompilePhase {
    /**
     * 执行主分析过程
     */
    void process();

    /**
     * 输出结果
     */
    void outputResult();
}
