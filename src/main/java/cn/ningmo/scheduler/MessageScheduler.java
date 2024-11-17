package cn.ningmo.scheduler;

import cn.ningmo.TimingMessage;
import cn.ningmo.enums.MessageType;
import cn.ningmo.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.util.*;

public class MessageScheduler {
    private static final Map<String, BukkitTask> tasks = new HashMap<>();
    private static final Map<String, BossBar> bossBars = new HashMap<>();
    private static final TimingMessage plugin = TimingMessage.getInstance();
    private static final Random random = new Random();

    public static void startScheduler() {
        if (!plugin.getConfig().getBoolean("enabled")) return;

        // 启动定时消息组
        startMessageGroups();
        // 启动定点消息组
        startScheduledGroups();
        // 启动世界特定消息组
        startWorldGroups();
    }

    private static void startMessageGroups() {
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("message-groups");
        if (groups == null) return;

        for (String groupName : groups.getKeys(false)) {
            ConfigurationSection group = groups.getConfigurationSection(groupName);
            if (group == null || !group.getBoolean("enabled")) continue;

            String type = group.getString("type", "broadcast").toUpperCase();
            int interval = group.getInt("interval") * 1200; // 转换为tick
            List<String> messages = group.getStringList("messages");

            if (messages.isEmpty()) continue;

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                String message = messages.get(random.nextInt(messages.size()));
                broadcastMessage(MessageType.valueOf(type), message, groupName);
            }, interval, interval);

            tasks.put("group_" + groupName, task);
        }
    }

    private static void startScheduledGroups() {
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("scheduled-groups");
        if (groups == null) return;

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalTime now = LocalTime.now();
            String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());

            for (String groupName : groups.getKeys(false)) {
                ConfigurationSection group = groups.getConfigurationSection(groupName);
                if (group == null || !group.getBoolean("enabled")) continue;

                String scheduledTime = group.getString("time");
                if (currentTime.equals(scheduledTime)) {
                    String type = group.getString("type", "broadcast").toUpperCase();
                    List<String> messages = group.getStringList("messages");
                    if (!messages.isEmpty()) {
                        String message = messages.get(random.nextInt(messages.size()));
                        broadcastMessage(MessageType.valueOf(type), message, groupName);
                    }
                }
            }
        }, 0L, 1200L); // 每分钟检查一次

        tasks.put("scheduled_checker", task);
    }

    private static void startWorldGroups() {
        ConfigurationSection groups = plugin.getConfig().getConfigurationSection("world-groups");
        if (groups == null) return;

        for (String groupName : groups.getKeys(false)) {
            ConfigurationSection group = groups.getConfigurationSection(groupName);
            if (group == null || !group.getBoolean("enabled")) continue;

            String type = group.getString("type", "broadcast").toUpperCase();
            int interval = group.getInt("interval") * 1200;
            String worldName = group.getString("world");
            List<String> messages = group.getStringList("messages");

            if (messages.isEmpty() || worldName == null) continue;

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                World world = Bukkit.getWorld(worldName);
                if (world != null && !world.getPlayers().isEmpty()) {
                    String message = messages.get(random.nextInt(messages.size()));
                    broadcastWorldMessage(MessageType.valueOf(type), message, world, groupName);
                }
            }, interval, interval);

            tasks.put("world_" + groupName, task);
        }
    }

    private static void broadcastMessage(MessageType type, String message, String groupId) {
        message = MessageUtil.colorize(message);
        
        switch (type) {
            case BROADCAST:
                Bukkit.broadcastMessage(message);
                break;
            case TITLE:
                String[] titleParts = message.split("\n");
                String title = titleParts[0].replace("title: ", "");
                String subtitle = titleParts.length > 1 ? titleParts[1].replace("subtitle: ", "") : "";
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle(
                        MessageUtil.format(title, player),
                        MessageUtil.format(subtitle, player),
                        10, 70, 20
                    );
                }
                break;
            case ACTIONBAR:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    MessageUtil.sendActionBar(player, MessageUtil.format(message, player));
                }
                break;
            case BOSSBAR:
                updateBossBar(message, groupId);
                break;
        }
    }

    private static void broadcastWorldMessage(MessageType type, String message, World world, String groupId) {
        message = MessageUtil.colorize(message);
        
        for (Player player : world.getPlayers()) {
            switch (type) {
                case BROADCAST:
                    player.sendMessage(MessageUtil.format(message, player));
                    break;
                case TITLE:
                    String[] titleParts = message.split("\n");
                    String title = titleParts[0].replace("title: ", "");
                    String subtitle = titleParts.length > 1 ? titleParts[1].replace("subtitle: ", "") : "";
                    player.sendTitle(
                        MessageUtil.format(title, player),
                        MessageUtil.format(subtitle, player),
                        10, 70, 20
                    );
                    break;
                case ACTIONBAR:
                    MessageUtil.sendActionBar(player, MessageUtil.format(message, player));
                    break;
                case BOSSBAR:
                    updateWorldBossBar(message, world, groupId);
                    break;
            }
        }
    }

    private static void updateBossBar(String message, String groupId) {
        String text = message.replace("text: ", "");
        BarColor color = BarColor.valueOf(
            message.contains("color:") ? message.split("color: ")[1].trim().toUpperCase() : "PURPLE"
        );

        BossBar bossBar = bossBars.get(groupId);
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(text, color, BarStyle.SOLID);
            bossBars.put(groupId, bossBar);
        }

        bossBar.setTitle(text);
        bossBar.setColor(color);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    private static void updateWorldBossBar(String message, World world, String groupId) {
        String text = message.replace("text: ", "");
        BarColor color = BarColor.valueOf(
            message.contains("color:") ? message.split("color: ")[1].trim().toUpperCase() : "PURPLE"
        );

        String worldGroupId = groupId + "_" + world.getName();
        BossBar bossBar = bossBars.get(worldGroupId);
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(text, color, BarStyle.SOLID);
            bossBars.put(worldGroupId, bossBar);
        }

        bossBar.setTitle(text);
        bossBar.setColor(color);

        for (Player player : world.getPlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    public static void stopScheduler() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
    }
} 