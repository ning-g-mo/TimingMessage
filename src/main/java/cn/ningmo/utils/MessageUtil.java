package cn.ningmo.utils;

import cn.ningmo.TimingMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    private static final TimingMessage plugin = TimingMessage.getInstance();
    // 匹配RGB颜色代码的正则表达式 格式: &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * 处理消息中的颜色代码、HEX颜色代码、占位符和换行符
     */
    public static String format(String message, Player player) {
        if (message == null) return "";
        
        // 处理换行符
        message = message.replace("\\n", "\n");
        
        // 处理HEX颜色代码
        message = formatHexColors(message);
        
        // 处理传统颜色代码
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // 处理PlaceholderAPI占位符
        if (player != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        // 处理内置占位符
        message = replaceInternalPlaceholders(message, player);
        
        return message;
    }

    /**
     * 只处理颜色代码
     */
    public static String colorize(String message) {
        if (message == null) return "";
        message = formatHexColors(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * 处理HEX颜色代码
     */
    private static String formatHexColors(String message) {
        if (message == null) return "";
        
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + color).toString());
        }
        
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * 发送快捷栏消息
     */
    public static void sendActionBar(Player player, String message) {
        message = format(message, player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    /**
     * 处理内置占位符
     */
    private static String replaceInternalPlaceholders(String message, Player player) {
        if (player != null) {
            message = message.replace("%player%", player.getName())
                           .replace("%world%", player.getWorld().getName())
                           .replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                           .replace("%max_players%", String.valueOf(plugin.getServer().getMaxPlayers()));
        }
        
        message = message.replace("%server_name%", plugin.getServer().getName())
                       .replace("%server_version%", plugin.getServer().getVersion())
                       .replace("%plugin_version%", plugin.getDescription().getVersion())
                       .replace("%time%", java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")))
                       .replace("%date%", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        return message;
    }
} 