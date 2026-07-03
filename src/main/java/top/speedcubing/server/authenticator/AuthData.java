package top.speedcubing.server.authenticator;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.server.player.User;

public class AuthData {
    public static Map<User, AuthData> map = new ConcurrentHashMap<>();
    private final User user;
    private final boolean isAuthBypass;
    private String key;
    private boolean isAuthEnable;
    private boolean hasSessions;
    public String noKey = null;

    public String ip;

    public AuthData(User user) {
        SQLRow r = user.dbSelect("auth_bypass,auth_enable,auth_key,auth_sessions,auth_ip");
        this.user = user;
        this.isAuthBypass = r.getBoolean(0);
        this.isAuthEnable = r.getBoolean(1);
        this.key = r.getString(2).isEmpty() ? null : r.getString(2);
        this.hasSessions = r.getBoolean(3);
        this.ip = r.getString(4);
    }

    public boolean isAuthBypass() {
        return isAuthBypass;
    }

    public void setIp(String ip) {
        user.dbUpdate("auth_ip='" + ip + "'");
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public boolean hasKey() {
        return key != null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        Preconditions.checkArgument(key != null);

        this.key = key;

        user.dbUpdate("auth_key='" + key + "'");

        setSession(true);
    }

    public boolean isAuthEnable() {
        return isAuthEnable;
    }

    public void setAuthEnable() {
        isAuthEnable = true;
        user.dbUpdate("auth_enable=1");
    }

    public boolean hasSessions() {
        return hasSessions;
    }

    public void setSession(boolean hasSessions) {
        user.dbUpdate("auth_sessions=" + (hasSessions ? 1 : 0));
        this.hasSessions = hasSessions;
    }

    public boolean allowAction() {
        return hasSessions || !isAuthEnable || isAuthBypass;
    }

    @Override
    public String toString() {
        return "{isAuthBypass=" + isAuthBypass + ",key=" + key + ",isAuthEnabled=" + isAuthEnable + ",hasSessions=" + hasSessions + "}";
    }
}
