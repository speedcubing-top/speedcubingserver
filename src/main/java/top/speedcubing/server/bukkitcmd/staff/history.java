package top.speedcubing.server.bukkitcmd.staff;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import top.speedcubing.common.database.Database;
import top.speedcubing.common.text.Msg;
import top.speedcubing.common.utils.CubingTimeFormat;
import top.speedcubing.lib.bukkit.inventory.ItemBuilder;
import top.speedcubing.lib.minecraft.text.ComponentText;
import top.speedcubing.lib.minecraft.text.TextClickEvent;
import top.speedcubing.lib.minecraft.text.TextHoverEvent;
import top.speedcubing.lib.utils.SQL.SQLConnection;
import top.speedcubing.lib.utils.SQL.SQLResult;
import top.speedcubing.lib.utils.SQL.SQLRow;
import top.speedcubing.lib.utils.SystemUtils;
import top.speedcubing.server.player.User;

public class history implements CommandExecutor, Listener {
    private static final String PREFIX = Msg.STAFF;
    List<Inventory> banList = new ArrayList<>();
    List<Inventory> muteList = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            openHistoryGui(player, User.getUser(player).realName);
            return true;
        }
        if (args.length == 1) {
            String targetName = args[0];
            if (!openHistoryGui(player, targetName)) {
                player.sendMessage(PREFIX + Msg.ERROR + "The player has never joined this server.");
                return true;
            }

        }
        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        String[] info;
        String title = e.getInventory().getTitle();
        if (title.equals("Punishment History") || title.contains("'s ban logs") || title.contains("'s mute logs")) {
            e.setCancelled(true);
            if (e.getInventory().getTitle().equals("Punishment History")) {
                info = e.getInventory().getItem(1).getItemMeta().getLore().toArray(new String[0]);
                switch (e.getRawSlot()) {
                    case 0:
                        if (e.isLeftClick()) {
                            banList = generateBanGUI(info);
                            if (banList.isEmpty()) {
                                player.sendMessage(PREFIX + Msg.ERROR + "This player doesn't have any ban punishment logs.");
                                player.closeInventory();
                                return;
                            }
                            player.openInventory(banList.get(0));
                        } else if (e.isRightClick()) {
                            muteList = generateMuteGUI(info);
                            if (muteList.isEmpty()) {
                                player.sendMessage(PREFIX + Msg.ERROR + "This player doesn't have any mute punishment logs.");
                                player.closeInventory();
                                return;
                            }
                            player.openInventory(muteList.get(0));
                        }
                        break;
                    case 8:
                        e.getWhoClicked().closeInventory();
                        break;
                }
            }
            if (title.contains("'s ban logs")) {
                Page(e, player);
            }
            if (title.contains("'s mute logs")) {
                Page(e, player);
            }
        }

    }

    private void Page(InventoryClickEvent e, Player player) {
        String title = e.getInventory().getTitle();
        String[] pageString = e.getInventory().getTitle().split("Page");
        String[] page = pageString[1].split("/");
        int currentPage = Integer.parseInt(page[0].trim());
        int currentPageIndex = currentPage - 1;
        int totalPage = Integer.parseInt(page[1].replace(")", ""));
        String[] skullStr = e.getInventory().getItem(0).getItemMeta().getDisplayName().split(":");
        String name = skullStr[1].trim();
        e.setCancelled(true);
        switch (e.getRawSlot()) {
            case 1:
                player.sendMessage(new ComponentText().both(PREFIX + Msg.VALUE + "Click here to query " + name + "'s client"
                                , TextClickEvent.runCommand("/clientinfo " + name)
                                , TextHoverEvent.showText("§eClick me!"))
                        .toBungee());
                player.closeInventory();
                break;
            case 45:
                if (currentPage == 1) {
                    openHistoryGui(player, name);
                    return;
                }
                if (title.contains("ban")) {
                    player.openInventory(banList.get(currentPageIndex - 1));
                } else {
                    player.openInventory(muteList.get(currentPageIndex - 1));
                }
                break;
            case 53:
                if (currentPage == totalPage) {
                    if (title.contains("ban")) {
                        player.openInventory(banList.get(0));
                    } else {
                        player.openInventory(muteList.get(0));
                    }
                    return;
                }
                if (title.contains("ban")) {
                    player.openInventory(banList.get(currentPageIndex + 1));
                } else {
                    player.openInventory(muteList.get(currentPageIndex + 1));
                }
                break;
            case 49:
                player.closeInventory();
                break;
        }
    }

    private boolean openHistoryGui(Player sender, String name) {
        try (SQLConnection connection = Database.getCubing()) {
            SQLResult result = connection.select("name,profile_textures_value,uuid")
                    .from("playersdata")
                    .where("name='" + name + "'")
                    .executeResult();
            if (result.isEmpty())
                return false;

            SQLRow r = result.get(0);

            Inventory inventory = Bukkit.createInventory(null, 9, "Punishment History");

            inventory.setItem(0, new ItemBuilder(Material.SKULL_ITEM).name("§a" + r.getString(0) + "'s punishment history")
                    .addLore("§eLeft Click to view ban history.", "§eRight Click to view mute history.", "§eUUID: " + r.getString(2)).durability(3)
                    .owner(name)
                    .build());
            inventory.setItem(1, new ItemBuilder(Material.PAPER).name("§aINFORMATION")
                    .addLore(r.getString(0), r.getString(1), r.getString(2))
                    .build());
            for (int i = 2; i < 8; i++) {
                inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build());
            }
            inventory.setItem(8, new ItemBuilder(Material.BARRIER).name("§cCLOSE").build());
            sender.openInventory(inventory);
            return true;
        }
    }


    private List<Inventory> generateBanGUI(String[] values) {
        List<BanPunishment> datas = getBanPunishmentHistory(values[2]);
        List<Inventory> result = new ArrayList<>();

        int pageSize = 27;
        int pageCount = (int) Math.ceil((double) datas.size() / pageSize);
        for (int page = 0; page < pageCount; page++) {
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, datas.size());
            Inventory inv = Bukkit.createInventory(null, 54, values[0] + "'s ban logs (Page " + (page + 1) + "/" + pageCount + ")");

            int itemIndex = 18;
            for (int i = startIndex; i < endIndex; i++) {
                GUISettings(values, inv);

                BanPunishment punishment = datas.get(i);
                long remain = getPunishRemain(punishment.at(), punishment.days(), punishment.pardon(), SystemUtils.getCurrentSecond());
                try (SQLConnection connection = Database.getCubing()) {
                    inv.setItem(itemIndex++, new ItemBuilder(Material.WOOL).name("§eBanned logs")
                            .addLore("§eName: §a" + values[0],
                                    "§eOperator: §a" + (punishment.operator().length() == 36 ?
                                            connection.select("name").from("playersdata").where("uuid='" + punishment.operator() + "'").executeResult().getString() :
                                            punishment.operator()),
                                    "§eReason: §a" + punishment.reason(),
                                    "§eBanID: §a" + punishment.id(),
                                    "§eDuration: §a" + punishment.days(),
                                    "§eAt: §a" + CubingTimeFormat.toYMDHMS(punishment.at()),
                                    "§eIp: §a" + punishment.ip(),
                                    "§eHideID: §a" + (punishment.hideid().equals("1") ? "true" : "false"),
                                    "§eState: §a" + (remain > 0 ? "Expire in " + CubingTimeFormat.period(remain) : remain < 0 ? "Not Unbanned yet" : punishment.pardon().isEmpty() ? "§aExpired" : "Unbanned by " + punishment.pardon()),
                                    "§ePardon at: §a" + (punishment.pardonat() == 0 ? "null" : CubingTimeFormat.toYMDHMS(punishment.pardonat())))
                            .durability(remain > 0 ? 14 : remain < 0 ? 14 : 5)
                            .build());
                }
                if (itemIndex > 45) {
                    break;
                }
            }
            result.add(inv);
        }

        return result;
    }

    private void GUISettings(String[] values, Inventory inv) {
        String title = inv.getTitle();
        String[] pageString = title.split("Page");
        String[] page = pageString[1].split("/");
        int currentPage = Integer.parseInt(page[0].trim());
        int currentPageIndex = currentPage - 1;
        int totalPage = Integer.parseInt(page[1].replace(")", ""));
        inv.setItem(0, new ItemBuilder(Material.SKULL_ITEM).name("§aPlayer: " + values[0])
                .durability(3)
                .owner(values[0]).build());
        inv.setItem(1, new ItemBuilder(Material.COMPASS).name("§a/Clientinfo").addLore("§eClick to query this player client.").build());
        for (int j = 9; j < 18; j++) {
            inv.setItem(j, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build());
        }
        for (int j = 46; j < 49; j++) {
            inv.setItem(j, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build());
        }
        for (int j = 50; j < 53; j++) {
            inv.setItem(j, new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name(" ").build());
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(currentPage == 1 ? "§aGo back" : "§aPrevious Page")
                .addLore(currentPage == 1 ? "§8To Punishment History." : "§8Go to page " + (currentPage - 1) + ".")
                .build());
        inv.setItem(49, new ItemBuilder(Material.BARRIER).name("§cCLOSE").build());
        inv.setItem(53, new ItemBuilder(Material.ARROW).name("§aNext Page")
                .addLore(currentPage == totalPage ? "§8Go To Page 1." : "§8Go To Page " + (currentPage + 1) + ".")
                .build());
    }

    private List<Inventory> generateMuteGUI(String[] values) {
        List<MutePunishment> datas = getMutePunishmentHistory(values[2]);
        List<Inventory> result = new ArrayList<>();

        int pageSize = 27;
        int pageCount = (int) Math.ceil((double) datas.size() / pageSize);
        for (int page = 0; page < pageCount; page++) {
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, datas.size());
            Inventory inv = Bukkit.createInventory(null, 54, values[0] + "'s mute logs (Page " + (page + 1) + "/" + pageCount + ")");

            int itemIndex = 18;
            for (int i = startIndex; i < endIndex; i++) {
                GUISettings(values, inv);

                MutePunishment punishment = datas.get(i);
                long remain = getPunishRemain(punishment.at(), punishment.days(), punishment.pardon(), SystemUtils.getCurrentSecond());
                try (SQLConnection connection = Database.getCubing()) {
                    inv.setItem(itemIndex++, new ItemBuilder(Material.WOOL).name("§eMuted logs")
                            .addLore("§eName: §a" + values[0],
                                    "§eOperator: §a" + (punishment.operator().length() == 36 ? connection.select("name").from("playersdata").where("uuid='" + punishment.operator() + "'").executeResult().getString() : punishment.operator()),
                                    "§eReason: §a" + punishment.reason(),
                                    "§eMuteID: §a" + punishment.id(),
                                    "§eDuration: §a" + punishment.days(),
                                    "§eAt: §a" + CubingTimeFormat.toYMDHMS(punishment.at()),
                                    "§eState: §a" + (remain > 0 ? "Expire in " + CubingTimeFormat.period(remain) : remain < 0 ? "Not Unmuted yet" : punishment.pardon().isEmpty() ? "§aExpired" : "Unmuted by " + punishment.pardon()),
                                    "§ePardon at: §a" + (punishment.pardonat() == 0 ? "null" : CubingTimeFormat.toYMDHMS(punishment.pardonat())))
                            .durability(remain > 0 ? 14 : remain < 0 ? 14 : 5)
                            .build());
                }
                if (itemIndex > 45) {
                    break;
                }
            }
            result.add(inv);
        }

        return result;
    }

    private List<BanPunishment> getBanPunishmentHistory(String uuid) {
        List<BanPunishment> resultList = new ArrayList<>();
        try (SQLConnection connection = Database.getCubing();
             ResultSet resultSet = connection.select("id,uuid,ip,hideid,at,reason,operator,days,pardon,pardonat")
                     .from("banlist")
                     .where("uuid='" + uuid + "'")
                     .executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String playerUuid = resultSet.getString("uuid");
                String ip = resultSet.getString("ip");
                String hideid = resultSet.getString("hideid");
                long at = resultSet.getLong("at");
                String reason = resultSet.getString("reason");
                String operator = resultSet.getString("operator");
                int days = resultSet.getInt("days");
                String pardon = resultSet.getString("pardon");
                long pardonat = resultSet.getLong("pardonat");

                BanPunishment punishment = new BanPunishment(id, playerUuid, ip, hideid, at, reason, operator, days, pardon, pardonat);
                resultList.add(punishment);
            }
            resultList.sort((o1, o2) -> Long.compare(o2.at(), o1.at()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }


    private List<MutePunishment> getMutePunishmentHistory(String uuid) {
        List<MutePunishment> resultList = new ArrayList<>();
        try (SQLConnection connection = Database.getCubing();
             ResultSet resultSet = connection.select("id,uuid,at,reason,operator,days,pardon,pardonat")
                     .from("mutelist")
                     .where("uuid='" + uuid + "'")
                     .executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String playerUuid = resultSet.getString("uuid");
                long at = resultSet.getLong("at");
                String reason = resultSet.getString("reason");
                String operator = resultSet.getString("operator");
                int days = resultSet.getInt("days");
                String pardon = resultSet.getString("pardon");
                long pardonat = resultSet.getLong("pardonat");

                MutePunishment punishment = new MutePunishment(id, playerUuid, at, reason, operator, days, pardon, pardonat);
                resultList.add(punishment);
            }
            resultList.sort((o1, o2) -> Long.compare(o2.at(), o1.at()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
    }

    public static long getPunishRemain(long at, String days, String pardon, long unix) {
        return getPunishRemain(at, Integer.parseInt(days), pardon, unix);
    }

    public static long getPunishRemain(long at, int days, String pardon, long unix) {
        long l = days * 86400L + at - unix;
        return pardon.isEmpty() && (days == 0 || l > 0) ? l : 0;
    }

}

record BanPunishment(String id, String uuid, String ip, String hideid, long at, String reason, String operator,
                     int days, String pardon, long pardonat) {
}

record MutePunishment(String id, String uuid, long at, String reason, String operator, int days, String pardon,
                      long pardonat) {
}
