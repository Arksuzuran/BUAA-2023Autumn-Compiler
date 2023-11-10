package config;

/**
 * @Description 配置常量
 * @Author
 * @Date 2023/9/17
 **/
public class Config {
    // 项目根路径
    private static final String rootPath = System.getProperty("user.dir");

    // ============== 输出配置开关 ================
    // 当前是否正在本地测试（false提交时用，true本地测试用）
    public static boolean atLocalTest = true;
//    public static boolean atLocalTest = false;
    // 是否输出词法分析结果
    public static boolean outputLexicalAnalysis = false;
    // 是否输出语法分析结果
    public static boolean outputParsing = false;
    // 是否输出错误处理结果
    public static boolean outputErrors = false;
    // 是否输出中间代码
    public static boolean outputIr = true;

    // ============== 本地测试路径 ================
    // 输入文件路径
    public static String localInputFilePath = rootPath + "/testcases/testfile3.txt";
    // 默认输出文件路径
    public static String localOutputFilePath = rootPath + "/testcases/output.txt";
    // 错误处理输出文件路径
    public static String localOutputErrorFilePath = rootPath + "/testcases/error.txt";
    // 中间代码生成输出文件路径
    public static String localOutputIRFilePath = rootPath + "/testcases/llvm_ir.txt";

    // ============== 提交评测路径 ================
    // 输入文件路径
    public static String inputFilePath = "testfile.txt";
    // 默认输出文件路径
    public static String outputFilePath = "output.txt";
    // 错误处理输出文件路径
    public static String outputErrorFilePath = "error.txt";
    // LLVM输出文件路径
    public static String outputIRFilePath = "llvm_ir.txt";
}
