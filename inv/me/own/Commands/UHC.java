package inv.me.own.Commands;

import inv.me.own.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UHC implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("uhc")) {
            Player p = (Player) sender;
            if (p.hasPermission("uhc.reload") || p.isOp()) {
                if (args.length != 0) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        Main.instance.reloadConfig();
                        p.sendMessage("§aconfig.yml file has been reloaded");
                    } else {
                        p.sendMessage("§c/uhc reload");
                    }
                } else {
                    p.sendMessage("§c/uhc reload");
                }
            } else {
                p.sendMessage("§cYou don't have enough permissions to execute this command");
            }
        }
        return false;
    }
}
