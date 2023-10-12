import config.Config;
import frontend.Lexer;
import frontend.Parser;
import token.Token;
import utils.IO;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Description entrance
 * @Author HIKARI
 * @Date 2023/9/14
 **/
public class Compiler {
    public String inputTextPath;
    public String outputTextPath;
    public String inputText = "";

    private Lexer lexer = null;
    private ArrayList<Token> lexerResultList = null;    // 词法分析结果
    private Parser parser = null;
    Compiler(){
        this.inputTextPath = "";
        this.outputTextPath = "";
    }
    Compiler(String inputTextPath, String outputTextPath){
        this.inputTextPath = inputTextPath;
        this.outputTextPath = outputTextPath;
    }
    // 读取输入文件 如果未指定路径则按照Config配置
    public String readInputFile(){
        if(!Objects.equals(inputTextPath, "")){
            inputText = IO.read(inputTextPath);
        } else {
            inputText = IO.read();
        }
        return inputText;
    }
    // 词法分析
    public void doLexicalAnalysis(){
        System.out.println("开始词法分析!");
        if(inputText == null){
            System.out.println("文件未读入！");
            return;
        }

        lexer = new Lexer(inputText);
        lexer.doLexicalAnalysisByPass(false);
        lexerResultList = lexer.getLexerResultList();

        // 输出词法分析结果 如果未指定路径则按照Config配置
        if(Config.outputLexicalAnalysis){
            // 读取词法分析结果
            StringBuilder stringBuilder = new StringBuilder();
            for(Token token : lexerResultList){
                stringBuilder.append(token.type).append(" ").append(token.str).append("\n");
            }
            String result = stringBuilder.toString();
            // 输出词法分析结果至文件
            if(!Objects.equals(outputTextPath, "")){
                IO.write(outputTextPath, result);
            } else {
                IO.write(result);
            }
        }
    }

    // 语法分析
    public void doParsing(){
        System.out.println("开始语法分析!");
        parser = new Parser(lexerResultList);
        parser.doParsing();
        if(Config.outputParsing){
            parser.outputParsingResult();
        }
    }

    public static void main(String[] args) {
        System.out.println("hell, word!");
        Compiler compiler = new Compiler();
        compiler.readInputFile();
        compiler.doLexicalAnalysis();
        compiler.doParsing();
        System.out.println("执行完成!");
    }
}
