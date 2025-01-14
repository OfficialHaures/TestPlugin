package nl.inferno.testPlugin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import nl.inferno.testPlugin.Commands.HideMessageCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class TestPlugin extends JavaPlugin {

    private LuckPerms luckPerms;
    private FileConfiguration config;
    private Map<String, List<String>> permissionMessages = new HashMap<>();
    private List<String> defaultMessages;
    private Random random = new Random();
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        loadMessages();
        this.adventure = BukkitAudiences.create(this);
        this.miniMessage = MiniMessage.miniMessage();

        setupLuckPerms();
        startBroadcastTask();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure = null;
        }
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getCommand("hidemessage").setExecutor(new HideMessageCommand(this, luckPerms));
        } else {
            getLogger().severe("LuckPerms not found! Plugin will not function correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadMessages() {
        defaultMessages = config.getStringList("default_messages");

        for (String key : config.getKeys(false)) {
            if (key.endsWith("_messages") && !key.equals("default_messages")) {
                String permission = config.getString(key + ".permission");
                List<String> messages = config.getStringList(key + ".messages");
                if (permission != null && !messages.isEmpty()) {
                    permissionMessages.put(permission, messages);
                }
            }
        }
    }

    private void startBroadcastTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastNextMessage();
            }
        }.runTaskTimer(this, 0L, config.getLong("broadcast-interval") * 20L);
    }

    private void broadcastNextMessage() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<String> playerMessagePool = new ArrayList<>(defaultMessages);

            for (Map.Entry<String, List<String>> entry : permissionMessages.entrySet()) {
                if (player.hasPermission(entry.getKey())) {
                    playerMessagePool.addAll(entry.getValue());
                }
            }

            if (!playerMessagePool.isEmpty()) {
                String randomMessage = playerMessagePool.get(random.nextInt(playerMessagePool.size()));

                if (!player.hasPermission("testplugin.hide." + getMessageIndex(randomMessage))) {
                    Component message = miniMessage.deserialize(randomMessage);
                    Component clickable = Component.text("[Dit bericht niet meer zien]")
                            .color(NamedTextColor.YELLOW)
                            .clickEvent(ClickEvent.runCommand("/hidemessage " + getMessageIndex(randomMessage)))
                            .hoverEvent(HoverEvent.showText(Component.text("Klik hier om dit bericht niet meer te zien")));

                    adventure.player(player).sendMessage(message.append(Component.space()).append(clickable));
                }
            }
        }
    }

    private int getMessageIndex(String message) {
        return message.hashCode();
    }

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}