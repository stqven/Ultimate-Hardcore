package inv.me.own;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import inv.me.own.exceptions.ConfigNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public class Config {
    private static String getText(String path) throws ConfigNotFoundException {
        if (Main.instance.getConfig().contains(path)) return Main.instance.getConfig().getString(path).replaceAll("&", "§");
        throw new ConfigNotFoundException("Can't find '" + path + "' in the config file");
    }

    private static ArrayList<String> getList(String path) throws ConfigNotFoundException {
        if (Main.instance.getConfig().contains(path)) return (ArrayList<String>) Main.instance.getConfig().getStringList(path);
        throw new ConfigNotFoundException("Can't find the list '" + path + "' in the config file");
    }

    private static int getInt(String str) throws ConfigNotFoundException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new ConfigNotFoundException("Error with reading a number value");
        }
    }
    private static double getDouble(String str) throws ConfigNotFoundException {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            throw new ConfigNotFoundException("Error with reading a number value");
        }
    }

    private static long getLong(String str) throws ConfigNotFoundException {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            throw new ConfigNotFoundException("Error with reading a number value");
        }
    }

    private static long getNightStart() {
        try {
            return getLong(getText("Time.night-start"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private static long getNightEnd() {
        try {
            return getLong(getText("Time.night-end"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static boolean isNightTime(World w) {
        long start = getNightStart();
        long end = getNightEnd();
        long ct = w.getTime();
        return ((start <= end && (ct >= start && ct <= end)) || (start > end && ((ct >= start && ct <= 24000) || (ct >= 0 && ct <= end))));
    }

    public static ArrayList<Biome> getColdBiomes() {
        try {
            ArrayList<String> biomesStr = getList("Cold.biomes");
            ArrayList<Biome> biomes = new ArrayList<>();
            for (String biome : biomesStr) {
                biomes.add(Biome.valueOf(biome));
            }
            return biomes;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static HashMap<Biome, Boolean> getHeatBiomes() {
        try {
            ArrayList<String> biomesStr = getList("Heat.biomes");
            HashMap<Biome, Boolean> biomes = new HashMap<>();
            for (String biome : biomesStr) {
                String[] bstr = biome.split(" ");
                biomes.put(Biome.valueOf(bstr[0]), (bstr[1].equals("true"))? true : false);
            }
            return biomes;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ArrayList<World> getColdWorlds() {
        try {
            ArrayList<String> wArr = getList("Cold.worlds");
            ArrayList<World> worlds = new ArrayList<>();
            for (String wName : wArr) {
                for (World w : Bukkit.getServer().getWorlds()) {
                    if (w.getName().equalsIgnoreCase(wName)) {
                        worlds.add(w);
                        break;
                    }
                }
            }
            return worlds;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static HashMap<World, Boolean> getHeatWorlds() {
        try {
            ArrayList<String> wArr = getList("Heat.worlds");
            HashMap<World, Boolean> worlds = new HashMap<>();
            for (String w : wArr) {
                String[] bstr = w.split(" ");
                for (World wr : Bukkit.getServer().getWorlds()) {
                    if (wr.getName().equalsIgnoreCase(bstr[0])) {
                        worlds.put(wr, ((bstr[1].equals("true"))? true : false));
                        break;
                    }
                }
            }
            return worlds;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Material> getHeatSources() {
        try {
            ArrayList<String> materialStr = getList("Heat.sources");
            ArrayList<Material> materials = new ArrayList<>();
            for (String material : materialStr) {
                materials.add(Material.getMaterial(material));
            }
            return materials;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Material> getColdSources() {
        try {
            ArrayList<String> materialStr = getList("Cold.sources");
            ArrayList<Material> materials = new ArrayList<>();
            for (String material : materialStr) {
                materials.add(Material.getMaterial(material));
            }
            return materials;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static double getThirstBleedingReduce() {
        try {
            return getDouble(getText("Thirst.bleeding-reduce"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static double getThirstNightReduce() {
        try {
            return getDouble(getText("Thirst.night-reduce"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static Pair<Integer, Double> getThirstActivitiesReduce(String activity) {
        try {
            String[] str = getText("Thirst.activities-reduce." + activity).split(" ");
            return new Pair<>(Integer.parseInt(str[0]), Double.parseDouble(str[1]));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Thirst.activities-reduce." + activity + " Input should only contain numeric values");
        }
        return new Pair<>(1, 0.1);
    }

    public static HashMap<Material, Integer> getThirstFood() {
        try {
            ArrayList<String> materialStr = getList("Thirst.food");
            HashMap<Material, Integer> materials = new HashMap<>();
            for (String material : materialStr) {
                String[] matArr = material.split(" ");
                Material m = Material.getMaterial(matArr[0]);
                if (m != null) {
                    try {
                        materials.put(m, Integer.parseInt(matArr[1]));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return materials;
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getBossBarThirstTitle() {
        try {
            return getText("BossBar.Thirst.Title");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static BarColor getBossBarThirstColor() {
        try {
            String str = getText("BossBar.Thirst.Color");
            return BarColor.valueOf(str);
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
            return BarColor.BLUE;
        }
    }

    public static BarColor getBossBarTemperatureColor() {
        try {
            String str = getText("BossBar.Temperature.Color");
            return BarColor.valueOf(str);
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
            return BarColor.RED;
        }
    }

    public static String getBossBarTemperatureTitle() {
        try {
            return getText("BossBar.Temperature.Title");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static int getIncreaseTime(boolean heat) {
        try {
            return getInt(getText((heat? "Heat" : "Cold") + ".increase-time"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static int getDecrease(boolean heat) {
        try {
            return getInt(getText((heat? "Heat" : "Cold") + ".decrease-time"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static boolean getInjuryChance(int value) {
        for (int i = 21; i >= 5; i--) {
            if (Main.instance.getConfig().contains("Injury.broken-legs.chances." + i + ".chance")) {
                try {
                    int chance = Integer.parseInt(Main.instance.getConfig().getString("Injury.broken-legs.chances." + i + ".chance").replace("%", ""));
                    if (value < i) continue;
                    if ((new Random().nextInt(100)) < chance) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Error at Injury.broken-legs.chances." + i + ".chance " + " chance must be a number");
                }
            }
        }
        return false;
    }

    public static ArrayList<PotionEffect> getInjuryPotions() {
        try {
            ArrayList<PotionEffect> potions = new ArrayList<>();
            ArrayList<String> potionsArr = getList("Injury.broken-legs.potions");
            for (String potionStr : potionsArr) {
                String[] parr = potionStr.split(" ");
                PotionEffectType type = PotionEffectType.getByName(parr[0]);
                if (type != null) {
                    potions.add(new PotionEffect(type,Integer.MAX_VALUE, Integer.parseInt(parr[1]) - 1));
                }
            }
            return potions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static HashMap<String, Integer> getBleedingList() {
        HashMap<String, Integer> bhash = new HashMap<>();
        Set<String> set = Main.instance.getConfig().getConfigurationSection("Injury.bleeding.chances").getKeys(false);
        for (String str : set) {
            bhash.put(str, Integer.parseInt(Main.instance.getConfig().getString("Injury.bleeding.chances." + str).replace("%", "")));
        }
        return bhash;
    }

    private static HashMap<Material, Integer> getBleedingItems() {
        HashMap<Material, Integer> total = new HashMap<>();
        HashMap<String, Integer> bhash = getBleedingList();
        bhash.forEach(((k, v) -> {
            if (k.startsWith("ITEM_")) {
                total.put(Material.getMaterial(k.replace("ITEM_", "")), v);
            }
        }));
        return total;
    }

    public static boolean containsBleedingItems(Material mat) {
        HashMap<Material, Integer> hash = getBleedingItems();
        if (hash.containsKey(mat)) {
            if (new Random().nextInt(100) < hash.get(mat)) {
                return true;
            }
        }
        return false;
    }

    private static HashMap<EntityType, Integer> getBleedingETypes() {
        HashMap<EntityType, Integer> total = new HashMap<>();
        HashMap<String, Integer> bhash = getBleedingList();
        bhash.forEach(((k, v) -> {
            if (k.startsWith("MOB_")) {
                total.put(EntityType.valueOf(k.replace("MOB_", "")), v);
            }
        }));
        return total;
    }

    public static boolean containsBleedingETypes(EntityType type) {
        HashMap<EntityType, Integer> hash = getBleedingETypes();
        if (hash.containsKey(type)) {
            if (new Random().nextInt(100) < hash.get(type)) {
                return true;
            }
        }
        return false;
    }

    private static HashMap<Material, Integer> getBleedingBlocks() {
        HashMap<Material, Integer> total = new HashMap<>();
        HashMap<String, Integer> bhash = getBleedingList();
        bhash.forEach(((k, v) -> {
            if (k.startsWith("BLOCK_")) {
                total.put(Material.getMaterial(k.replace("BLOCK_", "")), v);
            }
        }));
        return total;
    }

    public static boolean containsBleedingBlocks(Material mat) {
        HashMap<Material, Integer> hash = getBleedingBlocks();
        if (hash.containsKey(mat)) {
            if (new Random().nextInt(100) < hash.get(mat)) {
                return true;
            }
        }
        return false;
    }

    private static HashMap<EntityType, Integer> getBleedingProjectile() {
        HashMap<EntityType, Integer> total = new HashMap<>();
        HashMap<String, Integer> bhash = getBleedingList();
        bhash.forEach(((k, v) -> {
            if (k.startsWith("PROJECTILE_")) {
                total.put(EntityType.valueOf(k.replace("PROJECTILE_", "")), v);
            }
        }));
        return total;
    }

    public static boolean containsBleedingProjectile(EntityType type) {
        HashMap<EntityType, Integer> hash = getBleedingProjectile();
        if (hash.containsKey(type)) {
            if (new Random().nextInt(100) < hash.get(type)) {
                return true;
            }
        }
        return false;
    }

    public static int getBleedingTime() {
        try {
            return getInt(getText("Injury.bleeding.time"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    public static double getBleedingDamage() {
        try {
            return getDouble(getText("Injury.bleeding.damage"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static double getLowThirstDamage() {
        try {
            return getDouble(getText("Thirst.low-thirst-damage"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static double getLowTemperatureDamage() {
        try {
            return getDouble(getText("Heat.low-temperature-reduce"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static double getBrokenLegDamage() {
        try {
            return getDouble(getText("Injury.bleeding.damage"));
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static String getActionBarMessage(HCPlayer hcp) {
        String msg = "";
        try {
            if (hcp.isBleeding() && hcp.isBroken()) {
                return getText("Messages.actionbar-both");
            } else if (hcp.isBroken()) {
                return getText("Messages.actionbar-brokenleg");
            } else if (hcp.isBleeding()) {
                return getText("Messages.actionbar-bleeding");
            }
        } catch (ConfigNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }
    public static boolean checkFlag(Player p, String flag_name) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(p.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            Map<Flag<?>, Object> flags = region.getFlags();
            for (Flag flag : flags.keySet()) {
                if (flag.getName().equalsIgnoreCase(flag_name)) {
                    if (flag_name.equalsIgnoreCase("thirst-draining")) {
                        if (flags.get(flag).toString().equalsIgnoreCase("deny")) return true;
                    } else {
                        if (flags.get(flag).toString().equalsIgnoreCase("allow")) return true;
                    }
                }
            }
        }
        return false;
    }

    public static String getThirstTitle() {
        try {
            return getText("Messages.thirst-title");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getThirstSubtitle() {
        try {
            return getText("Messages.thirst-subtitle");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getColdTitle() {
        try {
            return getText("Messages.cold-title");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getColdSubtitle() {
        try {
            return getText("Messages.cold-subtitle");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    public static String getHeatTitle() {
        try {
            return getText("Messages.heat-title");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getHeatSubtitle() {
        try {
            return getText("Messages.heat-subtitle");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ShapedRecipe getBrokenLegRecipe() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta mitem = item.getItemMeta();
        mitem.setDisplayName(getBrokenLegName());
        item.setItemMeta(mitem);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("TST", " S ", "TST");
        recipe.setIngredient('T', Material.STICK);
        recipe.setIngredient('S', Material.STRING);
        return recipe;
    }

    public static String getBrokenLegName() {
        try {
            return getText("Recipes.broken-leg");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ShapedRecipe getBandageRecipe() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta mitem = item.getItemMeta();
        mitem.setDisplayName(getBandageName());
        item.setItemMeta(mitem);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("SPS", "PPP", "SPS");
        recipe.setIngredient('S', Material.SLIME_BALL);
        recipe.setIngredient('P', Material.PAPER);
        return recipe;
    }

    public static String getBandageName() {
        try {
            return getText("Recipes.bandage");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void addGlowToItem(ItemStack stack){
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(Enchantment.WATER_WORKER, 70, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
    }

    public static ShapedRecipe getHelmetRecipe() {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta mitem = (LeatherArmorMeta) item.getItemMeta();
        mitem.setDisplayName(getHelmetName());
        mitem.setColor(Color.AQUA);
        item.setItemMeta(mitem);
        addGlowToItem(item);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("SSS", "SAS", "SSS");
        recipe.setIngredient('A', Material.LEATHER_HELMET);
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(new ItemStack(Material.PACKED_ICE, 64)));
        return recipe;
    }

    public static String getHelmetName() {
        try {
            return getText("Recipes.helmet");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ShapedRecipe getChestplateRecipe() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta mitem = (LeatherArmorMeta) item.getItemMeta();
        mitem.setDisplayName(getChestplateName());
        mitem.setColor(Color.AQUA);
        item.setItemMeta(mitem);
        addGlowToItem(item);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("SSS", "SAS", "SSS");
        recipe.setIngredient('A', Material.LEATHER_CHESTPLATE);
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(new ItemStack(Material.PACKED_ICE, 64)));
        return recipe;
    }

    public static String getChestplateName() {
        try {
            return getText("Recipes.chestplate");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ShapedRecipe getLeggingsRecipe() {
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta mitem = (LeatherArmorMeta) item.getItemMeta();
        mitem.setDisplayName(getLeggingsName());
        mitem.setColor(Color.AQUA);
        item.setItemMeta(mitem);
        addGlowToItem(item);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("SSS", "SAS", "SSS");
        recipe.setIngredient('A', Material.LEATHER_LEGGINGS);
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(new ItemStack(Material.PACKED_ICE, 64)));
        return recipe;
    }

    public static String getLeggingsName() {
        try {
            return getText("Recipes.leggings");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static ShapedRecipe getBootsRecipe() {
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta mitem = (LeatherArmorMeta) item.getItemMeta();
        mitem.setDisplayName(getBootsName());
        mitem.setColor(Color.AQUA);
        item.setItemMeta(mitem);
        addGlowToItem(item);
        ShapedRecipe recipe = new ShapedRecipe(item);
        recipe.shape("SSS", "SAS", "SSS");
        recipe.setIngredient('A', Material.LEATHER_BOOTS);
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(new ItemStack(Material.PACKED_ICE, 64)));
        return recipe;
    }

    public static boolean isFullLeather(Player p, boolean ice) {
        PlayerInventory inv = p.getInventory();
        return (inv.getHelmet() != null && inv.getChestplate() != null && inv.getLeggings() != null && inv.getBoots() != null &&
                inv.getHelmet().getType() == Material.LEATHER_HELMET &&
                inv.getChestplate().getType() == Material.LEATHER_CHESTPLATE &&
                inv.getLeggings().getType() == Material.LEATHER_LEGGINGS &&
                inv.getBoots().getType() == Material.LEATHER_BOOTS && (
                ice && inv.getHelmet().getItemMeta().getDisplayName().equals(Config.getHelmetName()) &&
                        inv.getChestplate().getItemMeta().getDisplayName().equals(Config.getChestplateName()) &&
                        inv.getLeggings().getItemMeta().getDisplayName().equals(Config.getLeggingsName())  &&
                        inv.getBoots().getItemMeta().getDisplayName().equals(Config.getBootsName())
        ) || (!ice));
    }

    public static String getBootsName() {
        try {
            return getText("Recipes.boots");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getDeathReason(String reason, String pname) {
        try {
            return getText("Messages.death-messages." + reason).replaceAll("%player%",pname);
        } catch (ConfigNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getNotInOcean() {
        try {
            return getText("Messages.not-in-ocean");
        } catch (ConfigNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}