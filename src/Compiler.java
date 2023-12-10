import backend.MipsBuilder;
import backend.parts.MipsModule;
import config.Config;
import frontend.Checker;
import frontend.Lexer;
import frontend.Parser;
import ir.IrBuilder;
import ir.values.Module;
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
    private Parser parser = null;
    private Checker checker = null;
    private IrBuilder irBuilder = null;
    private MipsBuilder mipsBuilder = null;
    private ArrayList<Token> lexerResultList = null;    // 词法分析结果

    private CompUnitNode compUnitNode = null;           // 语法分析结果   AST
    private boolean hasError = false;                   // 错误处理 是否有错误
    private Module irModule = null;                     // 中间代码生成结果 AST
    private MipsModule mipsModule = null;               // 目标代码生成结果 AST

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
        hasError = checker.doCheck();
        if(Config.outputErrors){
            checker.outputError();
        }
        System.out.println("=====[错误处理与符号表生成]完成=====");
    }

    public void doIrBuilding(){
        System.out.println("=====[LLVM生成]开始=====");
        irBuilder = new IrBuilder(compUnitNode);
        irModule = irBuilder.doIrBuilding();
        if(Config.outputIr){
            irBuilder.outputIr();
        }
        System.out.println("=====[LLVM生成]完成=====");
    }

    public void doMipsBuilding() {
        System.out.println("=====[MIPS生成]开始=====");
        mipsBuilder = new MipsBuilder(irModule);
        mipsBuilder.doMipsBuilding();
        if(Config.outputMIPS){
            mipsBuilder.outputMIPS();
        }
        System.out.println("=====[MIPS生成]完成=====");
    }

    public void doCompiling() {
        System.out.println("[编译]开始");
        readInputFile();
        doLexicalAnalysis();
        doParsing();
        doChecking();
        if(!hasError){
            doIrBuilding();
            if(Config.genMips){
                doMipsBuilding();
            }
        }
        System.out.println("[编译]执行完成!");
    }

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.doCompiling();
    }
}
