package kz.project.bots;

import kz.project.configuration.BotProperties;
import kz.project.dto.MyCredentials;
import kz.project.exception.LoginFailedException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class OazaBot extends BasicDriverBot{
    private static final Logger log = LoggerFactory.getLogger(OazaBot.class);

    private Queue<String> tokenResponseBody;

    public OazaBot(MyCredentials credentials, String urlPath, BotProperties botProperties) {
        super(credentials, urlPath, botProperties);
        this.standardWaitDuration = botProperties.getOazaWait();
        this.tokenResponseBody = new ArrayDeque<>();
    }

    @Override
    public void setupDriver() {
        super.setupDriver();

        try {

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

    public String getTokenResponseBody() {
        return tokenResponseBody.poll();
    }
}
