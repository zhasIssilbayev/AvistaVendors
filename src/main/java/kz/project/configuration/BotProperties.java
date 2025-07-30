package kz.project.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private boolean visible ;
    private boolean debug;

    private String chromePath = "";
    private long gunzWait = 5;
    private long oazaWait = 8;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getChromePath() {
        return chromePath;
    }

    public void setChromePath(String chromePath) {
        this.chromePath = chromePath;
    }

    public long getGunzWait() {
        return gunzWait;
    }

    public void setGunzWait(long gunzWait) {
        this.gunzWait = gunzWait;
    }

    public long getOazaWait() {
        return oazaWait;
    }

    public void setOazaWait(long oazaWait) {
        this.oazaWait = oazaWait;
    }
}
