package cn.ningmo.utils;

import cn.ningmo.TimingMessage;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private final TimingMessage plugin;
    private final String GITHUB_API_URL = "https://api.github.com/repos/ning-g-mo/TimingMessage/releases/latest";

    public UpdateChecker(TimingMessage plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getLogger().info(MessageUtil.colorize(
                    plugin.getLanguageManager().getMessage("plugin.version-check")
                ));
                
                URL url = new URL(GITHUB_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "TimingMessage Plugin");

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String latestVersion = parseVersion(response.toString());
                    String currentVersion = plugin.getDescription().getVersion();

                    if (compareVersions(currentVersion, latestVersion) < 0) {
                        String message = plugin.getLanguageManager()
                            .getMessage("plugin.update-found")
                            .replace("%version%", latestVersion);
                        plugin.getLogger().info(MessageUtil.colorize(message));
                    } else {
                        plugin.getLogger().info(MessageUtil.colorize(
                            plugin.getLanguageManager().getMessage("plugin.no-update")
                        ));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("检查更新时发生错误: " + e.getMessage());
            }
        });
    }

    private String parseVersion(String response) {
        try {
            if (response.contains("\"tag_name\":\"")) {
                String version = response.split("\"tag_name\":\"")[1].split("\"")[0];
                return version.startsWith("v") ? version.substring(1) : version;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("解析版本信息时发生错误");
        }
        return "0.0.0";
    }

    /**
     * 比较两个版本号
     * @return -1 如果version1 < version2, 0 如果相等, 1 如果version1 > version2
     */
    private int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        
        int length = Math.max(v1.length, v2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            
            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        return 0;
    }
} 