package top.speedcubing.server.mulitproxy;

import top.speedcubing.lib.utils.bytes.ByteArrayBuffer;
import top.speedcubing.server.player.User;
import top.speedcubing.server.speedcubingServer;

public class BungeeProxy {
    public static void switchServer(int id, String server) {
        User user = User.getUser(id);
        if (user != null) {
            if (user.proxy != null) {
                user.proxy.write(new ByteArrayBuffer().writeUTF("connectionreq").writeInt(id).writeUTF("").writeUTF(server).toByteArray());
            } else {
                speedcubingServer.getInstance().getLogger().warning(
                        "Cannot switch player " + id + " to " + server + ": proxy is not set");
            }
            return;
        }
        speedcubingServer.writeToInternal(new ByteArrayBuffer().writeUTF("connectionreq").writeInt(id).writeUTF("").writeUTF(server).toByteArray());
    }

    public static void proxyCommand(String command) {
        byte[] packet = new ByteArrayBuffer()
                .writeUTF("proxycmd")
                .writeUTF(command)
                .toByteArray();

        speedcubingServer.writeToInternal(packet);
    }
}
