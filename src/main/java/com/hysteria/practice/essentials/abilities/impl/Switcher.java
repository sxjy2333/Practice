package com.hysteria.practice.essentials.abilities.impl;

import com.hysteria.practice.HyPractice;
import com.hysteria.practice.essentials.abilities.utils.DurationFormatter;
import com.hysteria.practice.essentials.abilities.Ability;
import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.utilities.chat.CC;
import org.bukkit.Location;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class Switcher extends Ability {

    private final HyPractice plugin = HyPractice.get();

    public Switcher() {
        super("SWITCHER");
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            if (isAbility(shooter.getItemInHand())) {
                event.getEntity().setMetadata(this.getAbility(), new FixedMetadataValue(this.plugin, true));
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isAbility(event.getItem())) return;

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player shooter = event.getPlayer();
            Profile profile = Profile.get(shooter.getUniqueId());

            if (profile.getSwitcher().onCooldown(shooter)) {
                shooter.sendMessage(CC.translate("&7You are on &c&lSwitcher &7cooldown for &4" + DurationFormatter.getRemaining(profile.getSwitcher().getRemainingMillis(shooter), true, true)));
                shooter.updateInventory();
                event.setCancelled(true);
                return;
            }

            if(profile.getPartneritem().onCooldown(shooter)){
                shooter.sendMessage(CC.translate("&7You are on &c&lPartner Item &7cooldown &7for &4" + DurationFormatter.getRemaining(profile.getPartneritem().getRemainingMillis(shooter), true, true)));
                shooter.updateInventory();
                event.setCancelled(true);
                return;
            }

            profile.getSwitcher().applyCooldown(shooter, 8 * 1000);
            profile.getPartneritem().applyCooldown(shooter,  10 * 1000);

            plugin.getAbilityManager().cooldownExpired(shooter, this.getName(), this.getAbility());
            plugin.getAbilityManager().playerMessage(shooter, this.getAbility());
        }
    }

    @EventHandler
    public void checkCooldown(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Profile profile = Profile.get(player.getUniqueId());
        if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            if (!isAbility(player.getItemInHand())) {
                return;
            }
            if (isAbility(player.getItemInHand())) {
                if (this.hasCooldown(player)) {
                    player.sendMessage(CC.translate("&7You are on cooldown for &4" + DurationFormatter.getRemaining(profile.getSwitcher().getRemainingMillis(player), true)));
                    event.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile instanceof Egg && projectile.hasMetadata(this.getAbility())) {
                Player player = (Player) event.getEntity();
                Player shooter = (Player) projectile.getShooter();

                Location playerLocation = player.getLocation().clone();
                Location shooterLocation = shooter.getLocation().clone();

                player.teleport(shooterLocation);
                shooter.teleport(playerLocation);

                this.plugin.getAbilityManager().targetMessage(player, shooter, this.getAbility());
            }
            else if (projectile instanceof Snowball && projectile.hasMetadata(this.getAbility())) {
                Player player = (Player) event.getEntity();
                Player shooter = (Player) projectile.getShooter();

                Location playerLocation = player.getLocation().clone();
                Location shooterLocation = shooter.getLocation().clone();

                player.teleport(shooterLocation);
                shooter.teleport(playerLocation);

                this.plugin.getAbilityManager().targetMessage(player, shooter, this.getAbility());
            }
        }
    }
}