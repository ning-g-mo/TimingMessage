package cn.ningmo.listeners;

import cn.ningmo.TimingMessage;
import cn.ningmo.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final TimingMessage plugin = TimingMessage.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("features.join-message.enabled")) return;
        
        Player player = event.getPlayer();
        String message = plugin.getLanguageManager().getMessage("messages.join");
        message = MessageUtil.format(message, player);
        
        if (plugin.getConfig().getBoolean("features.join-message.broadcast")) {
            event.setJoinMessage(message);
        } else {
            event.setJoinMessage(null);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("features.quit-message.enabled")) return;
        
        Player player = event.getPlayer();
        String message = plugin.getLanguageManager().getMessage("messages.quit");
        message = MessageUtil.format(message, player);
        
        if (plugin.getConfig().getBoolean("features.quit-message.broadcast")) {
            event.setQuitMessage(message);
        } else {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!plugin.getConfig().getBoolean("features.world-change.enabled")) return;
        
        Player player = event.getPlayer();
        String newWorldName = player.getWorld().getName();
        String oldWorldName = event.getFrom().getName();
        
        handleWorldMessage(player, newWorldName, "enter");
        handleWorldMessage(player, oldWorldName, "leave");
    }
    
    private void handleWorldMessage(Player player, String worldName, String type) {
        ConfigurationSection worldConfig = plugin.getConfig()
            .getConfigurationSection("features.world-change.worlds." + worldName + "." + type);
            
        if (worldConfig != null) {
            String message = worldConfig.getString("message");
            if (message != null && !message.isEmpty()) {
                message = MessageUtil.format(message, player);
                
                if (worldConfig.getBoolean("broadcast")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(message);
                    }
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }
} 