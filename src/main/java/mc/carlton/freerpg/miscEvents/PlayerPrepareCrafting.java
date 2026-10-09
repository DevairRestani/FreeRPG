package mc.carlton.freerpg.miscEvents;

import mc.carlton.freerpg.globalVariables.ItemGroups;
import mc.carlton.freerpg.configStorage.ConfigLoad;
import mc.carlton.freerpg.playerInfo.PlayerStats;
import mc.carlton.freerpg.utilities.UtilityMethods;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Map;

public class PlayerPrepareCrafting implements Listener {

    @EventHandler
    public void craftEvent(PrepareItemCraftEvent event) {
        ItemStack[] contents = event.getInventory().getContents();
        ItemStack firstInContents = contents[0];
        if (firstInContents == null) { //No recipe result
            return;
        }
        Player p = (Player) event.getView().getPlayer();
        PlayerStats pStatClass = new PlayerStats(p);
        Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();

        if((firstInContents.getType()== Material.TNT) && (firstInContents.getAmount() == 1)) {
            ConfigLoad configLoad = new ConfigLoad();
            if (!configLoad.getAllowedSkillsMap().get("mining")) {
                return;
            }
            int moreBombsLevel = (int)pStat.get("mining").get(8);
            if (moreBombsLevel > 0) {
                firstInContents.setAmount(Math.min(6,moreBombsLevel+1));
            }
        }
        else if((firstInContents.getType()== Material.ARROW) && (firstInContents.getAmount() < 64)) {
            ConfigLoad configLoad = new ConfigLoad();
            if (!configLoad.getAllowedSkillsMap().get("archery")) {
                return;
            }
            if ((int)pStat.get("archery").get(7) > 0) {
                firstInContents.setAmount(4+(int)pStat.get("archery").get(7));
            }
        }
        else if(firstInContents.getType() == Material.TIPPED_ARROW && contents.length > 5 && contents[5] != null && contents[5].getType() == Material.POTION) {
            ConfigLoad configLoad = new ConfigLoad();
            if (!configLoad.getAllowedSkillsMap().get("archery")) {
                return;
            }
            PotionMeta potionMeta = (PotionMeta) contents[5].getItemMeta();
            ItemGroups itemGroups = new ItemGroups();
            ItemStack arrow = new ItemStack(Material.ARROW,1);
            PotionType potionType = potionMeta.getBasePotionType(); //null for potions with only custom effects
            if (potionType == null) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            //Since 1.20.5 long/strong potions are separate types (e.g. LONG_SWIFTNESS), so switch on the base type
            boolean isExtended = UtilityMethods.isExtendedPotionType(potionType);
            boolean isUpgraded = UtilityMethods.isUpgradedPotionType(potionType);
            switch (UtilityMethods.getBasePotionType(potionType)) {
                case LUCK:
                    arrow = itemGroups.getArrow("luck");
                    break;
                case LEAPING:
                    arrow = itemGroups.getArrow("leaping");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_leaping");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_leaping");
                    }
                    break;
                case REGENERATION:
                    arrow = itemGroups.getArrow("regeneration");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_regeneration");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_regeneration");
                    }
                    break;
                case SWIFTNESS:
                    arrow = itemGroups.getArrow("swiftness");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_swiftness");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_swiftness");
                    }
                    break;
                case POISON:
                    arrow = itemGroups.getArrow("poison");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_poison");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_poison");
                    }
                    break;
                case SLOWNESS:
                    arrow = itemGroups.getArrow("slowness");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_slowness");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_slowness");
                    }
                    break;
                case STRENGTH:
                    arrow = itemGroups.getArrow("strength");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_strength");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_strength");
                    }
                    break;
                case WEAKNESS:
                    arrow = itemGroups.getArrow("weakness");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_weakness");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_weakness");
                    }
                    break;
                case HEALING:
                    arrow = itemGroups.getArrow("healing");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_healing");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_healing");
                    }
                    break;
                case INVISIBILITY:
                    arrow = itemGroups.getArrow("invisibility");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_invisibility");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_invisibility");
                    }
                    break;
                case NIGHT_VISION:
                    arrow = itemGroups.getArrow("night_vision");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_night_vision");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_night_vision");
                    }
                    break;
                case SLOW_FALLING:
                    arrow = itemGroups.getArrow("slow_falling");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_slow_falling");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_slow_falling");
                    }
                    break;
                case TURTLE_MASTER:
                    arrow = itemGroups.getArrow("turtle_master");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_turtle_master");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_turtle_master");
                    }
                    break;
                case HARMING:
                    arrow = itemGroups.getArrow("harming");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_harming");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_harming");
                    }
                    break;
                case FIRE_RESISTANCE:
                    arrow = itemGroups.getArrow("fire_resistance");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_fire_resistance");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_fire_resistance");
                    }
                    break;
                case WATER_BREATHING:
                    arrow = itemGroups.getArrow("water_breathing");
                    if (isExtended) {
                        arrow = itemGroups.getArrow("long_water_breathing");
                    }
                    if (isUpgraded) {
                        arrow = itemGroups.getArrow("strong_water_breathing");
                    }
                    break;
                case WIND_CHARGED:
                    arrow = itemGroups.getArrow("wind_charged");
                    break;
                case WEAVING:
                    arrow = itemGroups.getArrow("weaving");
                    break;
                case OOZING:
                    arrow = itemGroups.getArrow("oozing");
                    break;
                case INFESTED:
                    arrow = itemGroups.getArrow("infested");
                    break;
                default:
                    arrow = new ItemStack(Material.AIR);
                    break;
            }
            event.getInventory().setResult(arrow);
        }


    }
}
