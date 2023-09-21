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

    // 将字符串覆写入指定路径的文件
    public static void write(String filePath, String content){
        try{
            File file = new File(filePath);
            System.out.println("覆写至文件：" + file.getAbsolutePath());
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
        } catch (IOException e){
            System.out.println("文件输出失败" + e);
        }
    }

    // 默认覆写入已配置好的路径
    public static void write(String content){
        String filePath = Config.atLocalTest ? Config.localOutputFilePath : Config.outputFilePath;
        write(filePath, content);
    }

    // 将字符串追加写入指定路径的文件
    public static void write(String filePath, String content, boolean appending){
        if(!appending){
            write(filePath, content);
            return;
        }
        try{
            File file = new File(filePath);
//            System.out.println("追加写至文件：" + file.getAbsolutePath() + "内容：" + content);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(content + '\n');
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e){
            System.out.println("文件输出失败" + e);
        }
    }
    // 默认写入已配置好的路径
    public static void write(String content, boolean appending){
        if(!appending){
            write(content);
            return;
        }
        String filePath = Config.atLocalTest ? Config.localOutputFilePath : Config.outputFilePath;
        write(filePath, content, appending);
    }

    // 默认读入已配置好的路径
    public static String read(){
        String filePath = Config.atLocalTest ? Config.localInputFilePath : Config.inputFilePath;
        return read(filePath);
    }

    // 重载 写Token
    public static void write(Token token){
        String filePath = Config.atLocalTest ? Config.localOutputFilePath : Config.outputFilePath;
        write(filePath, token.toString());
    }
}
