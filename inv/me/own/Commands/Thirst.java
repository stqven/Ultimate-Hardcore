package inv.me.own.Commands;

import inv.me.own.HCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Thirst implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("thirst")) {
            Player p = (Player) sender;
            if (p.hasPermission("thirst.use") || p.isOp()) {
                if (args.length == 0) {
                    HCPlayer.getHCP(p).refillThirst();
                } else {
                    Player t = Bukkit.getPlayer(args[0]);
                    if (t != null) {
                        HCPlayer.getHCP(t).refillThirst();
                    } else {
                        p.sendMessage("§cPlayer " + args[0] + " isn't online");
                    }
                }
            } else {
                p.sendMessage("§cYou don't have enough permissions to execute this command");
            }
        }
        return false;
    }
}
