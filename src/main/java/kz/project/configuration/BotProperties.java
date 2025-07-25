package kz.project.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private final boolean visible ;
    private final boolean debug;

    public BotProperties(boolean visible, boolean debug) {
        this.visible = visible;
        this.debug = debug;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDebug() {
        return debug;
    }
}
