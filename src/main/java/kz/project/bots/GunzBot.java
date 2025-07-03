package kz.project.bots;

import kz.project.dto.MyCredentials;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class GunzBot extends BasicDriverBot {
    private static final Logger log = LoggerFactory.getLogger(GunzBot.class);

    public GunzBot(MyCredentials credentials, String urlPath, boolean visible) {
        super(credentials, urlPath, visible);
    }

    @Override
    public void login() {
        driver.get(urlPath);
        acceptCookiesIfPresent();

        if (successfullyLoggedIn()) {
            log.info("По куки уже залогинены — вход не требуется.");
            loggedIn = true;
            return;
        }

        openLoginPopup();
        fillCredentialsAndSubmit();
        wait.until(d -> successfullyLoggedIn());
        loggedIn = true;
        log.info("Успешная авторизация.");
    }

    private void acceptCookiesIfPresent() {
        try {
            By cookiePopup = By.id("consent_management_popup__content_wrapper");
            wait.withTimeout(Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(cookiePopup));
            driver.findElement(By.cssSelector("a.ip_button__accept_cookies")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePopup));
            log.info("Куки приняты.");
        } catch (TimeoutException ignored) {
            log.info("Окно куки не появилось — пропущено.");
        } finally {
            wait.withTimeout(Duration.ofSeconds(STANDARD_WAIT_DURATION));
        }
    }

    private boolean successfullyLoggedIn() {
        WebElement loginDiv = driver.findElement(By.id("login"));
        String classes = loginDiv.getAttribute("class");
        return classes != null && classes.contains("logged_in");
    }

    private void openLoginPopup() {
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