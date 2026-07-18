package top.speedcubing.server.login;

import top.speedcubing.lib.utils.SQL.SQLRow;

public class LoginContext {
    private final SQLRow row;
    private final SQLRow onlinePlayerRow;
    private final String realRank;

    public LoginContext(SQLRow row, SQLRow onlinePlayerRow, String realRank) {
        this.row = row;
        this.onlinePlayerRow = onlinePlayerRow;
        this.realRank = realRank;
    }

    public SQLRow getRow() {
        return row;
    }

    public SQLRow getOnlinePlayerRow() {
        return onlinePlayerRow;
    }

    public String getRealRank() {
        return realRank;
    }
}
