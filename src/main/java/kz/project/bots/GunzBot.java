package kz.project.bots;

import kz.project.configuration.BotProperties;
import kz.project.dto.MyCredentials;
import kz.project.exception.LoginFailedException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.Duration;
import java.util.Map;

public class GunzBot extends BasicDriverBot {
    private static final Logger log = LoggerFactory.getLogger(GunzBot.class);

    public GunzBot(MyCredentials credentials, String urlPath, BotProperties botProperties) {
        super(credentials, urlPath, botProperties);
        this.standardWaitDuration = botProperties.getGunzWait();
    }

    @Override
    public void login() throws LoginFailedException {
        driver.get(urlPath);
        acceptCookiesIfPresent();

        if (successfullyLoggedIn()) {
            log.info("По куки уже залогинены — вход не требуется.");
            loggedIn = true;
            return;
        }

        openLoginPopup();
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

    @Override
    public Map<String, String> getTokenResponseBodies() {
        throw new UnsupportedOperationException("Unsupported method for this bot.");
    }

    private void acceptCookiesIfPresent() {
        try {
            By cookiePopup = By.id("consent_management_popup__content_wrapper");
            wait.until(ExpectedConditions.presenceOfElementLocated(cookiePopup));
            driver.findElement(By.cssSelector("a.ip_button__accept_cookies")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePopup));
            log.info("Куки приняты.");
        } catch (TimeoutException ignored) {
            log.info("Окно куки не появилось — пропущено.");
        }
    }

    private boolean isLoginErrorVisible() {
        WebElement loginDiv = driver.findElement(By.id("fF__error__password"));
        String classes = loginDiv.getAttribute("class");
        return classes != null && classes.contains("ipfit__field_error");
    }

    private boolean successfullyLoggedIn() {
        WebElement loginDiv = driver.findElement(By.id("login"));
        String classes = loginDiv.getAttribute("class");
        return classes != null && classes.contains("logged_in");
    }

    private void openLoginPopup() {
        if (botProperties.isDebug()) {
            try {
                File screenshot = (driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), Paths.get("debug.png"), StandardCopyOption.REPLACE_EXISTING);
                log.info("Скриншот сохранён в debug.png");
            } catch (Exception e) {
                log.warn("Не удалось сохранить скриншот", e);
            }
        }

        By loginButton = By.xpath("//div[@id='login']/a[contains(@href, '/formular/login')]");
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_popup")));
    }

    private void fillCredentialsAndSubmit() {
        WebElement loginInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login_popup__form__username")));
        loginInput.clear();
        loginInput.sendKeys(credentials.login());

        WebElement passwordInput = driver.findElement(By.id("login_popup__form__password"));
        passwordInput.clear();
        passwordInput.sendKeys(credentials.password());

        driver.findElement(By.xpath("//a[@data-ipbutton-action='login' and contains(@class, 'ip_button__sign_in')]")).click();
    }
}