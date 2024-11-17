package cn.ningmo;

import cn.ningmo.utils.LanguageManager;
import cn.ningmo.commands.CommandHandler;
import cn.ningmo.listeners.PlayerListener;
import cn.ningmo.scheduler.MessageScheduler;
import cn.ningmo.utils.UpdateChecker;
import cn.ningmo.utils.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class TimingMessage extends JavaPlugin {
    private static TimingMessage instance;
    private LanguageManager languageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 加载配置
        saveDefaultConfig();
        
        // 初始化语言管理器
        languageManager = new LanguageManager(this);
        
        // 显示启动信息
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.start")));
        
        // 加载配置文件
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.config")));
        
        // 注册命令
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.command")));
        getCommand("timingmessage").setExecutor(new CommandHandler());
        
        // 注册监听器
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.listener")));
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        // 启动消息调度器
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.scheduler")));
        MessageScheduler.startScheduler();
        
        // 检查更新
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.update-checker")));
        new UpdateChecker(this).checkForUpdates();
        
        // 加载完成
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("loading.complete")));
    }
    
    @Override
    public void onDisable() {
        // 停止消息调度器
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("disable.scheduler")));
        MessageScheduler.stopScheduler();
        
        // 卸载完成
        getLogger().info(MessageUtil.colorize(languageManager.getMessage("disable.complete")));
    }
    
    public static TimingMessage getInstance() {
        return instance;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
} 