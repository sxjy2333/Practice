package com.hysteria.practice.match.listeners.impl;

import com.hysteria.practice.HyPractice;
import com.hysteria.practice.Locale;
import com.hysteria.practice.match.MatchState;
import com.hysteria.practice.match.participant.MatchGamePlayer;
import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.player.profile.ProfileState;
import com.hysteria.practice.utilities.LocationUtil;
import com.hysteria.practice.utilities.MessageFormat;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import com.hysteria.practice.match.impl.BasicTeamRoundMatch;
import com.hysteria.practice.player.profile.participant.alone.GameParticipant;
import com.hysteria.practice.utilities.*;
import com.hysteria.practice.utilities.chat.CC;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MatchSpecialListener implements Listener {

    @EventHandler
    public void onPlayerInteractSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profileSoup = Profile.get(player.getUniqueId());

        if (profileSoup.getState() == ProfileState.FIGHTING && profileSoup.getMatch().getKit().getGameRules().isSoup()) {
            if (event.getItem() != null && event.getItem().getType() == Material.MUSHROOM_SOUP) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    Player p = event.getPlayer();
                    Damageable d = event.getPlayer();
                    if (d.getHealth() < d.getMaxHealth() - 7) {
                        d.setHealth(d.getHealth() + 7);
                        p.setItemInHand(new ItemStack(Material.BOWL));
                    } else if (d.getHealth() < d.getMaxHealth()) {
                        d.setHealth(d.getMaxHealth());
                        p.setItemInHand(new ItemStack(Material.BOWL));
                    } else if (p.getFoodLevel() < 13) {
                        p.setFoodLevel(p.getFoodLevel() + 7);
                        p.setItemInHand(new ItemStack(Material.BOWL));
                        if (p.getSaturation() < 13) {
                            p.setSaturation(p.getSaturation() + 7);
                        } else {
                            p.setSaturation(20);
                        }
                    } else if (p.getFoodLevel() < 20) {
                        p.setFoodLevel(20);
                        p.setItemInHand(new ItemStack(Material.BOWL));
                        if (p.getSaturation() < 13) {
                            p.setSaturation(p.getSaturation() + 7);
                        } else {
                            p.setSaturation(20);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onFireball(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Profile profile = Profile.get(player.getUniqueId());

        if (profile.getState() != ProfileState.FIGHTING) return;

        if (event.getItem() == null) return;
        if (!(event.getItem().getType() == Material.FIREBALL)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Fireball fireball = player.launchProjectile(Fireball.class);
            fireball.setIsIncendiary(false);
            fireball.setYield(0.0F);
            fireball.setVelocity(player.getLocation().getDirection().multiply(0.3));
        }
    }

    @EventHandler
    private void onFireballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getEntity();
            fireball.getWorld().createExplosion(fireball.getLocation(), 0.0F, false);
            fireball.getNearbyEntities(2.0, 2.0, 2.0).forEach(entity -> {
                if (entity instanceof Player) {
                    Player player = (Player) entity;

                    Vector direction = player.getLocation().getDirection().normalize();

                    double pitch = player.getLocation().getPitch();
                    double heightMultiplier = Math.sin(Math.toRadians(pitch)) * 2;
                    double speedMultiplier = player.isSprinting() ? 1.0 : 0.5;
                    double thresholdAngle = 70;

                    if (pitch < thresholdAngle) {
                        direction.multiply(-1);
                    }

                    Vector adjustedVelocity = direction.multiply(speedMultiplier).add(new Vector(0, heightMultiplier, 0));
                    Vector currentVelocity = player.getVelocity();
                    Vector newVelocity = currentVelocity.add(adjustedVelocity);

                    player.setVelocity(newVelocity);
                }
            });
        }
    }

    @EventHandler
    public void onSign(SignChangeEvent e) {
        int currentLine = 0;
        for (String line : e.getLines()) {
            if (line.equalsIgnoreCase("[Elevator]")) {
                if (currentLine < 3) {
                    if (e.getLine(currentLine + 1).equalsIgnoreCase("Up")) {
                        e.setLine(0, "");
                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&c[Elevator]"));
                        e.setLine(2, ChatColor.translateAlternateColorCodes('&', "Up"));
                        e.setLine(3, "");
                    } else if (e.getLine(currentLine + 1).equalsIgnoreCase("Down")) {
                        e.setLine(0, "");
                        e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&c[Elevator]"));
                        e.setLine(2, ChatColor.translateAlternateColorCodes('&', "Down"));
                        e.setLine(3, "");
                    } else {
                        //Bukkit.broadcastMessage(e.getLine(currentLine + 1));
                        e.getBlock().breakNaturally();
                        e.getPlayer().sendMessage(CC.translate("&cInvalid direction."));
                    }
                }
            }

            currentLine++;
        }
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent e) {
        boolean ret = e.getClickedBlock() == null;
        Player player = e.getPlayer();
        if (ret) {
            return;
        }
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.WALL_SIGN) || e.getClickedBlock().getType() == Material.SIGN_POST) {
            final Sign s = (Sign) e.getClickedBlock().getState();
            if (s.getLine(1).equalsIgnoreCase(ChatColor.RED + "[Elevator]")) { // If first line == "Elevator"
                if (s.getLine(2).equalsIgnoreCase("Up")) { // If second line == "Up"
                    Location loc = e.getClickedBlock().getLocation().add(0.0, 1.0, 0.0);
                    while (loc.getY() < 254.0) {
                        if (loc.getBlock().getType() != Material.AIR) {
                            while (loc.getBlockY() < 254) {
                                if (loc.getBlock().getType() == Material.AIR && loc.add(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR) {
                                    Location pl = player.getLocation();
                                    player.teleport(new Location(pl.getWorld(), loc.getX() + 0.5, loc.getY() - 1.0, loc.getZ() + 0.5, pl.getYaw(), pl.getPitch()));
                                    break;
                                }
                                loc.add(0.0, 1.0, 0.0);
                            }
                            break;
                        }
                        loc.add(0.0, 1.0, 0.0);
                    }
                    if (loc.getY() == 254.0) {
                        player.sendMessage(CC.translate("&cCould not teleport.")); // Send error if teleport isn't possbile.
                    }
                }
                else if (s.getLine(2).equalsIgnoreCase("Down")) { // If second line == "down"
                    Location loc = e.getClickedBlock().getLocation().subtract(0.0, 1.0, 0.0);
                    while (loc.getY() > 2.0) {
                        if (loc.getBlock().getType() != Material.AIR) {
                            while (loc.getY() > 2.0) {
                                if (loc.getBlock().getType() == Material.AIR && loc.subtract(0.0, 1.0, 0.0).getBlock().getType() == Material.AIR) {
                                    player.teleport(new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getY(), loc.getZ() + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch()));
                                    break;
                                }
                                loc.subtract(0.0, 1.0, 0.0);
                            }
                            break;
                        }
                        loc.subtract(0.0, 1.0, 0.0);
                    }
                    if (loc.getY() == 2.0) {
                        player.sendMessage(CC.translate("&cCould not teleport.")); // Send error if teleport isn't possible.
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.get(player.getUniqueId());
        if (profile.getState() == ProfileState.FIGHTING) {
            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                if (player.getLocation().getBlock().getType() == Material.ENDER_PORTAL || player.getLocation().getBlock().getType() == Material.ENDER_PORTAL_FRAME) {
                    if (LocationUtil.isTeamPortal(player)) {
                        player.sendMessage(CC.translate("&8[&c&lMatch&8] &7You can't enter your own portal"));
                        profile.getMatch().onDeath(player);
                        return;
                    }
                    BasicTeamRoundMatch match = (BasicTeamRoundMatch) profile.getMatch();
                    if (match.getState() == MatchState.ENDING_ROUND || match.getState() == MatchState.ENDING_MATCH) return;
                    match.getParticipants().forEach(gamePlayerGameParticipant ->
                            gamePlayerGameParticipant.getPlayers().forEach(gamePlayer -> {
                                Player other = gamePlayer.getPlayer();

                                new MessageFormat(Locale.MATCH_BRIDGE_SCORED
                                        .format(Profile.get(other.getUniqueId()).getLocale()))
                                        .add("{color}", match.getRelationColor(other, player).toString())
                                        .add("{player}", player.getName())
                                        .send(other);

                                match.broadcastTitle("&c&l" + player.getName() + " &7has scored!", "", 50);
                            }));

                    GameParticipant<MatchGamePlayer> otherTeam = match.getParticipantA()
                            .containsPlayer(player.getUniqueId()) ?
                            match.getParticipantB() :
                            match.getParticipantA();

                    otherTeam.getPlayers().forEach(matchGamePlayer -> matchGamePlayer.setDead(true));
                    otherTeam.setEliminated(true);

                    if (match.canEndRound()) {
                        match.setState(MatchState.ENDING_ROUND);
                        match.getLogicTask().setNextAction(2);
                        match.onRoundEnd();

                        if (match.canEndMatch()) match.setState(MatchState.ENDING_MATCH);
                    }
                }
            }
        }
    }
}
