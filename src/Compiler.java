import config.Config;
import exception.LexerException;
import frontend.Lexer;
import frontend.Token;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @Description entrance
 * @Author HIKARI
 * @Date 2023/9/14
 **/
public class Compiler {
    public String inputTextPath = "";
    public String outputTextPath = "";
    public File inputFile = null;
    public String inputText = "";
    Compiler(String inputTextPath, String outputTextPath){
        this.inputTextPath = inputTextPath;
        this.outputTextPath = outputTextPath;
    }
    // 读取输入文件
    public String readInputFile(){
        // 需要根据当前类的路径
        inputFile = new File(inputTextPath);
        if(Config.atLocalTest){
            inputFile = new File(Config.localInputFilePath);
        }
        System.out.println("读取输入文件：" + inputFile.getAbsolutePath());
        try {
            FileReader reader = new FileReader(inputFile);
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[10];
            int size;
            while((size = reader.read(buffer)) != -1){
                stringBuilder.append(buffer, 0, size);
            }
            inputText = stringBuilder.toString();
        } catch (Exception e){
            System.out.println("文件读入失败！" + e);
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

        // 输出词法分析结果
        if(Config.outputLexicalAnalysis){
            // 读取词法分析结果
            StringBuilder stringBuilder = new StringBuilder();
            for(Token<?> token : lexer.getLexerResultList()){
                stringBuilder.append(token.type).append(" ").append(token.val).append("\n");
            }
            String result = stringBuilder.toString();

            try{
                File file = new File(outputTextPath);
                if(Config.atLocalTest){
                    file = new File(Config.localOutputFilePath);
                }
                System.out.println("输出词法分析文件至：" + file.getAbsolutePath());
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(result.getBytes());
            } catch (IOException e){
                System.out.println("词法分析结果文件输出失败" + e);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("hell, word!");
        Compiler compiler = new Compiler(Config.inputFilePath, Config.outputFilePath);
        compiler.readInputFile();
        compiler.DoLexicalAnalysis();
    }
}
