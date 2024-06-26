package com.hysteria.practice.match.command;

import com.hysteria.practice.api.command.BaseCommand;
import com.hysteria.practice.api.command.Command;
import com.hysteria.practice.api.command.CommandArgs;
import com.hysteria.practice.match.Match;
import com.hysteria.practice.utilities.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CancelAllMatchesCommand extends BaseCommand {

    @Command(name = "cancelallmatches", permission = "hypractice.command.cancelallmatches")
    @Override
    public void onCommand(CommandArgs commandArgs) {
        Bukkit.broadcastMessage(CC.CHAT_BAR);
        Bukkit.broadcastMessage(CC.translate("&8[&b&lMatch&8] &7All matches has been cancelled."));
        Bukkit.broadcastMessage(CC.translate("&8[&b&lMatch&8] &7Preparing for reboot"));
        Bukkit.broadcastMessage(CC.CHAT_BAR);
        Match.getMatches().forEach(Match::end);
    }
}
