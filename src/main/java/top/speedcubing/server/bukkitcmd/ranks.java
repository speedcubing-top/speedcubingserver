package top.speedcubing.server.bukkitcmd;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.speedcubing.common.rank.Rank;
import top.speedcubing.lib.bukkit.inventory.InventoryBuilder;
import top.speedcubing.lib.bukkit.inventory.ItemBuilder;
import top.speedcubing.server.lang.Lang;
import top.speedcubing.server.player.User;

public class ranks implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        open(player);
        return true;
    }

    private void open(Player player) {
        int lang = User.getUser(player).lang;
        InventoryBuilder inv = new InventoryBuilder(54, l(lang, "title"));
        ItemStack grayGlass = new ItemBuilder(Material.STAINED_GLASS_PANE).name(" ").durability(7).build();
        int rankWeight = Rank.rankByName.get(User.getUser(player).realRank).getWeight();
        ItemStack champ = new ItemBuilder(Material.WATCH).name("§3[Champ]").
                lore(
                        l(lang, "chat_prefix", "§3[Champ]"),
                        l(lang, "kbffa_champ_kill_effects"),
                        l(lang, "kbffa_champ_projectile_effects"),
                        l(lang, "fly_lobby"),
                        l(lang, "preview", getFormatName(player, "champ")),
                        l(lang, "coming_soon"),
                        "",
                        l(lang, "require_champ")
                )
                .build();
        ItemBuilder vip = new ItemBuilder(Material.IRON_BLOCK).name("§d[VIP]");
        if (rankWeight < Rank.rankByName.get("vip").getWeight()) {
            vip.lore(
                    l(lang, "chat_prefix", "§d[VIP]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "preview", getFormatName(player, "vip")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "5$"),
                    l(lang, "buy")
            );
        } else {
            vip.lore(
                    l(lang, "chat_prefix", "§d[VIP]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "preview", getFormatName(player, "vip")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "5$"),
                    l(lang, "purchased"),
                    l(lang, "buy")
            );
        }

        ItemBuilder vipPlus = new ItemBuilder(Material.IRON_BLOCK).name("§d[VIP+]").glow();
        if (rankWeight < Rank.rankByName.get("vipplus").getWeight() && rankWeight != Rank.rankByName.get("vip").getWeight()) {
            vipPlus.lore(
                    l(lang, "chat_prefix", "§d[VIP+]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "preview", getFormatName(player, "vipplus")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "10$"),
                    l(lang, "buy")
            );
        } else if (rankWeight == Rank.rankByName.get("vip").getWeight()) {
            vipPlus.lore(
                    l(lang, "chat_prefix", "§d[VIP+]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "preview", getFormatName(player, "vipplus")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price_discount", "10$", "5$"),
                    l(lang, "upgrade_price", "$5", "$10"),
                    l(lang, "buy")
            );
        } else if (rankWeight >= Rank.rankByName.get("vipplus").getWeight()) {
            vipPlus.lore(
                    l(lang, "chat_prefix", "§d[VIP+]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "preview", getFormatName(player, "vipplus")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "10$"),
                    l(lang, "purchased"),
                    l(lang, "buy")
            );
        }
        ItemBuilder premium = new ItemBuilder(Material.GOLD_BLOCK).name("§6[Premium]");
        if (rankWeight < Rank.rankByName.get("premium").getWeight() && rankWeight != Rank.rankByName.get("vipplus").getWeight() && rankWeight != Rank.rankByName.get("vip").getWeight()) {
            premium.lore(
                    l(lang, "chat_prefix", "§6[Premium]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "kbffa_weapon_selector"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "skin"),
                    l(lang, "preview", getFormatName(player, "premium")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "15$"),
                    l(lang, "buy")
            );
        } else if (rankWeight == Rank.rankByName.get("vip").getWeight()) {
            premium.lore(
                    l(lang, "chat_prefix", "§6[Premium]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "kbffa_weapon_selector"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "skin"),
                    l(lang, "preview", getFormatName(player, "premium")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price_discount", "15$", "10$"),
                    l(lang, "upgrade_price", "$10", "$15"),
                    l(lang, "buy")
            );
        } else if (rankWeight == Rank.rankByName.get("vipplus").getWeight()) {
            premium.lore(
                    l(lang, "chat_prefix", "§6[Premium]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "kbffa_weapon_selector"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "skin"),
                    l(lang, "preview", getFormatName(player, "premium")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price_discount", "15$", "5$"),
                    l(lang, "upgrade_price", "$5", "$15"),
                    l(lang, "buy")
            );
        } else if (rankWeight >= Rank.rankByName.get("premium").getWeight()) {
            premium.lore(
                    l(lang, "chat_prefix", "§6[Premium]"),
                    l(lang, "kbffa_all_kill_effects"),
                    l(lang, "kbffa_all_projectile_effects"),
                    l(lang, "kbffa_armor_color"),
                    l(lang, "kbffa_weapon_selector"),
                    l(lang, "fly_lobby"),
                    l(lang, "reset_mlgrush"),
                    l(lang, "skin"),
                    l(lang, "preview", getFormatName(player, "premium")),
                    l(lang, "coming_soon"),
                    "",
                    l(lang, "price", "15$"),
                    l(lang, "purchased"),
                    l(lang, "buy")
            );
        }
        ItemStack premiumPlus = new ItemBuilder(Material.GOLD_BLOCK).name("§6[Premium+]").glow().
                lore(
                        l(lang, "chat_prefix", "§6[Premium+]"),
                        l(lang, "kbffa_all_kill_effects"),
                        l(lang, "kbffa_all_projectile_effects"),
                        l(lang, "kbffa_armor_color"),
                        l(lang, "kbffa_weapon_selector"),
                        l(lang, "fly_lobby"),
                        l(lang, "reset_mlgrush"),
                        l(lang, "skin"),
                        l(lang, "nick"),
                        l(lang, "preview", getFormatName(player, "premiumplus")),
                        l(lang, "coming_soon"),
                        "",
                        l(lang, "price_monthly", "3$"),
                        l(lang, "note_premium_required"),
                        l(lang, "buy")
                )
                .build();
        ItemStack yt = new ItemBuilder(Material.WOOL).durability(10).name("§5[YT]").glow().
                lore(
                        l(lang, "chat_prefix", "§5[YT]"),
                        l(lang, "kbffa_all_kill_effects"),
                        l(lang, "kbffa_all_projectile_effects"),
                        l(lang, "kbffa_armor_color"),
                        l(lang, "kbffa_weapon_selector"),
                        l(lang, "fly_lobby"),
                        l(lang, "reset_mlgrush"),
                        l(lang, "skin"),
                        l(lang, "nick"),
                        l(lang, "preview", getFormatName(player, "yt")),
                        l(lang, "coming_soon"),
                        "",
                        l(lang, "require_subscribers", "1000"),
                        l(lang, "note_social_media"),
                        l(lang, "application")
                ).build();
        ItemStack ytPlus = new ItemBuilder(Material.WOOL).durability(14).name("§4[YT+]").glow().
                lore(
                        l(lang, "chat_prefix", "§4[YT+]"),
                        l(lang, "kbffa_all_kill_effects"),
                        l(lang, "kbffa_all_projectile_effects"),
                        l(lang, "kbffa_armor_color"),
                        l(lang, "kbffa_weapon_selector"),
                        l(lang, "fly_lobby"),
                        l(lang, "reset_mlgrush"),
                        l(lang, "skin"),
                        l(lang, "nick_custom"),
                        l(lang, "preview", getFormatName(player, "ytplus")),
                        l(lang, "coming_soon"),
                        "",
                        l(lang, "require_subscribers", "5000"),
                        l(lang, "note_social_media"),
                        l(lang, "application")
                ).build();
        ItemStack builder = new ItemBuilder(Material.EMERALD_BLOCK).name("§3[Builder]").
                lore(
                        l(lang, "chat_prefix", "§3[Builder]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "builder")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        ItemStack helper = new ItemBuilder(Material.getMaterial(351)).durability(4).name("§9[Helper]").
                lore(
                        l(lang, "chat_prefix", "§9[Helper]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "helper")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        ItemStack mod = new ItemBuilder(Material.REDSTONE).name("§c[Mod]").
                lore(
                        l(lang, "chat_prefix", "§c[Mod]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "mod")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        ItemStack dev = new ItemBuilder(Material.LAPIS_BLOCK).name("§b[Dev]").
                lore(
                        l(lang, "chat_prefix", "§b[Dev]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "developer")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        ItemStack admin = new ItemBuilder(Material.REDSTONE_BLOCK).name("§4[Admin]").
                lore(
                        l(lang, "chat_prefix", "§4[Admin]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "admin")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        ItemStack owner = new ItemBuilder(Material.BARRIER).name("§4[Owner]").
                lore(
                        l(lang, "chat_prefix", "§4[Owner]"),
                        l(lang, "all_perks"),
                        l(lang, "preview", getFormatName(player, "owner")),
                        "",
                        l(lang, "apply_unavailable")
                ).build();
        //more ranks
        for (int i = 0; i < 9; i++) {
            inv.setItem(grayGlass, i);
            inv.setItem(grayGlass, i + 45);
        }
        for (int i = 0; i < 6; i++) {
            inv.setItem(grayGlass, i * 9);
            inv.setItem(grayGlass, i * 9 + 8);
        }
        inv.setItem(champ, 10);
        inv.setItem(vip.build(), 11);
        inv.setItem(vipPlus.build(), 12);
        inv.setItem(premium.build(), 13);
        inv.setItem(premiumPlus, 14);
        inv.setItem(yt, 15);
        inv.setItem(ytPlus, 16);
        inv.setItem(builder, 19);
        inv.setItem(helper, 20);
        inv.setItem(mod, 21);
        inv.setItem(dev, 22);
        inv.setItem(admin, 23);
        inv.setItem(owner, 24);

        inv.setClickable(false);
        player.openInventory(inv.getInventory());
    }

    private static String l(int lang, String key, String... param) {
        return Lang.of("%lang_server_ranks_" + key + "%", param).getString(lang);
    }

    private String getFormatName(Player player, String rank) {
        User user = User.getUser(player);
        String prefix = Rank.getFormat(rank, 0).getPrefix();
        return prefix + user.realName;
    }
}
