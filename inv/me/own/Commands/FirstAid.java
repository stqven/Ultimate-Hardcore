package inv.me.own.Commands;

import inv.me.own.HCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FirstAid implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("firstaid")) {
            Player p = (Player) sender;
            if (p.hasPermission("uhc.heal") || p.isOp()) {
                if (args.length == 0) {
                    HCPlayer.getHCP(p).setBreakLeg(false);
                    HCPlayer.getHCP(p).setBleeding(false);
                } else {
                    Player t = Bukkit.getPlayer(args[0]);
                    if (t != null) {
                        HCPlayer.getHCP(t).setBreakLeg(false);
                        HCPlayer.getHCP(t).setBleeding(false);
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
