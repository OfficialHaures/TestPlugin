package nl.inferno.testPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.inferno.testPlugin.Commands.HideMessageCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;


import java.util.List;

public final class TestPlugin extends JavaPlugin {

    private LuckPerms luckPerms;
    private FileConfiguration config;
    private List<String> messages;
    private int currentMessageIndex = 0;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        messages = config.getStringList("messages");
        this.adventure = BukkitAudiences.create(this);
        this.miniMessage = MiniMessage.miniMessage();

        setupLuckPerms();
        startBroadcastTask();
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getCommand("hidemessage").setExecutor(new HideMessageCommand(this, luckPerms));

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
        if (messages.isEmpty()) return;

        String rawMessage = messages.get(currentMessageIndex);
        String permission = "testplugin.hide." + currentMessageIndex;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(permission)) {
                Component message = miniMessage.deserialize(rawMessage);
                Component clickable = Component.text("[Dit bericht niet meer zien]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/hidemessage " + currentMessageIndex))
                        .hoverEvent(HoverEvent.showText(Component.text("Klik hier om dit bericht niet meer te zien")));

                Component fullMessage = message.append(Component.newline()).append(clickable);

                String jsonMessage = GsonComponentSerializer.gson().serialize(fullMessage);
                player.spigot().sendMessage(net.md_5.bungee.chat.ComponentSerializer.parse(jsonMessage));
            }
        }

        currentMessageIndex = (currentMessageIndex + 1) % messages.size();
    }




    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
        }
    }
}
