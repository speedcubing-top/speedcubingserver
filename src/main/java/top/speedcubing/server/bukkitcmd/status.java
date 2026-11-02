package top.speedcubing.server.bukkitcmd;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.lib.bukkit.inventory.InventoryBuilder;
import top.speedcubing.lib.bukkit.inventory.ItemBuilder;
import top.speedcubing.server.player.User;

public class status implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        open(player);
        return true;
    }

    private void open(Player player) {
        User user = User.getUser(player);
        InventoryBuilder inv = new InventoryBuilder(54, "§bSpeedcubing Server §aStatus");
        ItemStack grayGlass = new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(7).build();
        ItemStack cps = generateItem(player, Material.WATCH, "CPS", hasPermission(player, "cps"), " ", "§7Preview:", "§7[CPS §a0 §7| §a0§7]");
        inv.setItem(cps, ClickInventoryEvent -> {
            if (user.status.equalsIgnoreCase("cps")) {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);
                user.status = "null";
                open(player);
                return;
            }
            if (hasPermission(player, "cps")) {
                user.status = "cps";
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 2f);
                open(player);
            } else {
                player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 0.5f);
                player.sendMessage("§cYou don't have permission to use this status!");
            }
        }, 10);
        for (int i = 0; i < 9; i++) {
            inv.setItem(grayGlass, i);
            inv.setItem(grayGlass, i + 45);
        }
        for (int i = 0; i < 6; i++) {
            inv.setItem(grayGlass, i * 9);
            inv.setItem(grayGlass, i * 9 + 8);
        }
        inv.setClickable(false);
        player.openInventory(inv.getInventory());
    }

    private ItemStack generateItem(Player player, Material material, String name, boolean unlock, String... lore) {
        boolean selected = false;
        String selectedItem = User.getUser(player).status;
        if (selectedItem == null) {
            selectedItem = "null";
        }
        if (selectedItem.equalsIgnoreCase(name)) {
            selected = true;
        }
        ItemBuilder builder = new ItemBuilder(material).name(name);
        builder.lore(lore);
        if (unlock) {
            builder.name("§a" + name);
        } else {
            builder.name("§c" + name);
        }
        if (unlock && !selected) {
            builder.addLore(" ");
            builder.addLore("§eClick to select!");
        } else if (!selected) {
            builder.addLore(" ");
            builder.addLore("§7Unlocked in speedcubing.top/discord. ask for help.");
            builder.addLore(" ");
            builder.addLore("§cLocked!");
        } else {
            builder.addLore(" ");
            builder.addLore("§aSelected!");
        }

        return builder.build();

    }

    private boolean hasPermission(Player player, String status) {
        return User.getUser(player).hasPermission("status." + status) || User.getUser(player).hasPermission("status.*");
    }
}
