package kz.project.bots;

import kz.project.configuration.BotProperties;
import kz.project.dto.MyCredentials;
import kz.project.exception.LoginFailedException;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

public abstract class BasicDriverBot implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(BasicDriverBot.class);

    protected static final Path PROFILE_DIR = Paths.get(System.getenv("LOCALAPPDATA") +
            "\\Google\\chromium");

    protected final MyCredentials credentials;
    protected final String urlPath;
    protected BotProperties botProperties;

    protected long standardWaitDuration;
    protected ChromeDriver driver;
    protected WebDriverWait wait;
    protected boolean loggedIn;

    protected BasicDriverBot(MyCredentials credentials, String urlPath, BotProperties botProperties) {
        this.credentials = credentials;
        this.urlPath = urlPath;
        this.botProperties = botProperties;
    }

    public void setupDriver() {
        try {
            ChromeOptions options = new ChromeOptions();
            // Использование собственного браузера,
            Path currentDir = Paths.get("").toAbsolutePath();
            String chromeBinaryPath = currentDir.resolve("chrome-win").resolve("chrome.exe").toString();
            options.setBinary(chromeBinaryPath);

            String userDirectory = "User_" + credentials.login().replaceAll("[^a-zA-Z0-9]", "");
            options.addArguments("user-data-dir=" + PROFILE_DIR.resolve(userDirectory));
            if (!botProperties.isVisible()) {
                options.addArguments("--headless=new", "--disable-gpu", "--disable-extensions");
            }
            options.addArguments("--window-size=1920,1080");
            driver = new ChromeDriver(options);
			driver.manage().window().setSize(new Dimension(1920, 1080));
            wait = new WebDriverWait(driver, Duration.ofSeconds(standardWaitDuration));
            log.info("Драйвер успешно инициализирован.");
        } catch (Exception e) {
            log.error("Ошибка при настройке драйвера", e);
            throw new RuntimeException("Ошибка setupDriver: " + e.getMessage(), e);
        }
    }

    public abstract void login() throws LoginFailedException;

    @Override
    public void close() {
        try {
            if (driver != null) {
                driver.quit();
                log.info("Драйвер корректно закрыт.");
            }
        } catch (Exception e) {
            log.warn("Ошибка при закрытии драйвера", e);
        }
    }

    public Set<Cookie> getCookies() {
        try {
            return driver.manage().getCookies();
        } catch (Exception e) {
            log.error("Ошибка при получении cookies", e);
            throw new RuntimeException("Не удалось получить cookies", e);
        }
    }

    public abstract Map<String, String> getTokenResponseBodies();
}