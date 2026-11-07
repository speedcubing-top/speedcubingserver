package top.speedcubing.server.cubingcmd.staff;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.common.staff.StaffAlert;
import top.speedcubing.common.text.Msg;
import top.speedcubing.lib.bukkit.inventory.InventoryBuilder;
import top.speedcubing.lib.bukkit.inventory.ItemBuilder;
import top.speedcubing.server.player.User;
import top.speedcubing.server.system.command.CubingCommand;

/**
 * /staffalerts: one menu to turn each staff notice ({@link StaffAlert}) on or off for yourself. The choice is stored in
 * playersdata.staffalerts_off and applied by the proxy, which sends every staff notice through StaffAlerts.
 */
public class staffalerts extends CubingCommand {
    private static final String PREFIX = Msg.STAFF;
    // inner slots of a 4-row menu, filled in StaffAlert order
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

    public staffalerts() {
        super("staffalerts", "tsm");
    }

    @Override
    public void execute(CommandSender sender, String command, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + Msg.PLAYERS_ONLY);
            return;
        }
        User user = User.getUser(player);
        if (!user.isStaff) {
            sender.sendMessage(PREFIX + Msg.ERROR + "Only staff receive staff alerts.");
            return;
        }
        open(player, user, user.dbSelect(StaffAlert.COLUMN).getInt(StaffAlert.COLUMN));
    }

    private static void open(Player player, User user, int disabled) {
        InventoryBuilder inv = new InventoryBuilder(36, "Staff Alerts");
        ItemStack glass = new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(7).build();
        for (int i = 0; i < 36; i++)
            inv.setItem(glass, i);
        StaffAlert[] alerts = StaffAlert.values();
        for (int i = 0; i < alerts.length; i++)
            toggle(inv, player, user, disabled, alerts[i], SLOTS[i]);
        inv.setClickable(false);
        player.openInventory(inv.getInventory());
    }

    private static void toggle(InventoryBuilder inv, Player player, User user, int disabled, StaffAlert alert, int slot) {
        boolean on = alert.isEnabled(disabled);
        ItemStack item = new ItemBuilder(icon(alert))
                .name((on ? "§a" : "§c") + alert.displayName)
                .lore("§7Show a notice when", "§7" + alert.when + ".", " ", on ? "§aEnabled §7- click to disable" : "§cDisabled §7- click to enable")
                .build();
        inv.setItem(item, e -> {
            int updated = disabled ^ alert.bit;
            user.dbUpdate(StaffAlert.COLUMN + "=" + updated);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, on ? 0.5f : 2f);
            open(player, user, updated);
        }, slot);
    }

    private static Material icon(StaffAlert alert) {
        return switch (alert) {
            case CONNECT -> Material.EMERALD;
            case SWITCH -> Material.COMPASS;
            case DISCONNECT -> Material.REDSTONE;
            case STAFF_CHAT -> Material.BOOK_AND_QUILL;
            case REPORT -> Material.PAPER;
            case PUNISHMENT -> Material.IRON_AXE;
            case CHAT_FILTER -> Material.HOPPER;
            case NEW_PLAYER -> Material.SKULL_ITEM;
            case CLIENT_SPOOF -> Material.BARRIER;
        };
    }
}
