package mc.carlton.freerpg.brewingEvents;

import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.gameTools.BrewingStandUserTracker;
import mc.carlton.freerpg.perksAndAbilities.Alchemy;
import mc.carlton.freerpg.playerInfo.ChangeStats;
import mc.carlton.freerpg.configStorage.ConfigLoad;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

public class FinishedBrewing implements Listener {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    @EventHandler(priority = EventPriority.HIGH)
    void onBrewComplete(BrewEvent e){
        if (e.isCancelled()) {
            return;
        }
        ConfigLoad configLoad = new ConfigLoad();
        if (!configLoad.getAllowedSkillsMap().get("alchemy")) {
            return;
        }
        BrewerInventory inventory = e.getContents();
        if (inventory.getItem(3) == null) {
            return;
        }
        ItemStack ingredient = inventory.getItem(3).clone();
        ItemStack[] slotItems = {inventory.getItem(0),inventory.getItem(1),inventory.getItem(2)};
        BrewingStandUserTracker brewTracker = new BrewingStandUserTracker();
        Player p = brewTracker.getPlayer(inventory.getHolder());
        if (p != null) {
            Alchemy alchemyClass = new Alchemy(p);
            alchemyClass.giveBrewingEXP(ingredient,slotItems);
        }
        if (ingredient.getType() == Material.DRAGON_BREATH || ingredient.getType() == Material.GUNPOWDER) {
            /*
             * Custom potions are converted by changing the results of this event. This used to be done one tick later
             * by writing into a BrewingStand snapshot and calling update(), which put the converted potion back into
             * the stand even if the brewed potion had already been taken out (by a player or a hopper), duplicating it.
             */
            List<ItemStack> results = e.getResults();
            for (int i=0; i<3 && i<results.size();i++) {
                ItemStack slot_i = slotItems[i];
                if (slot_i == null || slot_i.getType() == Material.AIR) {
                    continue;
                }
                if (!slot_i.getEnchantments().containsKey(Enchantment.LOYALTY) && !slot_i.getEnchantments().containsKey(Enchantment.UNBREAKING)) {
                    continue;
                }
                if (!(slot_i.getItemMeta() instanceof PotionMeta)) {
                    continue;
                }
                PotionMeta slotMeta = (PotionMeta) slot_i.getItemMeta();
                String normalName = slotMeta.hasDisplayName() ? ChatColor.stripColor(slotMeta.getDisplayName()) : null;
                Material newType = null;
                if (ingredient.getType() == Material.GUNPOWDER) {
                    if (slot_i.getType() == Material.POTION) {
                        newType = Material.SPLASH_POTION;
                        if (normalName != null) {
                            slotMeta.setDisplayName(ChatColor.RESET + "Splash " + normalName);
                        }
                    }
                }
                else {
                    if (slot_i.getType() == Material.SPLASH_POTION) {
                        newType = Material.LINGERING_POTION;
                        if (normalName != null) {
                            if (normalName.startsWith("Splash ")) {
                                normalName = normalName.substring(7);
                            }
                            slotMeta.setDisplayName(ChatColor.RESET + "Lingering " + normalName);
                        }
                        if (slotMeta.hasCustomEffects()) {
                            PotionEffect oldEffect = slotMeta.getCustomEffects().get(0);
                            int newLength = (int) Math.round(oldEffect.getDuration()*0.25);
                            slotMeta.addCustomEffect(new PotionEffect(oldEffect.getType(),newLength,oldEffect.getAmplifier()),true);
                        }
                    }
                }
                if (newType == null) {
                    continue;
                }
                ItemStack result = slot_i.withType(newType);
                result.setItemMeta(slotMeta);
                results.set(i, result);
                if (p != null) {
                    Map<String,Integer> expMap = configLoad.getExpMapForSkill("alchemy");
                    ChangeStats increaseStats = new ChangeStats(p);
                    increaseStats.changeEXP("alchemy", expMap.get("brewSplashPotion"));
                }
            }
        }

    }
}
