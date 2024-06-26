package com.hysteria.practice.game.ffa.command.subcommands;
/* 
   Made by hypractice Development Team
   Created on 27.11.2021
*/

import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.player.profile.ProfileState;
import com.hysteria.practice.game.arena.Arena;
import com.hysteria.practice.HyPractice;
import com.hysteria.practice.api.command.BaseCommand;
import com.hysteria.practice.api.command.Command;
import com.hysteria.practice.api.command.CommandArgs;
import org.bukkit.entity.Player;

public class FFAJoinCommand extends BaseCommand {

    @Command(name="ffa.join")
    @Override
    public void onCommand(CommandArgs commandArgs) {
        final Player player = commandArgs.getPlayer();
        final Profile profile = Profile.get(player.getUniqueId());

        if(profile.getState() != ProfileState.LOBBY) {
            return;
        }

        HyPractice.get().getFfaManager().handleFirstFFAJoin(player, Arena.getByName("FFA"));
    }
}
