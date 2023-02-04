package rip.crystal.practice.player.cosmetics.impl.killeffects.menu;
/* 
   Made by cpractice Development Team
   Created on 30.11.2021
*/

import rip.crystal.practice.player.cosmetics.impl.killeffects.KillEffectType;
import rip.crystal.practice.player.profile.Profile;
import rip.crystal.practice.utilities.ItemBuilder;
import rip.crystal.practice.utilities.TaskUtil;
import rip.crystal.practice.utilities.chat.CC;
import rip.crystal.practice.utilities.menu.Button;
import rip.crystal.practice.utilities.menu.pagination.PaginatedMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class KillEffectsMenu extends PaginatedMenu
{

    @Override
    public String getPrePaginatedTitle(final Player player) {
        return CC.translate("&4&lDeath Effects");
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        //Stream.of(KillEffectType.values()).forEach(type -> buttons.put(KillEffectType.values().length, new SettingsButton(type)));
        for (KillEffectType killEffectTypes : KillEffectType.values()) {
            buttons.put(buttons.size(), new SettingsButton(killEffectTypes));
        }
        return buttons;
    }

    private static class SettingsButton extends Button
    {
        private final KillEffectType type;

        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.get(player.getUniqueId());
            return new ItemBuilder(type.getMaterial())
                    .name((profile.getKillEffectType() == this.type) ? "&4&l" + this.type.getName() : (this.type.hasPermission(player) ? (CC.translate("&4&l")) : "&4&l") + this.type.getName())
                    .durability((profile.getKillEffectType() == this.type) ? 5 : (this.type.hasPermission(player) ? 3 : 14))
                    .lore(CC.MENU_BAR)
                    .lore("&7Left click to change your")
                    .lore("&7death effect to to " + "&4" + this.type.getName() + "&7.")
                    .lore("")
                    .lore("&7Selected Death Effect: " + "&4" + ((profile.getKillEffectType() != null) ? profile.getKillEffectType().getName() : "&4None"))
                    .lore((profile.getKillEffectType() == this.type) ? "&7That death effect is already selected." : (this.type.hasPermission(player) ? "&7Click to select this death effect." : "&4You don't own this death effect."))
                    .lore(CC.MENU_BAR)
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.get(player.getUniqueId());
            if (!this.type.hasPermission(player)) {
                player.sendMessage(CC.translate("&7You don't have the &4" + this.type.getName() + "&7 death effect. Purchase it at &4store.hy-pvp.net" + "&7."));
            }
            else if (profile.getKillEffectType() == this.type) {
                player.sendMessage(CC.translate("&4" + this.type.getName() + "&7 death effect is already selected."));
            }
            else {
                profile.setKillEffectType(this.type);
                player.sendMessage(CC.translate("&4" + this.type.getName() + "&7 is now set as your death effect."));
            }
            player.closeInventory();
            TaskUtil.runAsync(profile::save);
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }

        public SettingsButton(final KillEffectType type) {
            this.type = type;
        }
    }
}
