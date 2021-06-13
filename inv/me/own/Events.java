package inv.me.own;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.sk89q.worldguard.protection.flags.StringFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {

    @EventHandler
    public void onBuild(BlockPlaceEvent e) {
        HCPlayer hcp = HCPlayer.getHCP(e.getPlayer());
        hcp.buildBlock();
    }

    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        HCPlayer hcp = HCPlayer.getHCP((Player) e.getEntity());
        if (hcp.isGM()) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            if (Config.getInjuryChance((int) Math.ceil(e.getEntity().getFallDistance()))) {
                hcp.setBreakLeg(true);
            }
        } else if (e.getCause() == EntityDamageEvent.DamageCause.CONTACT) {
            Location loc = e.getEntity().getLocation();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 3; y++) {
                        if (Config.containsBleedingBlocks(e.getEntity().getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z).getType())) {
                            hcp.setBleeding(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCombat(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            HCPlayer hcp = HCPlayer.getHCP((Player) e.getDamager());
            if (hcp.isGM()) return;
            HCPlayer.getHCP((Player) e.getDamager()).combat();
            if (Config.containsBleedingItems(((Player) e.getDamager()).getItemInHand().getType())) {
                hcp.setBleeding(true);
            }
        } else if (Config.containsBleedingETypes(e.getDamager().getType()) || Config.containsBleedingProjectile(e.getDamager().getType())) {
            if (e.getEntity() instanceof Player) {
                HCPlayer hcp = HCPlayer.getHCP((Player) e.getEntity());
                if (hcp.isGM()) return;
                hcp.setBleeding(true);
            }
        }
    }

    public void removeHandItem(Player p) {
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                p.getInventory().setItem(p.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
            } else {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
        }, 1L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getFoodLevel() == 20) {
            p.setFoodLevel(19);
            Bukkit.getServer().getScheduler().runTaskLater(Main.instance, () -> {
                p.setFoodLevel(20);
            }, 1L);
        }
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        HCPlayer hcp = HCPlayer.getHCP(p);
        if (mainHand.getType() == Material.PAPER) {
            if (mainHand.getItemMeta().getDisplayName().equalsIgnoreCase(Config.getBandageName())) {
                if (hcp.isBleeding()) {
                    hcp.setBleeding(false);
                    removeHandItem(p);
                }
            } else if (mainHand.getItemMeta().getDisplayName().equalsIgnoreCase(Config.getBrokenLegName())) {
                if (hcp.isBroken()) {
                    hcp.setBreakLeg(false);
                    removeHandItem(p);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory().getType() == InventoryType.WORKBENCH) {
            if (e.getSlot() == 0 && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                e.getWhoClicked().setItemOnCursor(e.getCurrentItem());
                e.getClickedInventory().clear();
            } else {
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    for (int i = 1; i < 10; i++) {
                        e.getClickedInventory().setItem(i, e.getClickedInventory().getItem(i));
                    }
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onPrepareItem(PrepareItemCraftEvent e) {
        ItemStack[] items = e.getInventory().getMatrix();
        for (ItemStack item : items) {
            if (item == null) return;
        }
        if (items[0].getType() == Material.PACKED_ICE &&
                items[1].getType() == Material.PACKED_ICE &&
                items[2].getType() == Material.PACKED_ICE &&
                items[3].getType() == Material.PACKED_ICE &&
                items[5].getType() == Material.PACKED_ICE &&
                items[6].getType() == Material.PACKED_ICE &&
                items[7].getType() == Material.PACKED_ICE &&
                items[8].getType() == Material.PACKED_ICE &&
                (items[4].getType() == Material.LEATHER_HELMET ||
                        items[4].getType() == Material.LEATHER_CHESTPLATE ||
                        items[4].getType() == Material.LEATHER_LEGGINGS ||
                        items[4].getType() == Material.LEATHER_BOOTS)) {
            for (int i = 0; i < 9; i++) {
                if (i == 4) continue;
                if (items[i].getAmount() < 64) {
                    e.getInventory().setResult(null);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        HCPlayer hcp = HCPlayer.getHCP(e.getEntity());
        hcp.respawn();
        if (hcp.getDeathReason() != null) {
            e.setDeathMessage(null);
            Bukkit.broadcastMessage(hcp.getDeathReason());
            hcp.setDeathReason(null);
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e) {
        if (Config.getThirstFood().containsKey(e.getItem().getType())) {
            HCPlayer hcp = HCPlayer.getHCP(e.getPlayer());
            int thirstValue = Config.getThirstFood().get(e.getItem().getType());
            hcp.addThirst(thirstValue, true);
        }
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        HCPlayer.getHCP(e.getPlayer()).setDigging(false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        HCPlayer.getHCP(e.getPlayer());
    }

    public static void onBlockDigging() {
        Main.pm.addPacketListener(new PacketAdapter(Main.instance, ListenerPriority.HIGHEST, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                HCPlayer hcp = HCPlayer.getHCP(e.getPlayer());
                if (e.getPacket().getPlayerDigTypes().getValues().contains(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)) {
                    hcp.setDigging(true);
                } else if (e.getPacket().getPlayerDigTypes().getValues().contains(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK)) {
                    hcp.setDigging(false);
                }
            }
        });
        Main.pm.addPacketListener(new PacketAdapter(Main.instance, ListenerPriority.LOWEST, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                Player p = e.getPlayer();
                if (e.getPacket().getHands().getValues().contains(EnumWrappers.Hand.MAIN_HAND)) {
                    HCPlayer.getHCP(p).digBlock();
                }
            }
        });
    }
}
