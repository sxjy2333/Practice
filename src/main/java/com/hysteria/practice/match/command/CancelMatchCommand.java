package com.hysteria.practice.match.command;

import com.hysteria.practice.Locale;
import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.utilities.MessageFormat;
import com.hysteria.practice.chunk.ChunkRestorationManager;
import com.hysteria.practice.game.arena.impl.StandaloneArena;
import com.hysteria.practice.utilities.chat.CC;
import com.hysteria.practice.api.command.BaseCommand;
import com.hysteria.practice.api.command.Command;
import com.hysteria.practice.api.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CancelMatchCommand extends BaseCommand {

	@Command(name = "cancelmatch", permission = "hypractice.command.cancelmatch")
	@Override
	public void onCommand(CommandArgs commandArgs) {
		Player player = commandArgs.getPlayer();
		String[] args = commandArgs.getArgs();

		if (args.length == 0) {
			player.sendMessage(CC.RED + "Usage: /cancelmatch <player>.");
			return;
		}

		Player target = Bukkit.getPlayer(args[0]);

		if(target == null) {
			player.sendMessage(CC.RED + "This player isn't online.");
			return;
		}

		Profile targetProfile = Profile.get(target.getUniqueId());

		if(targetProfile.getMatch() == null) {
			player.sendMessage(CC.RED + "Player is not in a match.");
			return;
		}

		new MessageFormat(Locale.MATCH_CANCELLED.format(targetProfile.getLocale()))
				.add("<cancelled_by>", player.getName())
				.send(player);

		targetProfile.getMatch().getArena().setActive(false);
		targetProfile.getMatch().end();

		player.sendMessage(CC.translate("&7You have cancelled &b" + target.getName() + "'s &7match."));
	}
}
