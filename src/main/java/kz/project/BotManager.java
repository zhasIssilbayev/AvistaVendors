package kz.project;

import kz.project.bots.*;
import kz.project.configuration.BotProperties;
import kz.project.dto.BotLoginRequest;
import kz.project.dto.MyCredentials;
import kz.project.exception.LoginFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BotManager {
    private static final Logger log = LoggerFactory.getLogger(BotManager.class);

    @Autowired
    private BotProperties botProperties;

    public Map<String, String> loginAndGetCookies(BotLoginRequest req) throws LoginFailedException {
        log.info("Попытка логина через {}", req.botType());
        try (BasicDriverBot bot = createBot(req)) {
            bot.setupDriver();
            bot.login();
            Set<org.openqa.selenium.Cookie> raw = bot.getCookies();
            return raw.stream().collect(Collectors.toMap(org.openqa.selenium.Cookie::getName, org.openqa.selenium.Cookie::getValue));
        } catch (LoginFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при работе с ботом", e);
            throw new RuntimeException("Bot login failed: " + e.getMessage(), e);
        }
    }

    private BasicDriverBot createBot(BotLoginRequest r) {
        MyCredentials creds = new MyCredentials(r.login(), r.password());
        return switch (r.botType()) {
            case GUNZ -> new GunzBot(creds, r.url(), botProperties);
            case OAZA -> new OazaBot(creds, r.url(), botProperties);
        };
    }
}