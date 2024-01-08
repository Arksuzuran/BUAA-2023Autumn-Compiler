package config;

/**
 * @Description 配置常量
 * @Author
 * @Date 2023/9/17
 **/
public class Config {
    // 项目根路径
    private static final String rootPath = System.getProperty("user.dir");

    // ============== 输出配置开关 ================、
    // 当前是否正在本地测试（false提交时用，true本地测试用）
    public static boolean atLocalTest = true;
//    public static boolean atLocalTest = false;
    // 是否输出词法分析结果
    public static boolean outputLexicalAnalysis = false;
    // 是否输出语法分析结果
    public static boolean outputParsing = false;
    // 是否输出错误处理结果
    public static boolean outputErrors = true;
    // 是否输出中间代码
    public static boolean outputIr = true;
    // 是否输出目标代码
    public static boolean outputMIPS = true;

    // ============== 优化配置开关 ================
    // 是否生成Mips 为false则只生成llvm,不进入后端
    public static boolean genMips = true;
    // 是否开启Mem2Reg优化
//    public static boolean openMem2RegOpt = false;
    public static boolean openMem2RegOpt = true;

    // 是否开启窥孔优化
//    public static boolean openPeepHoleOpt = false;
    public static boolean openPeepHoleOpt = true;
    // 是否开启寄存器分配
    public static boolean openRegAllocOpt = true;
    // 是否启用防止[莫名其妙RE但本地可以跑通]的保护
//    public static boolean REProtect = false;
    public static boolean REProtect = true;

    // ============== 本地测试路径 ================
    // 输入文件路径
    public static String localInputFilePath = rootPath + "/testcases/testfile3.txt";
    // 默认输出文件路径
    public static String localOutputFilePath = rootPath + "/testcases/output.txt";
    // 错误处理输出文件路径
    public static String localOutputErrorFilePath = rootPath + "/testcases/error.txt";
    // LLVM生成输出文件路径
    public static String localOutputIRFilePath = rootPath + "/testcases/llvm_ir.txt";
    // MIPS生成输出文件路径
    public static String localOutputMIPSFilePath = rootPath + "/testcases/mips.txt";

    // ============== 提交评测路径 ================
    // 输入文件路径
    public static String inputFilePath = "testfile.txt";
    // 默认输出文件路径
    public static String outputFilePath = "output.txt";
    // 错误处理输出文件路径
    public static String outputErrorFilePath = "error.txt";
    // LLVM输出文件路径
    public static String outputIRFilePath = "llvm_ir.txt";
    // MIPS输出文件路径
    public static String outputMIPSFilePath = "mips.txt";

    // =============== 控制台相关输出开关 ==================
    // 是否在控制台输出词法分析的tokens
    public static boolean debuggingTokens = false;
    // 是否开启控制台的debug输出
    public static boolean debugging = false;
}
