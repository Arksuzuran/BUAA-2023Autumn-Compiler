package utils;

import config.Config;
import token.Token;

import java.io.*;

/**
 * @Description 读写文件工具类
 * @Author  H1KARI
 * @Date 2023/9/18
 **/
public class IO {
    public enum IOType{
        PARSER, LEXER, CHECKER
    }
    // 从指定路径读取文件 将内容存入字符串中
    public static String read(String filePath){
        // 需要根据当前类的路径
        File inputFile = new File(filePath);
        String text = "";
        System.out.println("读取输入文件：" + inputFile.getAbsolutePath());
        try {
            FileReader reader = new FileReader(inputFile);
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[10];
            int size;
            while((size = reader.read(buffer)) != -1){
                stringBuilder.append(buffer, 0, size);
            }
            text = stringBuilder.toString();
        } catch (Exception e){
            System.out.println("文件读入失败！" + e);
        }
        return text;
    }

    // 将字符串追加写入指定路径的文件
    // appending:   追加写
    // println:     在末尾附加换行符
    public static void write(String filePath, String content, boolean appending, boolean println){
        File file = new File(filePath);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            if(!appending){
                System.out.println("覆写至文件：" + file.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes());
            }
            else{
                FileWriter fileWriter = new FileWriter(file, true);
                if(println){
                    fileWriter.write(content + '\n');
                } else {
                    fileWriter.write(content);
                }
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e){
            System.out.println("文件输出失败" + e);
        }
    }
    // 根据当前输出类型写入已配置好的路径
    // appending:   追加写
    // println:     在末尾附加换行符
    public static void write(IOType ioType, String content, boolean appending, boolean println){
        String filePath = getPath(true, ioType);
        write(filePath, content, appending, println);
    }

    // 默认读入已配置好的路径
    public static String read(){
        String filePath = getPath(false, IOType.LEXER);
        return read(filePath);
    }

    // 计算路径
    public static String getPath(boolean output, IOType ioType){
        if(output){
            switch (ioType){
                case LEXER, PARSER ->{
                    return Config.atLocalTest ? Config.localOutputFilePath : Config.outputFilePath;
                }
                case CHECKER -> {
                    return Config.atLocalTest ? Config.localOutputErrorFilePath : Config.outputErrorFilePath;
                }
            }
            return "";
        } else {
            return Config.atLocalTest ? Config.localInputFilePath : Config.inputFilePath;
        }
    }
}
