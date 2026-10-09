package mc.carlton.freerpg.perksAndAbilities;

import mc.carlton.freerpg.configStorage.ConfigLoad;
import mc.carlton.freerpg.utilities.UtilityMethods;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BrewingStand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Alchemy extends Skill{
    private String skillName = "alchemy";

    private static Map<BrewerInventory, Integer> counter = new HashMap<>();
    private static Map<BrewerInventory, Integer> failSafe = new HashMap<>();
    private boolean runMethods;


    public Alchemy(Player p) {
        super(p);
        ConfigLoad configLoad = new ConfigLoad();
        this.runMethods = configLoad.getAllowedSkillsMap().get(skillName);
        expMap = configLoad.getExpMapForSkill("alchemy");
    }

    public void startBrewing(BrewerInventory inventory, ItemStack output, ItemStack input) {
        if (!runMethods) {
            return;
        }
        if (counter.containsKey(inventory)) {
            return;
        }
        //int speedBrewingLevel = (int) pStat.get(skillName).get(7);
        int time = 400;
        BrewingStand stand = inventory.getHolder();
        if (stand == null) {
            return;
        }
        if (stand.getFuelLevel() == 0) { //Checked before registering the stand, otherwise it would be locked out of custom brewing forever
            return;
        }
        World world = stand.getWorld();
        counter.put(inventory, time);
        failSafe.put(inventory, time + 2);
        stand.setBrewingTime(time);
        stand.update();
        new BukkitRunnable() {
            @Override
            public void run() {
                failSafe.put(inventory, failSafe.get(inventory) - 1);
                if (failSafe.get(inventory) < 0) {
                    stopBrewing(inventory);
                    cancel();
                    return;
                }
                int timer = counter.get(inventory);

                if (!isStillBrewing(inventory, input)) {
                    stopBrewing(inventory);
                    cancel();
                    return;
                }

                if (timer == 0) //Finished brewing item changes
                {
                    /*
                     * Only the live inventory is modified here. Writing items through an old BrewingStand snapshot
                     * (getSnapshotInventory() + update()) puts stale stacks back into the stand, which duplicated
                     * anything taken out of it while brewing.
                     */
                    ItemStack ingredient = inventory.getIngredient();
                    ingredient.setAmount(ingredient.getAmount() - 1);
                    inventory.setIngredient(ingredient);
                    for (int i = 0; i < 3; i++) {
                        ItemStack slotItem = inventory.getItem(i);
                        if (slotItem == null || slotItem.getType() == Material.AIR) {
                            continue;
                        }
                        // output is a shared template (see ItemGroups), so never modify it directly
                        ItemStack result = output.clone();
                        PotionMeta resultMeta = (PotionMeta) result.getItemMeta();
                        String normalName = resultMeta.getDisplayName();
                        if (slotItem.getType() == Material.SPLASH_POTION) {
                            result = result.withType(Material.SPLASH_POTION);
                            resultMeta.setDisplayName(ChatColor.RESET + ChatColor.WHITE.toString() + "Splash " + normalName);
                        } else if (slotItem.getType() == Material.LINGERING_POTION) {
                            result = result.withType(Material.LINGERING_POTION);
                            resultMeta.setDisplayName(ChatColor.RESET + ChatColor.WHITE.toString() + "Lingering " + normalName);
                            if (resultMeta.hasCustomEffects()) {
                                PotionEffect oldEffect = resultMeta.getCustomEffects().get(0);
                                int newLength = (int) Math.round(oldEffect.getDuration() / 4.0);
                                resultMeta.addCustomEffect(new PotionEffect(oldEffect.getType(), newLength, oldEffect.getAmplifier()), true);
                            }
                        } else {
                            resultMeta.setDisplayName(ChatColor.RESET + ChatColor.WHITE.toString() + normalName);
                        }
                        result.setItemMeta(resultMeta);
                        inventory.setItem(i, result);
                        increaseStats.changeEXP(skillName,expMap.get("brewCustomPotion"));
                    }

                    BrewingStand liveStand = inventory.getHolder(); //Fresh snapshot, so update() can't restore old items
                    liveStand.setFuelLevel(Math.max(liveStand.getFuelLevel() - 1, 0));
                    liveStand.setBrewingTime(0);
                    liveStand.update();
                    world.playSound(liveStand.getLocation(), org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
                    stopBrewing(inventory);
                    cancel();
                    return;
                }

                //Update counter progress
                counter.put(inventory, timer - 1);
                updateBrewingTime(inventory, timer - 1);
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    public void upgradeBrewing(BrewerInventory inventory, ItemStack input, boolean[] slotsToCheck) {
        if (!runMethods) {
            return;
        }
        if (input.getType() != Material.REDSTONE && input.getType() != Material.GLOWSTONE_DUST) {
            return;
        }
        double durationMultiplier = 1;
        int potency = 0;
        if (input.getType() == Material.REDSTONE) {
            durationMultiplier = 8.0 / 3.0;
        } else if (input.getType() == Material.GLOWSTONE_DUST) {
            potency = 1;
            durationMultiplier = 0.5;
        }
        if (counter.containsKey(inventory)) {
            return;
        }
        //int speedBrewingLevel = (int) pStat.get(skillName).get(7);
        int time = 400; //(int) Math.round((1 - speedBrewingLevel * 0.15) * 400);
        BrewingStand stand = inventory.getHolder();
        if (stand == null) {
            return;
        }
        if (stand.getFuelLevel() == 0) { //Checked before registering the stand, otherwise it would be locked out of custom brewing forever
            return;
        }
        World world = stand.getWorld();
        counter.put(inventory, time);
        failSafe.put(inventory, time + 2);
        stand.setBrewingTime(time);
        stand.update();
        double finalDurationMultiplier = durationMultiplier;
        int finalPotency = potency;
        new BukkitRunnable() {
            @Override
            public void run() {
                failSafe.put(inventory, failSafe.get(inventory) - 1);
                if (failSafe.get(inventory) < 0) {
                    stopBrewing(inventory);
                    cancel();
                    return;
                }
                int timer = counter.get(inventory);

                if (!isStillBrewing(inventory, input)) {
                    stopBrewing(inventory);
                    cancel();
                    return;
                }

                if (timer == 0) //Finished brewing item changes
                {
                    //Only the live inventory is modified here (see startBrewing)
                    ItemStack ingredient = inventory.getIngredient();
                    ingredient.setAmount(ingredient.getAmount() - 1);
                    inventory.setIngredient(ingredient);
                    for (int i = 0; i < 3; i++) {
                        if (!slotsToCheck[i]) {
                            continue;
                        }
                        ItemStack potion = inventory.getItem(i);
                        if (potion == null || potion.getType() == Material.AIR) {
                            continue;
                        }
                        if (!(potion.getItemMeta() instanceof PotionMeta)) {
                            continue;
                        }
                        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
                        //The potion may have been swapped while brewing, only upgrade custom potions that weren't upgraded yet
                        if (!potionMeta.hasEnchant(Enchantment.LOYALTY) || !potionMeta.hasCustomEffects()) {
                            continue;
                        }
                        PotionEffectType effect = potionMeta.getCustomEffects().get(0).getType();
                        int newLength = (int) Math.round(potionMeta.getCustomEffects().get(0).getDuration() * finalDurationMultiplier);
                        potionMeta.addCustomEffect(new PotionEffect(effect, newLength, finalPotency), true);
                        if (finalPotency == 1) { //Glowstone
                            potionMeta.setDisplayName(potionMeta.getDisplayName() + " II");
                        }
                        potionMeta.removeEnchant(Enchantment.LOYALTY);
                        potionMeta.addEnchant(Enchantment.UNBREAKING,1,true);
                        potion.setItemMeta(potionMeta);
                        inventory.setItem(i, potion);
                        increaseStats.changeEXP(skillName,expMap.get("upgradeCustomPotion"));
                    }

                    BrewingStand liveStand = inventory.getHolder(); //Fresh snapshot, so update() can't restore old items
                    liveStand.setFuelLevel(Math.max(liveStand.getFuelLevel() - 1, 0));
                    liveStand.setBrewingTime(0);
                    liveStand.update();
                    world.playSound(liveStand.getLocation(), org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
                    stopBrewing(inventory);
                    cancel();
                    return;
                }

                //Update counter progress
                counter.put(inventory, timer - 1);
                updateBrewingTime(inventory, timer - 1);
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private static void stopBrewing(BrewerInventory inventory) {
        counter.remove(inventory);
        failSafe.remove(inventory);
    }

    /**
     * @return false if the custom brew should be aborted (the stand is gone, the ingredient was removed/changed, or all bottles were taken out)
     */
    private static boolean isStillBrewing(BrewerInventory inventory, ItemStack input) {
        if (inventory.getHolder() == null) {
            return false;
        }
        // ingredient removed checks
        ItemStack ingredient = inventory.getIngredient();
        if (ingredient == null || ingredient.getType() != input.getType()) {
            return false;
        }
        // water bottles removed check
        for (int i = 0; i < 3; i++) {
            if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the brewing progress arrow. A fresh BlockState is used every time: its items equal the live items,
     * so update() only changes the brewing time (an old snapshot would overwrite the stand's items with stale copies).
     */
    private static void updateBrewingTime(BrewerInventory inventory, int brewingTime) {
        BrewingStand liveStand = inventory.getHolder();
        if (liveStand == null) {
            return;
        }
        liveStand.setBrewingTime(brewingTime);
        liveStand.update();
    }

    public boolean comparePotionEffects(ItemStack p1, ItemStack p2) {
        if (!runMethods) {
            return false;
        }
        if (p1 == null || p2 == null) {
            return false;
        }
        if (p1.getType() == Material.AIR || p2.getType() == Material.AIR) {
            return false;
        }

        if (!(p1.getItemMeta() instanceof PotionMeta) || !(p2.getItemMeta() instanceof PotionMeta)) {
            return false;
        }
        PotionType p1Type = ((PotionMeta) p1.getItemMeta()).getBasePotionType();
        PotionType p2Type = ((PotionMeta) p2.getItemMeta()).getBasePotionType();
        if (p1Type == p2Type) {
            return true;
        }
        return false;
    }


    public void drinkPotion(ItemStack potion) {
        if (!runMethods) {
            return;
        }
        Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
        int lengthBoostLevel = (int) pStat.get(skillName).get(4);
        double durationMultiplier = 1.0 + 0.001 * lengthBoostLevel;
        int potionMasterLevel = (int) Math.min((int) pStat.get(skillName).get(13), 1);
        if ((int) pStat.get("global").get(15) != 1) {
            potionMasterLevel = 0;
        }
        if (potion.getType() != Material.POTION) {
            return;
        }
        if (potion.getItemMeta() instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
            PotionType potionType = potionMeta.getBasePotionType(); //null for potions that only have custom effects
            PotionType[] noEXPPots0 = {PotionType.MUNDANE,PotionType.WATER,PotionType.AWKWARD,PotionType.THICK};
            List<PotionType> noEXPPots = Arrays.asList(noEXPPots0);
            if (!noEXPPots.contains(potionType)) {
                if (UtilityMethods.isUpgradedPotionType(potionType)) {
                    increaseStats.changeEXP(skillName, expMap.get("drinkUpgradedPotion"));
                }
                else if (UtilityMethods.isExtendedPotionType(potionType)) {
                    increaseStats.changeEXP(skillName, expMap.get("drinkExtendedPotion"));
                }
                else {
                    increaseStats.changeEXP(skillName, expMap.get("drinkPotion"));
                }
            }


            if (potionMeta.hasCustomEffects()) {
                increaseStats.changeEXP(skillName, expMap.get("drinkCustomPotion"));
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    p.addPotionEffect(new PotionEffect(effect.getType(), (int) Math.round(effect.getDuration() * durationMultiplier), effect.getAmplifier() + potionMasterLevel));
                }
            } else if (potionType != null) {
                PotionEffect pEffect = potionToEffect(potionType);
                if (!pEffect.getType().equals(PotionEffectType.BAD_OMEN)) {
                    p.addPotionEffect(pEffect);
                }

            }
        }
    }

    public PotionEffect potionToEffect(PotionType potionType) {
        PotionEffect pEffect = new PotionEffect(PotionEffectType.BAD_OMEN, 1, 1);
        if (potionType == null) {
            return pEffect;
        }
        Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
        int lengthBoostLevel = (int) pStat.get(skillName).get(4);
        double durationMultiplier = 1.0 + 0.001 * lengthBoostLevel;
        int potionMasterLevel = (int) Math.min((int) pStat.get(skillName).get(13), 1);
        if ((int) pStat.get("global").get(15) != 1) { //Potion Master toggled off
            potionMasterLevel = 0;
        }
        //Since 1.20.5 long/strong potions are separate types (e.g. LONG_SWIFTNESS), so switch on the base type
        boolean isExtended = UtilityMethods.isExtendedPotionType(potionType);
        boolean isUpgraded = UtilityMethods.isUpgradedPotionType(potionType);
        switch (UtilityMethods.getBasePotionType(potionType)) {
            case WEAKNESS:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.WEAKNESS, (int) Math.round(20 * 60 * 4 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.WEAKNESS, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.WEAKNESS, (int) Math.round(20 * 90 * durationMultiplier), potionMasterLevel);
                }
                break;
            case POISON:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.POISON, (int) Math.round(20 * 90 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.POISON, (int) Math.round(20 * 21 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.POISON, (int) Math.round(20 * 45 * durationMultiplier), potionMasterLevel);
                }
                break;
            case LEAPING:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case SWIFTNESS:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.SPEED, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.SPEED, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.SPEED, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case WATER_BREATHING:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.WATER_BREATHING, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.WATER_BREATHING, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.WATER_BREATHING, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case FIRE_RESISTANCE:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case HARMING:
                if (potionMasterLevel > 0) { //damages an additional 3(?) hearts
                    pEffect = new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0);
                }
                break;
            case SLOW_FALLING:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, (int) Math.round(20 * 4 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, (int) Math.round(20 * 90 * durationMultiplier), potionMasterLevel);
                }
                break;
            case NIGHT_VISION:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case INVISIBILITY:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.INVISIBILITY, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.INVISIBILITY, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.INVISIBILITY, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case HEALING:
                if (potionMasterLevel > 0) { //heals an additional 2 hearts
                    pEffect = new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0);
                }
                break;
            case STRENGTH:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.STRENGTH, (int) Math.round(20 * 8 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.STRENGTH, (int) Math.round(20 * 90 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.STRENGTH, (int) Math.round(20 * 180 * durationMultiplier), potionMasterLevel);
                }
                break;
            case SLOWNESS:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.SLOWNESS, (int) Math.round(20 * 4 * 60 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.SLOWNESS, (int) Math.round(20 * 20 * durationMultiplier), 3 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.SLOWNESS, (int) Math.round(20 * 90 * durationMultiplier), potionMasterLevel);
                }
                break;
            case REGENERATION:
                if (isExtended) {
                    pEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) Math.round(20 * 90 * durationMultiplier), potionMasterLevel);
                } else if (isUpgraded) {
                    pEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) Math.round(20 * 22 * durationMultiplier), 1 + potionMasterLevel);
                } else {
                    pEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) Math.round(20 * 45 * durationMultiplier), potionMasterLevel);
                }
                break;
            default:
                break;
        }
        return pEffect;
    }

    public void giveBrewingEXP(ItemStack ingredient, ItemStack[] slots) {
        if (!runMethods) {
            return;
        }
        int brewedPotions = 0;
        for (int i = 0; i < 3; i++) {
            if (slots[i] != null) {
                if (slots[i].getType() != Material.AIR) {
                    brewedPotions += 1;
                }
            }
        }
        int expToGive = 0;
        switch (ingredient.getType()) {
            case SUGAR:
                expToGive = expMap.get("brewSpeedPotion");
                break;
            case RABBIT_FOOT:
                expToGive = expMap.get("brewJumpPotion");
                break;
            case BLAZE_POWDER:
                expToGive = expMap.get("brewStrengthPotion");
                break;
            case GLISTERING_MELON_SLICE:
                expToGive = expMap.get("brewHealingPotion");
                break;
            case SPIDER_EYE:
                expToGive = expMap.get("brewPoisonPotion");
                break;
            case GHAST_TEAR:
                expToGive = expMap.get("brewRegenerationPotion");
                break;
            case MAGMA_CREAM:
                expToGive = expMap.get("brewFireResistancePotion");
                break;
            case PUFFERFISH:
                expToGive = expMap.get("brewWaterBreathingPotion");
                break;
            case GOLDEN_CARROT:
                expToGive = expMap.get("brewNightVisionPotion");
                break;
            case TURTLE_HELMET:
                expToGive = expMap.get("brewPotionOfTurtleMaster");
                break;
            case PHANTOM_MEMBRANE:
                expToGive = expMap.get("brewPotionOfSlowFalling");
                break;
            case FERMENTED_SPIDER_EYE:
                expToGive = expMap.get("brewPotionOfWeakness");
                break;
            case NETHER_WART:
                expToGive = expMap.get("brewAwkwardPotion");
                break;
            case GUNPOWDER:
                expToGive = expMap.get("brewSplashPotion");
                break;
            case DRAGON_BREATH:
                expToGive = expMap.get("brewLingeringPotion");
                break;
            case GLOWSTONE_DUST:
                expToGive = expMap.get("upgradePotion");
                break;
            case REDSTONE:
                expToGive = expMap.get("extendPotion");
                break;
            default:
                expToGive = expMap.get("brewAnythingElse");
                break;

        }
        increaseStats.changeEXP(skillName,expToGive*brewedPotions);


    }
}
