package inv.me.own;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Utilities {

    public static boolean isBlockInArea(Player p, ArrayList<Material> types, int times) {
        for (int y = times * -1; y <= times; y++) {
            for (int x = times * -1; x <= times; x++) {
                for (int z = times * -1; z <= times; z++) {
                    if (types.contains(p.getLocation().add(x, y, z).getBlock().getType())) return true;
                }
            }
        }
        return false;
    }

    public static boolean isInOcean(Location loc) {
        double water = 0, total = 0;
        for (int x = -25; x < 25; x++) {
            for (int z = -25; z < 25; z++) {
                for (int y = -25; y < 25; y++) {
                    Material mat = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getType();
                    if (mat != Material.AIR) total++;
                    if (mat == Material.WATER) water++;
                }
            }
        }
        return (water/total > 0.09);
    }
}
