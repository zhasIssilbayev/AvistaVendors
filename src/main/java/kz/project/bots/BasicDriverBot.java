package kz.project.bots;

import io.github.bonigarcia.wdm.WebDriverManager;
import kz.project.dto.MyCredentials;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;

public abstract class BasicDriverBot implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(BasicDriverBot.class);

    protected static long STANDARD_WAIT_DURATION = 8;
    protected static final Path PROFILE_DIR = Paths.get(System.getenv("LOCALAPPDATA") +
            "\\Google\\Chrome\\User Data\\SeleniumProfile");

    protected final MyCredentials credentials;
    protected final String urlPath;
    protected final boolean visible;

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected boolean loggedIn;

    protected BasicDriverBot(MyCredentials credentials, String urlPath, boolean visible) {
        this.credentials = credentials;
        this.urlPath = urlPath;
        this.visible = visible;
    }

    public void setupDriver() {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            String userDirectory = "User_" + credentials.login().replaceAll("[^a-zA-Z0-9]", "");
            options.addArguments("user-data-dir=" + PROFILE_DIR.resolve(userDirectory));
            if (!visible) {
                options.addArguments("--headless=new", "--disable-gpu", "--disable-extensions");
            }
            options.addArguments("--window-size=1920,1080");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(STANDARD_WAIT_DURATION));
            log.info("Драйвер успешно инициализирован.");
        } catch (Exception e) {
            log.error("Ошибка при настройке драйвера", e);
            throw new RuntimeException("Ошибка setupDriver: " + e.getMessage(), e);
        }
    }

    public abstract void login();

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
}