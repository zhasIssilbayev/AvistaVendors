package kz.project.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private final boolean visible ;
    private final boolean debug;

    private long gunzWait = 5;
    private long oazaWait = 8;

    public BotProperties(boolean visible, boolean debug) {
        this.visible = visible;
        this.debug = debug;
    }

    public void setGunzWait(long gunzWait) {
        this.gunzWait = gunzWait;
    }

    public void setOazaWait(long oazaWait) {
        this.oazaWait = oazaWait;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDebug() {
        return debug;
    }

    public long getOazaWait() {
        return oazaWait;
    }

    public long getGunzWait() {
        return gunzWait;
    }
}
