package inv.me.own;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import inv.me.own.Commands.FirstAid;
import inv.me.own.Commands.Thirst;
import inv.me.own.Commands.UHC;
import javafx.util.Pair;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class Main extends JavaPlugin implements Listener {

    public static Main instance;
    public static ProtocolManager pm;

    public void onEnable() {
        registerEvents(new Events());
        registerCommands(new Pair<>("firstaid", new FirstAid()), new Pair<>("thirst", new Thirst()), new Pair<>("uhc", new UHC()));
        addRecipes(Config.getBrokenLegRecipe(), Config.getBandageRecipe(), Config.getHelmetRecipe(), Config.getChestplateRecipe(),
                Config.getLeggingsRecipe(), Config.getBootsRecipe());
        pm = ProtocolLibrary.getProtocolManager();
        Events.onBlockDigging();
        System.out.println("[Hardcore] The plugin has been enabled!");
        for (Player all : Bukkit.getOnlinePlayers()) {
            HCPlayer.getHCP(all);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (HCPlayer hcp : HCPlayer.getPlayers()) {
                    Player all = hcp.getPlayer();
                    if (all.isDead()) return;
                    if (all == null) continue;
                    if (hcp.isGM()) {
                        hcp.setBreakLeg(false);
                        hcp.setBleeding(false);
                        hcp.setDigging(false);
                        continue;
                    }
                    Location loc = all.getLocation();
                    Biome biome = all.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    if (Config.checkFlag(all, "temperature-increase") && !Utilities.isBlockInArea(all, Config.getColdSources(), 3)) {
                        hcp.addTemperature(Config.isFullLeather(all, false)? 1 : 2, false);
                    } else if (Config.checkFlag(all, "temperature-decrease") && !Utilities.isBlockInArea(all, Config.getHeatSources(), 3)) {
                        hcp.removeTemperature(1, false);
                    } else if (!Utilities.isBlockInArea(all, Config.getColdSources(), 3) && ((Config.getHeatBiomes().containsKey(biome) || Config.getHeatWorlds().containsKey(all.getWorld())) && !(Config.isFullLeather(all, true) && ( (Config.getHeatBiomes().containsKey(biome) && Config.getHeatBiomes().get(biome)) || (Config.getHeatWorlds().containsKey(all.getWorld()) && Config.getHeatWorlds().get(all.getWorld())))))) {
                        hcp.addTemperature(Config.isFullLeather(all, false)? 1 : 2, false);
                    } else if ((Config.getColdBiomes().contains(biome) ||
                            Config.getColdWorlds().contains(all.getWorld())) &&
                            !Utilities.isBlockInArea(all, Config.getHeatSources(), 3)) {
                        hcp.removeTemperature(1, false);
                    } else {
                        if (hcp.getTemperature() < 50) {
                            HCPlayer.getHCP(all).addTemperature(1, false);
                        } else if (hcp.getTemperature() > 50) {
                            HCPlayer.getHCP(all).removeTemperature(1, false);
                        }
                    }
                    if (all.isSprinting()) {
                        Pair<Integer, Double> sprintingValues = Config.getThirstActivitiesReduce("sprinting");
                        hcp.removeThirst(sprintingValues.getValue()/sprintingValues.getKey(), true);
                    }
                    if (all.getHealth() == 20) hcp.setBreakLeg(false);
                    if (hcp.isBleeding() || hcp.isBroken()) {
                        all.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Config.getActionBarMessage(hcp)));
                    }
                    if (hcp.isBleeding()) {
                        hcp.removeThirst(Config.getThirstBleedingReduce(), true);
                        if (hcp.isBroken()) {
                            hcp.damage(Config.getBleedingDamage()*2, Config.getDeathReason("bleeding-and-broken-leg", hcp.getPlayer().getName()));
                        } else {
                            hcp.damage(Config.getBleedingDamage()*2, Config.getDeathReason("bleeding", hcp.getPlayer().getName()));
                        }
                        all.getWorld().playEffect(all.getLocation(),Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                    }
                    if (hcp.isBroken()) {
                        if (hcp.isBleeding()) {
                            hcp.damage(Config.getBrokenLegDamage()*2, Config.getDeathReason("bleeding-and-broken-leg", hcp.getPlayer().getName()));
                        } else {
                            hcp.damage(Config.getBrokenLegDamage()*2, Config.getDeathReason("broken-leg", hcp.getPlayer().getName()));
                        }
                    }
                    if (hcp.getTemperature() <= 32) {
                        hcp.damage(Config.getLowThirstDamage(), Config.getDeathReason("low-temperature", hcp.getPlayer().getName()));
                        all.sendTitle(Config.getColdTitle(), Config.getColdSubtitle(),0, 30, 5);
                    } else if (hcp.getTemperature() >= 80) {
                        all.sendTitle(Config.getHeatTitle(), Config.getHeatSubtitle(),0, 30, 5);
                    }
                    if (hcp.getThirst() <= 10) {
                        all.sendTitle(Config.getThirstTitle(), Config.getThirstSubtitle(),0, 30, 5);
                        hcp.damage(Config.getLowThirstDamage(), Config.getDeathReason("low-thirst", hcp.getPlayer().getName()));
                    }
                    if (Config.isNightTime(all.getWorld()) && !Config.getHeatWorlds().containsKey(all.getWorld())) {
                        hcp.removeTemperature(Config.getThirstNightReduce(), true);
                    }
                    if (Utilities.isBlockInArea(all, new ArrayList<>(Arrays.asList(Material.LAVA)), 3)) {
                        if (!Config.isFullLeather(all, true)) {
                            hcp.damage(1, Config.getDeathReason("lava", hcp.getPlayer().getName()));
                        }
                    }
                }
            }
        }.runTaskTimer(Main.instance, 0L, 20L); //Delays in ticks
        saveDefaultConfig();
    }
    public void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public void registerCommands(Pair<String, CommandExecutor>... cmds) {
        for (Pair<String, CommandExecutor> cmd : cmds) {
            getCommand(cmd.getKey()).setExecutor(cmd.getValue());
        }
    }

    public void addRecipes(ShapedRecipe... recipes) {
        for (ShapedRecipe recipe : recipes) {
            Bukkit.addRecipe(recipe);
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        loadFlag("thirst-draining", false);
        loadFlag("temperature-increase", true);
        loadFlag("temperature-decrease", true);
    }

    public void loadFlag(String name, boolean state) {
        WorldGuard.getInstance().getFlagRegistry().register(new StringFlag(name, (state? "allow" : "deny")));
    }

    public void onDisable() {
        instance = null;
        System.out.println("[Hardcore] The plugin has been disabled!");
    }
}
