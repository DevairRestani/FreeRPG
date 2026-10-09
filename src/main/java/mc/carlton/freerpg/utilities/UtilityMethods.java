package mc.carlton.freerpg.utilities;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilityMethods {
    //Pre-1.20.5 PotionType names (and effect names commonly used in configs) mapped to their current PotionType names
    private static final Map<String, String> LEGACY_POTION_TYPE_NAMES = new HashMap<>();
    //Pre-1.20.5 PotionEffectType names mapped to their current registry keys
    private static final Map<String, String> LEGACY_POTION_EFFECT_TYPE_NAMES = new HashMap<>();
    private static final String LONG_POTION_PREFIX = "LONG_";
    private static final String STRONG_POTION_PREFIX = "STRONG_";
    static {
        LEGACY_POTION_TYPE_NAMES.put("JUMP", "LEAPING");
        LEGACY_POTION_TYPE_NAMES.put("JUMP_BOOST", "LEAPING");
        LEGACY_POTION_TYPE_NAMES.put("SPEED", "SWIFTNESS");
        LEGACY_POTION_TYPE_NAMES.put("SLOW", "SLOWNESS");
        LEGACY_POTION_TYPE_NAMES.put("REGEN", "REGENERATION");
        LEGACY_POTION_TYPE_NAMES.put("INSTANT_HEAL", "HEALING");
        LEGACY_POTION_TYPE_NAMES.put("INSTANT_HEALTH", "HEALING");
        LEGACY_POTION_TYPE_NAMES.put("HEAL", "HEALING");
        LEGACY_POTION_TYPE_NAMES.put("INSTANT_DAMAGE", "HARMING");
        LEGACY_POTION_TYPE_NAMES.put("HARM", "HARMING");
        LEGACY_POTION_TYPE_NAMES.put("INCREASE_DAMAGE", "STRENGTH");

        LEGACY_POTION_EFFECT_TYPE_NAMES.put("SLOW", "slowness");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("FAST_DIGGING", "haste");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("SLOW_DIGGING", "mining_fatigue");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("INCREASE_DAMAGE", "strength");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("HEAL", "instant_health");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("HARM", "instant_damage");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("JUMP", "jump_boost");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("CONFUSION", "nausea");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("DAMAGE_RESISTANCE", "resistance");
        LEGACY_POTION_EFFECT_TYPE_NAMES.put("REGEN", "regeneration");
    }

    public static String capitalizeString(String string) {
        if (string.length() == 0) {
            return string;
        }
        else if (string.length() == 1) {
            return string.toUpperCase();
        }
        else {
            return string.substring(0,1).toUpperCase() + string.substring(1);
        }
    }
    public static boolean stringCollectionContainsIgnoreCase(Collection<String> colection, String inputString) {
        for (String string : colection) {
            if (inputString.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }
    public static String convertStringToListCasing(List<String> list,String inputString) {
        for (String string : list) {
            if (inputString.equalsIgnoreCase(string)) {
                return string;
            }
        }
        return inputString;
    }
    public static String intToRankingString(int rank) {
        String suffix = "th";
        int lastTwoDigits = rank % 100;
        int lastDigit = rank % 10;
        if (!(lastTwoDigits >= 10 && lastTwoDigits <= 19) ) {
            if (lastDigit == 1) {
                suffix = "st";
            } else if (lastDigit == 2) {
                suffix = "nd";
            } else if (lastDigit == 3) {
                suffix = "rd";
            }
        }
        return (String.valueOf(rank) + suffix);
    }
    public static <E> boolean collectionOnlyContainsOneClass(Collection<? extends E> collection,Class<?> tClass ) {
        for (E item : collection) {
            if (!tClass.isInstance(item)) {
                return false;
            }
        }
        return true;
    }
    public static Color getColorFromString(String colorString) {
        colorString = colorString.substring(1,colorString.length()-1);
        List<String> RGB = Arrays.asList(colorString.trim().split(","));
        int red = 0;
        int green = 0;
        int blue = 0;
        if (RGB.size() == 3) {
            red = Integer.parseInt(RGB.get(0));
            green = Integer.parseInt(RGB.get(1));
            blue = Integer.parseInt(RGB.get(2));
        }
        return Color.fromRGB(red,green,blue);
    }
    public static String camelCaseToSpacedString(String camelCaseString) {
        return camelCaseString.replaceAll("([^_])([A-Z])", "$1 $2");
    }
    public static EntityType matchEntityType(String entityTypeString) {
        String convertedString = entityTypeString.replace(" ", "_").toUpperCase();
        for (EntityType entityType : EntityType.values()) {
            if (entityType.toString().equalsIgnoreCase(convertedString)) {
                return entityType;
            }
        }
        return null;
    }
    /**
     * Matches a PotionType by name. Pre-1.20.5 names (e.g. JUMP, SPEED, REGEN, INSTANT_HEAL, INSTANT_DAMAGE) are
     * converted to their current names so old configs keep working.
     *
     * @return the matching PotionType, or null if there is none (e.g. the removed UNCRAFTABLE type)
     */
    public static PotionType matchPotionType(String potionTypeString) {
        if (potionTypeString == null) {
            return null;
        }
        String convertedString = potionTypeString.trim().replace(" ", "_").toUpperCase();
        String prefix = "";
        if (convertedString.startsWith(LONG_POTION_PREFIX)) {
            prefix = LONG_POTION_PREFIX;
        } else if (convertedString.startsWith(STRONG_POTION_PREFIX)) {
            prefix = STRONG_POTION_PREFIX;
        }
        String baseName = convertedString.substring(prefix.length());
        if (LEGACY_POTION_TYPE_NAMES.containsKey(baseName)) {
            convertedString = prefix + LEGACY_POTION_TYPE_NAMES.get(baseName);
        }
        for (PotionType potionType : PotionType.values()) {
            if (potionType.toString().equalsIgnoreCase(convertedString)) {
                return potionType;
            }
        }
        return null;
    }

    /**
     * Matches a PotionEffectType by its key (e.g. "speed", "minecraft:mining_fatigue") or by its pre-1.20.5 name
     * (e.g. "SLOW_DIGGING", "DAMAGE_RESISTANCE").
     *
     * @return the matching PotionEffectType, or null if there is none
     */
    public static PotionEffectType matchPotionEffectType(String potionEffectTypeString) {
        if (potionEffectTypeString == null) {
            return null;
        }
        String convertedString = potionEffectTypeString.trim().replace(" ", "_").toUpperCase();
        if (LEGACY_POTION_EFFECT_TYPE_NAMES.containsKey(convertedString)) {
            convertedString = LEGACY_POTION_EFFECT_TYPE_NAMES.get(convertedString);
        }
        NamespacedKey key;
        try {
            key = NamespacedKey.fromString(convertedString.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (key == null) {
            return null;
        }
        return Registry.EFFECT.get(key);
    }

    /**
     * Since 1.20.5 potion types no longer have "extended" and "upgraded" flags. Instead, there are separate types
     * (e.g. SWIFTNESS, LONG_SWIFTNESS, STRONG_SWIFTNESS). This returns the type matching the old flags, or the base
     * type if no such variant exists.
     */
    public static PotionType getPotionTypeVariant(PotionType potionType, boolean isExtended, boolean isUpgraded) {
        if (potionType == null) {
            return null;
        }
        if (!isExtended && !isUpgraded) { //Also keeps types that were given as e.g. LONG_SWIFTNESS directly
            return potionType;
        }
        PotionType basePotionType = getBasePotionType(potionType);
        String prefix = (isUpgraded) ? STRONG_POTION_PREFIX : LONG_POTION_PREFIX;
        for (PotionType variant : PotionType.values()) {
            if (variant.toString().equals(prefix + basePotionType.toString())) {
                return variant;
            }
        }
        return basePotionType;
    }

    /**
     * @return the base (not extended, not upgraded) variant of a potion type, e.g. LONG_SWIFTNESS -> SWIFTNESS
     */
    public static PotionType getBasePotionType(PotionType potionType) {
        if (potionType == null) {
            return null;
        }
        String name = potionType.toString();
        String baseName = name;
        if (name.startsWith(LONG_POTION_PREFIX)) {
            baseName = name.substring(LONG_POTION_PREFIX.length());
        } else if (name.startsWith(STRONG_POTION_PREFIX)) {
            baseName = name.substring(STRONG_POTION_PREFIX.length());
        }
        if (baseName.equals(name)) {
            return potionType;
        }
        for (PotionType basePotionType : PotionType.values()) {
            if (basePotionType.toString().equals(baseName)) {
                return basePotionType;
            }
        }
        return potionType;
    }

    public static boolean isExtendedPotionType(PotionType potionType) {
        return potionType != null && potionType.toString().startsWith(LONG_POTION_PREFIX);
    }

    public static boolean isUpgradedPotionType(PotionType potionType) {
        return potionType != null && potionType.toString().startsWith(STRONG_POTION_PREFIX);
    }
    public static EntityDamageEvent.DamageCause matchDamageCause(String damageCauseString) {
        String convertedString = damageCauseString.replace(" ", "_").toUpperCase();
        for (EntityDamageEvent.DamageCause damageCause : EntityDamageEvent.DamageCause.values()) {
            if (damageCause.toString().equalsIgnoreCase(convertedString)) {
                return damageCause;
            }
        }
        return null;
    }
    public static boolean stringContainsIgnoreCase(String string, String containedString) {
        if (string.toLowerCase().contains(containedString.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean stringContainsIgnoreCase(String string, Collection<String> containedStringOptions) {
        for (String containedString : containedStringOptions) {
            if (stringContainsIgnoreCase(string,containedString)){
                return true;
            }
        }
        return false;
    }
    public static String toAlphabetic(int i) {
        if( i<0 ) {
            return "-"+toAlphabetic(-i-1);
        }

        int quot = i/26;
        int rem = i%26;
        char letter = (char)((int)'A' + rem);
        if( quot == 0 ) {
            return ""+letter;
        } else {
            return toAlphabetic(quot-1) + letter;
        }
    }
}
