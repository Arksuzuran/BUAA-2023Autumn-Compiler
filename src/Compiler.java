import config.Config;
import frontend.Lexer;
import token.Token;
import utils.IO;

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
    public void DoLexicalAnalysis(){
        System.out.println("开始词法分析!");
        if(inputText == null){
            System.out.println("文件未读入！");
            return;
        }
        Lexer lexer = new Lexer(inputText);
        lexer.doLexicalAnalysisByPass(true);

        // 输出词法分析结果 如果未指定路径则按照Config配置
        if(Config.outputLexicalAnalysis){
            // 读取词法分析结果
            StringBuilder stringBuilder = new StringBuilder();
            for(Token token : lexer.getLexerResultList()){
                stringBuilder.append(token.type).append(" ").append(token.str).append("\n");
            }
            String result = stringBuilder.toString();

            if(!Objects.equals(outputTextPath, "")){
                IO.write(outputTextPath, result);
            } else {
                IO.write(result);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("hell, word!");
        Compiler compiler = new Compiler();
        compiler.readInputFile();
        compiler.DoLexicalAnalysis();
    }
}
