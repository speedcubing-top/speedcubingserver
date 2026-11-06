package top.speedcubing.server.bukkitcmd.trolls;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.playmode.MonoStereoMode;
import com.xxmicloxx.NoteBlockAPI.model.playmode.StereoMode;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.speedcubing.common.text.Msg;
import top.speedcubing.server.player.User;
import top.speedcubing.server.utils.Prefix;

public class music implements CommandExecutor, Listener {
    private static final String PREFIX = Prefix.MUSIC;
    public static Song song;
    public static RadioSongPlayer radioSongPlayer;
    public static boolean broadcast = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
//        if (!Bukkit.getServerName().equalsIgnoreCase("lobby")) {
//            sender.sendMessage("§cThis command is disabled at here");
//            return true;
//        }
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) {
            player.sendMessage(PREFIX + Msg.usage("/music <play|resume|pause|loop|stop|add|remove|broadcast>"));
            return true;
        }
        if (args[0].equalsIgnoreCase("play")) {
            if (radioSongPlayer != null && radioSongPlayer.isPlaying()) {
                player.sendMessage(PREFIX + Msg.ERROR + "Song is already playing.");
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(PREFIX + Msg.usage("/music play <song>"));
                return true;
            }
            File file = new File("/storage/mcsongs/" + args[1] + ".nbs");
            if (!file.exists()) {
                player.sendMessage(PREFIX + Msg.ERROR + "Song not found.");
                return true;
            }
            song = NBSDecoder.parse(file);
            if (song == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "Song load failed.");
                return true;
            }
            StereoMode stereoMode = new StereoMode();
            stereoMode.setFallbackChannelMode(new MonoStereoMode());
            radioSongPlayer = new RadioSongPlayer(song);
            radioSongPlayer.addPlayer(player);
            radioSongPlayer.setPlaying(true);
            radioSongPlayer.setChannelMode(stereoMode);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Playing song.");
            player.sendMessage(PREFIX + Msg.SUCCESS + "Loop: " + Msg.VALUE + (radioSongPlayer.getRepeatMode() == RepeatMode.ALL ? "On" : "Off"));
            player.sendMessage(PREFIX + Msg.SUCCESS + "Broadcast: " + Msg.VALUE + (broadcast ? "On" : "Off"));
            return true;
        }
        if (args[0].equalsIgnoreCase("resume")) {
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to resume.");
                return true;
            }
            radioSongPlayer.setPlaying(true);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Resumed song.");
            return true;
        }
        if (args[0].equalsIgnoreCase("pause")) {
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to pause.");
                return true;
            }
            radioSongPlayer.setPlaying(false);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Paused song.");
            return true;
        }
        if (args[0].equalsIgnoreCase("loop")) {
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to loop.");
                return true;
            }
            radioSongPlayer.setRepeatMode(radioSongPlayer.getRepeatMode() == RepeatMode.ALL ? RepeatMode.NO : RepeatMode.ALL);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Set loop mode to " + Msg.VALUE + radioSongPlayer.getRepeatMode().name());
            return true;
        }
        if (args[0].equalsIgnoreCase("stop")) {
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to stop.");
                return true;
            }
            radioSongPlayer.setPlaying(false);
            radioSongPlayer.destroy();
            radioSongPlayer.getPlayerUUIDs().forEach(radioSongPlayer::removePlayer);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Stopped song.");
            return true;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 2) {
                player.sendMessage(PREFIX + Msg.usage("/music add <player>"));
                return true;
            }
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to add.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                return true;
            }
            radioSongPlayer.addPlayer(target);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Added " + User.getUser(target).getColorName() + Msg.SUCCESS + " to song player.");
            return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                player.sendMessage(PREFIX + Msg.usage("/music remove <player>"));
                return true;
            }
            if (radioSongPlayer == null) {
                player.sendMessage(PREFIX + Msg.ERROR + "No song to remove.");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(PREFIX + Msg.PLAYER_NOT_FOUND);
                return true;
            }
            radioSongPlayer.removePlayer(target);
            player.sendMessage(PREFIX + Msg.SUCCESS + "Removed " + User.getUser(target).getColorName() + Msg.SUCCESS + " from song player.");
            return true;
        }
        if (args[0].equalsIgnoreCase("broadcast")) {
            broadcast = !broadcast;
            player.sendMessage(PREFIX + Msg.SUCCESS + "Broadcast " + Msg.VALUE + (broadcast ? "On" : "Off") + Msg.SUCCESS + ".");
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (radioSongPlayer != null && broadcast) radioSongPlayer.addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (radioSongPlayer != null) radioSongPlayer.removePlayer(e.getPlayer());
    }

}