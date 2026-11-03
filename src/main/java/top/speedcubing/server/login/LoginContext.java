package top.speedcubing.server.login;

import top.speedcubing.common.rank.Rank;
import top.speedcubing.lib.utils.SQL.SQLRow;

public class LoginContext {
    private final SQLRow row;
    private final SQLRow onlinePlayerRow;
    private final String realRank;
    private final long rankExpiresAt;

    public LoginContext(SQLRow row, SQLRow onlinePlayerRow, Rank.Resolved rank) {
        this.row = row;
        this.onlinePlayerRow = onlinePlayerRow;
        this.realRank = rank.rank();
        this.rankExpiresAt = rank.expiresAt();
    }

    public long getRankExpiresAt() {
        return rankExpiresAt;
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
