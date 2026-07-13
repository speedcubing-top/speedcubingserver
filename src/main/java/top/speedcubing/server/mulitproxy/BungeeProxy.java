package top.speedcubing.server.mulitproxy;

import top.speedcubing.lib.utils.bytes.ByteArrayBuffer;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class BungeeProxy {
    public static void switchServer(int id, String server) {
        User user = User.getUser(id);
        if (user != null) {
            user.proxy.write(new ByteArrayBuffer().writeUTF("connectionreq").writeInt(id).writeUTF("").writeUTF(server).toByteArray());
            return;
        }
        speedcubingServer.writeToInternal(new ByteArrayBuffer().writeUTF("connectionreq").writeInt(id).writeUTF("").writeUTF(server).toByteArray());
    }

    public static void proxyCommand(String command) {
        speedcubingServer.publishToInternal("proxycmd", command);
    }
}
