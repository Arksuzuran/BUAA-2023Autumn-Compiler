package config;

/**
 * @Description 配置常量
 * @Author
 * @Date 2023/9/17
 **/
public class Config {
    // 项目根路径
    private static final String rootPath = System.getProperty("user.dir");
    // 提交评测路径
    public static String inputFilePath = "testfile.txt";
    public static String outputFilePath = "output.txt";
    // 当前是否正在本地测试（false提交时用，true本地测试用）
    public static boolean atLocalTest = false;
    // 本地测试路径
    public static String localInputFilePath = rootPath + "/testcases/testfile.txt";
    public static String localOutputFilePath = rootPath + "/testcases/output.txt";

    // 是否输出词法分析结果
    public static boolean outputLexicalAnalysis = false;
    // 是否输出语法分析结果
    public static boolean outputParsing = true;

}
