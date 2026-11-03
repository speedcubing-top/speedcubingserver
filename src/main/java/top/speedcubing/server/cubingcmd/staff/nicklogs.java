package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.utils.Prefix;
import top.speedcubing.common.utils.CubingTimeFormat;
import top.speedcubing.lib.bukkit.inventory.InventoryBuilder;
import top.speedcubing.lib.bukkit.inventory.ItemBuilder;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLResult;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.server.system.command.CubingCommand;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class nicklogs extends CubingCommand implements Listener {
    private static final String PREFIX = Prefix.STAFF;

    public nicklogs() {
        super("nicklogs");
    }
    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(PREFIX + Msg.usage("/nicklogs <player>"));
            return;
        }
        String name = args[0];
        if (name.contains("'")) {
            sender.sendMessage(PREFIX + Msg.ERROR + "Invalid name.");
            return;
        }
        List<NickLogData> nickLogs = getNickLogs(name);
        if (nickLogs == null) {
            sender.sendMessage(PREFIX + Msg.ERROR + "This player doesn't have any nick logs.");
            return;
        }
        open(player, name, nickLogs, 0);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }
        if (!e.getView().getTitle().startsWith("Nick Logs of ")) {
            return;
        }
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        String name = title.substring(13, title.indexOf(" ("));
        List<NickLogData> nickLogs = getNickLogs(name);
        if (nickLogs == null) {
            player.sendMessage(PREFIX + Msg.ERROR + "This player doesn't have any nick logs.");
            return;
        }
        int page = Integer.parseInt(title.split("Page ")[1].split("/")[0]);
        int totalPage = Integer.parseInt(title.split("/")[1].split("\\)")[0]);
        if (e.getSlot() == 45) {
            open(player, name, nickLogs, (page == 1) ? totalPage - 1 : page - 2);
        } else if (e.getSlot() == 53) {
            open(player, name, nickLogs, (page == totalPage) ? 0 : page);
        } else if (e.getSlot() == 49) {
            player.closeInventory();
        }
    }

    private void open(Player player, String targetName, List<NickLogData> nickLogs, int page) {
        try (SQLConnection connection = Database.getCubing()) {
            SQLResult result = connection.select("name")
                    .from("playersdata")
                    .where("name='" + targetName + "'")
                    .executeResult();
            if (result.isEmpty()) {
                return;
            }

            SQLRow r = result.get(0);
            String name = r.getString(0);
            player.openInventory(generateGUI(name, nickLogs).get(page));
        }
    }

    private List<Inventory> generateGUI(String name, List<NickLogData> nickLogs) {
        List<Inventory> inventories = new ArrayList<>();
        int pageSize = 27;
        int totalPages = (int) Math.ceil(nickLogs.size() / (double) pageSize);
        for (int i = 0; i < totalPages; i++) {
            InventoryBuilder builder = new InventoryBuilder(54, "Nick Logs of " + name + " (Page " + (i + 1) + "/" + totalPages + ")");
            builder.setItem(new ItemBuilder(Material.SKULL_ITEM).name("§aPlayer: " + name)
                    .durability(3)
                    .owner(name).build(), 0);

            for (int j = 9; j < 18; j++) {
                builder.setItem(new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build(), j);
            }
            for (int j = 46; j < 49; j++) {
                builder.setItem(new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build(), j);
            }
            for (int j = 50; j < 53; j++) {
                builder.setItem(new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build(), j);
            }
            builder.setItem(new ItemBuilder(Material.BARRIER).name("§cClose").build(), 49);
            builder.setItem(new ItemBuilder(Material.ARROW).name("§ePrevious Page").build(), 45);
            builder.setItem(new ItemBuilder(Material.ARROW).name("§eNext Page").build(), 53);

            int start = i * pageSize;
            int end = Math.min(start + pageSize, nickLogs.size());
            List<NickLogData> pageData = nickLogs.subList(start, end);
            for (int j = 0; j < pageData.size(); j++) {
                NickLogData data = pageData.get(j);
                int slotIndex = 18 + j;
                builder.setItem(new ItemBuilder(Material.PAPER)
                        .name("§eNick Logs")
                        .lore("§eOriginal Name§7: §a" + data.originalName(),
                                "§eNick Name§7: §a" + data.nickname(),
                                "§eNick Time§7: §a" + data.nickTimeFormat())
                        .build(), slotIndex);
            }
            builder.setClickable(false);
            inventories.add(builder.getInventory());
        }
        return inventories;
    }

    private List<NickLogData> getNickLogs(String name) {
        List<NickLogData> nickLogs = new ArrayList<>();
        try (SQLConnection connection = Database.getSystem();
             ResultSet resultSet = connection.select("name,nickname,nicktime")
                     .from("nicknames")
                     .where("name='" + name + "'")
                     .executeQuery()) {
            if (!resultSet.isBeforeFirst()) {
                return null;
            }
            while (resultSet.next()) {
                String originalName = resultSet.getString("name");
                String nickname = resultSet.getString("nickname");
                long nickTime = resultSet.getLong("nicktime");
                nickLogs.add(new NickLogData(originalName, nickname, nickTime, CubingTimeFormat.toYMDHMS(nickTime)));
            }
            nickLogs.sort(Comparator.comparingLong(NickLogData::nickTime).reversed());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nickLogs;
    }
}

record NickLogData(String originalName, String nickname, long nickTime, String nickTimeFormat) {
}
