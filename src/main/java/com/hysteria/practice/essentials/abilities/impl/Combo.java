package com.hysteria.practice.essentials.abilities.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hysteria.practice.essentials.abilities.utils.DurationFormatter;
import com.hysteria.practice.essentials.abilities.Ability;
import com.hysteria.practice.HyPractice;
import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.utilities.PlayerUtil;
import com.hysteria.practice.utilities.TaskUtil;
import com.hysteria.practice.utilities.chat.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Combo extends Ability {

    private final HyPractice plugin = HyPractice.get();
    private final Set<UUID> COMBO = Sets.newHashSet();
    private final Map<UUID, Integer> HITS = Maps.newHashMap();

    public Combo() {
        super("COMBO");
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (!isAbility(event.getItem())) return;

        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            Profile profile = Profile.get(player.getUniqueId());
            
            if (profile.getCombo().onCooldown(player)) {
                player.sendMessage(CC.translate("&7You are on &c&lCombo &7cooldown for &4" + DurationFormatter.getRemaining(profile.getCombo().getRemainingMillis(player), true, true)));
                player.updateInventory();
                return;
            }

            if(profile.getPartneritem().onCooldown(player)){
                player.sendMessage(CC.translate("&7You are on &c&lPartner Item &7cooldown &7for &4" + DurationFormatter.getRemaining(profile.getPartneritem().getRemainingMillis(player), true, true)));
                player.updateInventory();
                return;
            }

            PlayerUtil.decrement(player);

            profile.getCombo().applyCooldown(player, 60 * 1000);
            profile.getPartneritem().applyCooldown(player,  10 * 1000);

            this.giveComboEffects(player);

            COMBO.add(player.getUniqueId());
            HITS.put(player.getUniqueId(), 0);

            plugin.getAbilityManager().cooldownExpired(player, this.getName(), this.getAbility());
            plugin.getAbilityManager().playerMessage(player, this.getAbility());
        }
    }

    @EventHandler
    private void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();

            if (COMBO.contains(damager.getUniqueId())) {
                if (HITS.containsKey(damager.getUniqueId())) {
                    HITS.put(damager.getUniqueId(), HITS.get(damager.getUniqueId()) + 1);
                }
            }
        }
    }

    private void giveComboEffects(Player player) {
        TaskUtil.runLater(() -> {
            int hits = HITS.get(player.getUniqueId());

            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * hits, 1));
            player.playSound(player.getLocation(), Sound.ZOMBIE_INFECT, 1F, 1F);
            CC.message(player, "&7You've received Strength II for &4" + hits + " &7seconds.");

            HITS.remove(player.getUniqueId());
            COMBO.remove(player.getUniqueId());
        }, 20 * 6);
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
                    player.sendMessage(CC.translate("&7You are on cooldown for &4" + DurationFormatter.getRemaining(profile.getCombo().getRemainingMillis(player), true)));
                    event.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }
}