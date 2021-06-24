package me.ohowe12.spectatormode.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.io.ObjectInputFilter;

public class SpectatorEligibilityChecker {

    public static EligibilityStatus getStatus(Player player, boolean forced, ConfigManager configManager) {
        if (forced || player.hasPermission("smpspectator.bypass")) {
            return EligibilityStatus.CAN_GO;
        }
        if (configManager.getBoolean("prevent-falling") && player.getFallDistance() > 0) {
            return EligibilityStatus.FALLING;
        }
        if (player.getHealth() < configManager.getDouble("minimum-health")) {
            return EligibilityStatus.HEALTH;
        }
        double closestAllowed = configManager.getDouble("closest-hostile");
        if (closestAllowed != 0) {
            for (Entity entity : player.getNearbyEntities(closestAllowed, closestAllowed, closestAllowed)) {
                if (entity instanceof Monster) {
                    return EligibilityStatus.MOB;
                }
            }
        }
        if (configManager.getList("worlds-allowed").stream().map(Object::toString).noneMatch(s -> s.equalsIgnoreCase(player.getWorld().getName())) && configManager.getBoolean("enforce-worlds")) {
            return EligibilityStatus.WORLDS;
        }
        return EligibilityStatus.CAN_GO;
    }

    public enum EligibilityStatus {
        CAN_GO(null),
        FALLING("falling-message"),
        HEALTH("health-message"),
        MOB("mob-too-close-message"),
        WORLDS("world-message");

        private final String message;

        EligibilityStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
