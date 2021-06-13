package inv.me.own;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class HCPlayer {
    private static HashMap<String, HCPlayer> players = new HashMap<>();
    private String pname;
    private BossBar thirstBar;
    private BossBar temperatureBar;
    private double thirst = 100;
    private double temperature = 50;
    private String deathReason = null;

    //Timers:
    private long thirstIncrease = 0;
    private long thirstDecrease = 0;
    private long temperatureIncrease = 0;
    private long temperatureDecrease = 0;

    //Counters:
    private int diggingCounter = 0;
    private int buildCounter = 0;
    private int combatCounter = 0;

    //Checkers:
    private boolean isDigging = false;
    private boolean isBroken = false;
    private boolean isBleeding = false;

    public HCPlayer(String pname) {
        this.pname = pname;
        thirstBar = Bukkit.createBossBar(Config.getBossBarThirstTitle().replaceAll("%value%", Integer.toString(((int) thirst))), Config.getBossBarThirstColor(), BarStyle.SOLID, BarFlag.DARKEN_SKY);
        temperatureBar = Bukkit.createBossBar(Config.getBossBarTemperatureTitle().replaceAll("%value%", Integer.toString((int)temperature)), Config.getBossBarTemperatureColor(), BarStyle.SOLID, BarFlag.DARKEN_SKY);
        updateBars();
    }

    public static HCPlayer getHCP(Player p) {
        String pname = p.getName();
        if (players.containsKey(pname)) {
            HCPlayer hcp = players.get(pname);
            hcp.updateBars();
            return hcp;
        } else {
            HCPlayer hcp = new HCPlayer(pname);
            players.put(pname, hcp);
            return hcp;
        }
    }

    public void setDeathReason(String deathReason) {
        this.deathReason = deathReason;
    }

    public String getDeathReason() {
        return this.deathReason;
    }

    public void respawn() {
        setBreakLeg(false);
        setBleeding(false);
        setDigging(false);
        refillThirst();
        temperature = 50;
        combatCounter = 0;
        diggingCounter = 0;
        buildCounter = 0;
        thirstIncrease = 0;
        thirstDecrease = 0;
        temperatureIncrease = 0;
        temperatureDecrease = 0;
        updateBars();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(pname);
    }

    public static  ArrayList<HCPlayer> getPlayers() {
        ArrayList<HCPlayer> ps = new ArrayList<>();
        players.values().forEach(x -> ps.add(x));
        return ps;
    }
    public void updateBars() {
        thirstBar.setProgress(thirst/100.0);
        temperatureBar.setProgress(((double) temperature)/100.0);
        thirstBar.setTitle(Config.getBossBarThirstTitle().replaceAll("%value%", Integer.toString(((int) thirst))));
        temperatureBar.setTitle(Config.getBossBarTemperatureTitle().replaceAll("%value%", Integer.toString((int)temperature)));
        if (!thirstBar.getPlayers().contains(getPlayer())) {
            thirstBar.addPlayer(getPlayer());
            temperatureBar.addPlayer(getPlayer());
        }
    }

    public void setDigging(boolean isDigging) {
        this.isDigging = isDigging;
    }

    public void setBleeding(boolean isBleeding) {
        if (isBleeding && isGM()) return;
        this.isBleeding = isBleeding;
        if (!isBleeding) return;
        int time = Config.getBleedingTime();
        if (time != -1) {
            Bukkit.getServer().getScheduler().runTaskLater(Main.instance, () -> {
                this.isBleeding = false;
            }, 20L * time);
        }
    }

    public boolean isBleeding() {
        return isBleeding;
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void digBlock() {
        if (!isDigging) return;
        Pair<Integer, Double> digValues = Config.getThirstActivitiesReduce("mining");
        if ((++diggingCounter) >= 21*digValues.getKey()) {
            diggingCounter = 0;
            removeThirst(digValues.getValue(), true);
        }
    }

    public void buildBlock() {
        Pair<Integer, Double> buildValues = Config.getThirstActivitiesReduce("building");
        if ((++buildCounter) >= buildValues.getKey()) {
            buildCounter = 0;
            removeThirst(buildValues.getValue(), true);
        }
    }

    public boolean isGM() {
        return (Bukkit.getServer().getPluginManager().getPlugin("CMI") != null && CMI.getInstance().getPlayerManager().getUser(getPlayer()).isGod());
    }

    public void setBreakLeg(boolean isBroken) {
        if (isGM()) return;
        if (isBroken) {
            this.isBroken = isBroken;
            Config.getInjuryPotions().forEach(potion -> getPlayer().addPotionEffect(potion));
        } else {
            this.isBroken = isBroken;
            Config.getInjuryPotions().forEach(potion -> {
                if (getPlayer().hasPotionEffect(potion.getType())) getPlayer().removePotionEffect(potion.getType());
            });
        }
    }

    public void combat() {
        Pair<Integer, Double> combatValues = Config.getThirstActivitiesReduce("combat");
        if ((++combatCounter) >= combatValues.getKey()) {
            combatCounter = 0;
            removeThirst(combatValues.getValue(), true);
        }
    }

    public void addThirst(double thirst, boolean ignoreTimer) {
        if (isGM()) return;
        if (thirst < 0) {
            removeThirst(thirst*-1, ignoreTimer);
            return;
        }
        if (!ignoreTimer) {
            if (thirstIncrease + Config.getIncreaseTime(false)*1000 > System.currentTimeMillis() + 100) return;
            thirstIncrease = System.currentTimeMillis();
        }
        if (this.thirst + thirst > 100) {
            this.thirst = 100;
        } else {
            this.thirst += thirst;
        }
        updateBars();
    }

    public void removeThirst(double thirst, boolean ignoreTimer) {
        if (isGM()) return;
        if (thirst < 0) {
            addThirst(thirst*-1, ignoreTimer);
            return;
        }
        if (Config.checkFlag(getPlayer(), "thirst-draining")) return;
        if (!ignoreTimer) {
            if (thirstDecrease + Config.getDecrease(false)*1000 > System.currentTimeMillis() + 100) return;
            thirstDecrease = System.currentTimeMillis();
        }
        if (this.thirst - thirst <= 0) {
            this.thirst = 0;
        } else {
            this.thirst -= thirst;
        }
        if (this.thirst <= 0) {
            deathReason = Config.getDeathReason("low-thirst", pname);
            getPlayer().setHealth(0);
        }
        updateBars();
    }

    public void refillThirst() {
        this.thirst = 100;
    }

    public void damage(double value, String msg) {
        Player p = getPlayer();
        if (p.getHealth() - value <= 0) {
            deathReason = msg;
        }
        p.damage(value);
    }

    public void addTemperature(double temperature, boolean ignoreTimer) {
        if (isGM()) return;
        if (temperature < 0) {
            removeTemperature(temperature*-1, ignoreTimer);
            return;
        }
        if (!ignoreTimer) {
            if (temperatureIncrease + Config.getIncreaseTime(true)*1000 > System.currentTimeMillis() + 100) return;
            temperatureIncrease = System.currentTimeMillis();
        }
        if (this.temperature + temperature > 100) {
            this.temperature = 100;
        } else {
            this.temperature += temperature;
        }
        if (this.temperature >= 100) {
            deathReason = Config.getDeathReason("high-temperature", pname);
            getPlayer().setHealth(0);
        }
        updateBars();
        temperatureIncrease = System.currentTimeMillis();
    }

    public void removeTemperature(double temperature, boolean ignoreTimer) {
        if (isGM()) return;
        if (temperature < 0) {
            addTemperature(temperature*-1, ignoreTimer);
            return;
        }
        if (!ignoreTimer) {
            if (temperatureIncrease + Config.getIncreaseTime(true)*1000 > System.currentTimeMillis() + 100) return;
            thirstDecrease = System.currentTimeMillis();
        }
        if (this.temperature - temperature <= 0) {
            this.temperature = 0;
            deathReason = Config.getDeathReason("low-temperature", pname);
            getPlayer().setHealth(0);
        } else {
            this.temperature -= temperature;
        }
        updateBars();
        temperatureDecrease = System.currentTimeMillis();
    }

    public double getThirst() {
        return thirst;
    }

    public double getTemperature() {
        return temperature;
    }
}
