package top.speedcubing.server.login;

import top.speedcubing.lib.utils.SQL.SQLRow;

public class LoginContext {
    private final SQLRow row;
    private final String realRank;

    public LoginContext(SQLRow row, String realRank) {
        this.row = row;
        this.realRank = realRank;
    }

    public SQLRow getRow() {
        return row;
    }

    public String getRealRank() {
        return realRank;
    }
}
