package cn.ningmo.utils;

import cn.ningmo.TimingMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final TimingMessage plugin;
    private FileConfiguration languageConfig;
    private final Map<String, FileConfiguration> loadedLanguages = new HashMap<>();
    private String currentLanguage;

    public LanguageManager(TimingMessage plugin) {
        this.plugin = plugin;
        loadLanguages();
    }

    public void loadLanguages() {
        // 确保语言文件夹存在
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveDefaultLanguages();
        }

        // 加载配置中指定的语言
        currentLanguage = plugin.getConfig().getString("settings.language", "zh_CN");
        boolean useDefaultIfMissing = plugin.getConfig().getBoolean("settings.use-default-if-missing", true);

        // 尝试加载指定语言
        File langFile = new File(langFolder, currentLanguage + ".yml");
        if (langFile.exists()) {
            languageConfig = YamlConfiguration.loadConfiguration(langFile);
        } else if (useDefaultIfMissing) {
            // 如果找不到指定语言且允许使用默认语言，加载中文
            currentLanguage = "zh_CN";
            langFile = new File(langFolder, "zh_CN.yml");
            if (langFile.exists()) {
                languageConfig = YamlConfiguration.loadConfiguration(langFile);
            } else {
                saveDefaultLanguage("zh_CN");
                languageConfig = YamlConfiguration.loadConfiguration(langFile);
            }
        }

        // 加载所有可用语言
        for (File file : langFolder.listFiles((dir, name) -> name.endsWith(".yml"))) {
            String langCode = file.getName().replace(".yml", "");
            loadedLanguages.put(langCode, YamlConfiguration.loadConfiguration(file));
        }
    }

    private void saveDefaultLanguages() {
        saveDefaultLanguage("zh_CN");
        saveDefaultLanguage("en_US");
    }

    private void saveDefaultLanguage(String lang) {
        File langFile = new File(plugin.getDataFolder(), "languages/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("languages/" + lang + ".yml", false);
        }
    }

    public String getMessage(String path) {
        return getMessage(path, currentLanguage);
    }

    public String getMessage(String path, String lang) {
        FileConfiguration langConfig = loadedLanguages.getOrDefault(lang, languageConfig);
        String message = langConfig.getString(path);
        
        if (message == null) {
            // 如果在当前语言中找不到，尝试在默认语言中查找
            langConfig = loadedLanguages.get("zh_CN");
            if (langConfig != null) {
                message = langConfig.getString(path);
            }
        }
        
        return message != null ? message : path;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String lang) {
        if (loadedLanguages.containsKey(lang)) {
            currentLanguage = lang;
            languageConfig = loadedLanguages.get(lang);
        }
    }

    public boolean isLanguageAvailable(String lang) {
        return loadedLanguages.containsKey(lang);
    }
} 