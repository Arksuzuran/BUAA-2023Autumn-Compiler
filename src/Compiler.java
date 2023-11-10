import config.Config;
import frontend.Checker;
import frontend.Lexer;
import frontend.Parser;
import ir.IrBuilder;
import node.CompUnitNode;
import token.Token;
import utils.IO;

import java.util.ArrayList;

/**
 * @Description entrance
 * @Author HIKARI
 * @Date 2023/9/14
 **/
public class Compiler {
    public String inputText = "";

    private Lexer lexer = null;
    private ArrayList<Token> lexerResultList = null;    // 词法分析结果
    private Parser parser = null;
    private CompUnitNode compUnitNode = null;           // 语法分析结果   AST
    private Checker checker = null;
    private IrBuilder irBuilder = null;

    // 读取输入文件 如果未指定路径则按照Config配置
    public String readInputFile(){
        inputText = IO.read();
        return inputText;
    }
    // 词法分析
    public void doLexicalAnalysis(){
        System.out.println("=====[词法分析]开始=====");
        if(inputText == null){
            System.out.println("文件未读入！");
            return;
        }

        lexer = new Lexer(inputText);
        lexer.doLexicalAnalysisByPass(false);
        lexerResultList = lexer.getLexerResultList();

        // 输出词法分析结果 如果未指定路径则按照Config配置
        if(Config.outputLexicalAnalysis){
            lexer.outputLexicalResult();
        }
        System.out.println("=====[词法分析]完成!=====");
    }

    // 语法分析
    public void doParsing(){
        System.out.println("=====[语法分析]开始=====");
        parser = new Parser(lexerResultList);
        parser.doParsing();
        if(Config.outputParsing){
            parser.outputParsingResult();
        }
        compUnitNode = parser.getParsingResultNode();
        System.out.println("=====[语法分析]完成!=====");
    }

    // 符号表生成和错误处理
    public void doChecking(){
        System.out.println("=====[错误处理与符号表生成]开始=====");
        checker = new Checker(compUnitNode);
        checker.doCheck();
        if(Config.outputErrors){
            checker.outputError();
        }
        System.out.println("=====[错误处理与符号表生成]完成=====");
    }

    public void doIrBuilding(){
        System.out.println("=====[LLVM生成]开始=====");
        irBuilder = new IrBuilder(compUnitNode);
        irBuilder.doIrBuilding();
        if(Config.outputIr){
            irBuilder.outputIr();
        }
        System.out.println("=====[LLVM生成]完成=====");
    }

    public static void main(String[] args) {
        System.out.println("[编译]开始");
        Compiler compiler = new Compiler();
        compiler.readInputFile();
        compiler.doLexicalAnalysis();
        compiler.doParsing();
        compiler.doChecking();
        compiler.doIrBuilding();
        System.out.println("[编译]执行完成!");
    }
}
