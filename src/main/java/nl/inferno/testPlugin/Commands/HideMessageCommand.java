package nl.inferno.testPlugin.Commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import nl.inferno.testPlugin.TestPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HideMessageCommand implements CommandExecutor {
    private final TestPlugin plugin;
    private final LuckPerms luckPerms;

    public HideMessageCommand(TestPlugin plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage("Usage: /hidemessage <messageIndex>");
            return true;
        }

        int messageIndex;
        try {
            messageIndex = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid message index.");
            return true;
        }

        String permission = "testplugin.hide." + messageIndex;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            user.data().add(Node.builder(permission).build());
            luckPerms.getUserManager().saveUser(user);
            player.sendMessage("You will no longer see this message.");
        } else {
            player.sendMessage("An error occurred while updating your permissions.");
        }

        return true;
    }
}
