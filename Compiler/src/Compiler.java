import exception.LexerException;
import frontend.Lexer;
import frontend.Token;

/**
 * @Description entrance
 * @Author HIKARI
 * @Date 2023/9/14
 **/
public class Compiler {

    public static void main(String[] args) {
        System.out.println("hell, word!");
        String text = "123 HH12_\n a_1\r\n\n\n 1H";
        Lexer lexer = new Lexer(text);
        Token<?> token = null;
        try {
            do {
                token = lexer.next();
                if (token != null) {
                    System.out.println(token);
                }
            } while (token != null);

        } catch (LexerException e) {
            System.out.println(e);
        }

    }
}
