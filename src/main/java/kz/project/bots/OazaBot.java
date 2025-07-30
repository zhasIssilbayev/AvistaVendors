package kz.project.bots;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.project.configuration.BotProperties;
import kz.project.dto.MyCredentials;
import kz.project.exception.LoginFailedException;
import org.openqa.selenium.*;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v138.network.Network;
import org.openqa.selenium.devtools.v138.network.model.RequestId;
import org.openqa.selenium.devtools.v138.network.model.Response;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.Optional;

public class OazaBot extends BasicDriverBot{
    private static final Logger log = LoggerFactory.getLogger(OazaBot.class);

    private Queue<String> tokenResponseBodies;

    public OazaBot(MyCredentials credentials, String urlPath, BotProperties botProperties) {
        super(credentials, urlPath, botProperties);
        this.standardWaitDuration = botProperties.getOazaWait();
        this.tokenResponseBodies = new ArrayDeque<>();
    }

    @Override
    public void setupDriver() {
        super.setupDriver();

        try {
            // CDP-сессия
            DevTools devTools = driver.getDevTools();
            devTools.createSession();

            // Включаем сеть
            devTools.send(Network.enable(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(true)
            ));

            // Слушаем ответы
            devTools.addListener(Network.responseReceived(), response -> {
                Response res = response.getResponse();
                String url = res.getUrl();

                // Проверяем, что это нужный URL
                if (url.endsWith("/token") && res.getStatus() == 200) {
                    RequestId requestId = response.getRequestId();

                    // Загружаем тело ответа
                    Network.GetResponseBodyResponse body = devTools.send(Network.getResponseBody(requestId));
                    String bodyData = body.getBody();
                    tokenResponseBodies.add(bodyData);
                }
            });

            log.info("Драйвер успешно до настроен OAZA.");
        } catch (Exception e) {
            log.error("Ошибка при настройке драйвера OAZA", e);
            throw new RuntimeException("Ошибка setupDriver: " + e.getMessage(), e);
        }
    }

    @Override
    public void login() throws LoginFailedException {
        driver.get(urlPath);

        boolean successfullyLoggedIn;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(3, standardWaitDuration / 2)));
            wait.until(d -> successfullyLoggedIn());

            successfullyLoggedIn = true;
        } catch (TimeoutException ignored) {
            successfullyLoggedIn = false;
        }

        if (successfullyLoggedIn) {
            log.info("По куки уже залогинены — вход не требуется.");
            loggedIn = true;
            return;
        }

        if (botProperties.isDebug()) {
            try {
                File screenshot = (driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), Paths.get("debug.png"), StandardCopyOption.REPLACE_EXISTING);
                log.info("Скриншот сохранён в debug.png");
            } catch (Exception e) {
                log.warn("Не удалось сохранить скриншот", e);
            }
        }

        fillCredentialsAndSubmit();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(2, standardWaitDuration / 3)));
            wait.until(d -> isLoginErrorVisible());

            throw new LoginFailedException("Неверный логин или пароль.");
        } catch (TimeoutException ignored) {
            // Ошибка входа не появилась — всё хорошо, продолжаем
        }

        wait.until(d -> successfullyLoggedIn());
        loggedIn = true;
        log.info("Успешная авторизация.");
    }

    private boolean isLoginErrorVisible() {
        WebElement loginDiv = driver.findElement(By.id("message-box"));
        String classes = loginDiv.getAttribute("class");
        return classes != null && classes.contains("message-box message-box-error");
    }

    private boolean successfullyLoggedIn() {
        return driver.getTitle().equalsIgnoreCase("eH One");
    }

    private void fillCredentialsAndSubmit() {
        WebElement loginInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        loginInput.clear();
        loginInput.sendKeys(credentials.login());

        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordInput.clear();
        passwordInput.sendKeys(credentials.password());

        WebElement checkbox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rememberMe")));
        if (!checkbox.isSelected()) {
            checkbox.click();  // Кликаем только если он не был уже выбран
        }

        WebElement logIN = wait.until(ExpectedConditions.elementToBeClickable(By.id("kc-login")));
        logIN.click();
    }

    public Map<String, String> getTokenResponseBodies() {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = tokenResponseBodies.peek();
        Map<String, String> map;
        try {
            map = objectMapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
