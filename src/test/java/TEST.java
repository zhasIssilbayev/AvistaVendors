import kz.project.BotManager;
import kz.project.bots.BotTypes;
import kz.project.dto.BotLoginRequest;
import kz.project.exception.LoginFailedException;

import java.util.Map;

public class TEST {
    public static void main(String[] args) {
        String login = "ledo1@ledo-markt.de";          // ← замени на свой
        String password = "Gunz76661";    // ← замени на свой
        String url = "https://www.gunz.cc/ru";

        BotManager manager = new BotManager();
        Map<String, String> cookies = null;
        try {
            cookies = manager.loginAndGetCookies(new BotLoginRequest(BotTypes.GUNZ, login, password, url));
        } catch (LoginFailedException e) {
            System.out.println("Авторизация не выполнена.");
        }

        System.out.println("Cookies:");
        cookies.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}