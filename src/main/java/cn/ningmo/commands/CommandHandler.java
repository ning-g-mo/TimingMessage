package cn.ningmo.commands;

import cn.ningmo.TimingMessage;
import cn.ningmo.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {
    private final TimingMessage plugin = TimingMessage.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("timingmessage.admin")) {
            sender.sendMessage(MessageUtil.colorize(plugin.getLanguageManager().getMessage("plugin.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                plugin.getLanguageManager().loadLanguages();
                sender.sendMessage(MessageUtil.colorize(plugin.getLanguageManager().getMessage("plugin.reload")));
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== 定时消息插件帮助 ===");
        sender.sendMessage("§e/tm reload §7- §f重载插件配置");
    }
} 