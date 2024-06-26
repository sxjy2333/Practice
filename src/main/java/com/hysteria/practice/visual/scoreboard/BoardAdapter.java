package com.hysteria.practice.visual.scoreboard;

import com.alonsoaliaga.alonsoleagues.api.AlonsoLeaguesAPI;
import com.google.common.collect.Lists;
import com.hysteria.practice.HyPractice;
import com.hysteria.practice.game.event.game.EventGame;
import com.hysteria.practice.game.tournament.Tournament;
import com.hysteria.practice.player.profile.Profile;
import com.hysteria.practice.player.profile.ProfileState;
import com.hysteria.practice.player.queue.QueueProfile;
import com.hysteria.practice.utilities.Animation;
import com.hysteria.practice.utilities.TimeUtil;
import com.hysteria.practice.utilities.elo.EloUtil;
import com.hysteria.practice.utilities.file.type.BasicConfigurationFile;
import com.hysteria.practice.visual.scoreboard.impl.Assemble;
import com.hysteria.practice.visual.scoreboard.impl.AssembleAdapter;
import com.hysteria.practice.player.party.Party;
import com.hysteria.practice.visual.scoreboard.game.ScoreboardAdapter;
import com.hysteria.practice.player.profile.modmode.Modmode;
import com.hysteria.practice.utilities.chat.CC;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class BoardAdapter implements AssembleAdapter {

	@Override
	public String getTitle(Player player) {
		return CC.translate(Animation.getScoreboardTitle());
	}

	@Override
	public List<String> getLines(Player player) {
		Profile profile = Profile.get(player.getUniqueId());
		if (!profile.getOptions().showScoreboard()) return Lists.newArrayList();
		List<String> lines = Lists.newArrayList();
		BasicConfigurationFile config = HyPractice.get().getScoreboardConfig();
		String bars = config.getString("LINES.BARS");

		if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.QUEUEING) {
			config.getStringList("LINES.LOBBY").forEach(line -> lines.add(line
				.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
				.replace("{in-fights}", String.valueOf(HyPractice.get().getInFights()))
				.replace("{in-queues}", String.valueOf(HyPractice.get().getInQueues()))
				.replace("{division}", String.valueOf(HyPractice.get().isRunningAlonsoLeagues() ? AlonsoLeaguesAPI.getLeague(player.getUniqueId()) : ""))
				.replace("{coins}", String.valueOf(profile.getCoins()))
				.replace("{elo}", String.valueOf(EloUtil.getGlobalElo(profile)))));
		}

		if (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.TOURNAMENT) {
			if (Tournament.getTournament() != null) {
				lines.addAll(Tournament.getTournament().getTournamentScoreboard());
			} else if (profile.getParty() != null) {
				int added = 0;
				Party party = profile.getParty();
				config.getStringList("LINES.PARTY.LINES").forEach(line -> lines.add(line.replace("{bars}", bars)));

				if (party.getListOfPlayers().size() <= 4) {
					for (Player otherPlayer : party.getListOfPlayers()) {
						added++;

						lines.add(config.getString("LINES.PARTY.MEMBERS-FORMAT")
								.replace("{color}", Profile.get(otherPlayer.getUniqueId()).getColor())
								.replace("{player}", otherPlayer.getName()));

						if (added >= 4) break;
					}
				} else {
					config.getStringList("LINES.PARTY.INFO").forEach(line -> lines.add(line.replace("{leader-color}", Profile.get(party.getLeader().getPlayer().getUniqueId()).getColor())
							.replace("{leader}", party.getLeader().getName())
							.replace("{size}", String.valueOf(party.getListOfPlayers().size()))));
				}
			} else if (profile.getClan() != null) {
				lines.addAll(profile.getClan().getClanScoreboard());
			}
		} else if (profile.getState() == ProfileState.QUEUEING) {
			QueueProfile queueProfile = profile.getQueueProfile();

			config.getStringList("LINES.IN-QUEUE").forEach(line -> {
				if (line.contains("{ranked}")) {
					if (queueProfile.getQueue().isRanked()) {
						lines.add(config.getString("LINES.RANKED-QUEUE")
								.replace("{min-range}", String.valueOf(queueProfile.getMinRange()))
								.replace("{max-range}", String.valueOf(queueProfile.getMaxRange())));
					}
					return;
				}
				if (line.contains("{pingrange}")) {
					//if(profile.getOptions().isUsingPingFactor()) {
						lines.add(config.getString("LINES.PINGRANGE-QUEUE").replace("{range}", "" + (profile.getPingRange() == -1 ? "Unrestricted" : Integer.valueOf(profile.getPingRange()))));
					//}
					return;
				}
				lines.add(line.replace("{queue}", queueProfile.getQueue().getQueueName())
						.replace("{elapsed}", TimeUtil.millisToTimer(queueProfile.getPassed()))
						.replace("{bars}", bars));
			});
		}
		else if (profile.getState() == ProfileState.FIGHTING) {
			lines.addAll(profile.getMatch().getScoreboardLines(player));
		}
		else if(profile.getState() == ProfileState.SPECTATING) {
			//lines.addAll(profile.getBasicTeamMatch().applySpectatorScoreboard(player));
			lines.addAll(ScoreboardAdapter.getScoreboardLinesSpectator(player));
		}
		else if (profile.getState() == ProfileState.FFA) {
			lines.addAll(ScoreboardAdapter.getScoreboardLinesFFA(player));
		}
		if (profile.getState() == ProfileState.EVENT) {
			if (EventGame.getActiveGame() != null) {
				lines.addAll(EventGame.getActiveGame().getGameLogic().getScoreboardEntries());
			}
		}
		else if (profile.getState() == ProfileState.STAFF_MODE) {
			lines.addAll(Modmode.getScoreboardLines(player));
		}

		lines.add(0, "&0" + bars);
		if (config.getBoolean("FOOTER-ENABLED")) {
			lines.add("");
			lines.add(Animation.getScoreboardFooter());
		}
		lines.add("&7" + bars);

		return HyPractice.get().isRunningPlaceholderAPI() ? PlaceholderAPI.setPlaceholders(player, lines) : lines;
	}

	public static String getDivision(Player player) {
		Profile profile = Profile.get(player.getUniqueId());
		int elo = EloUtil.getGlobalElo(profile);
		String division = "";

		if (elo <= 1000) {
			division = CC.GRAY + "Silver V";
		}
		if (elo >= 1000) {
			division = CC.GRAY + "Silver IV";
		}
		if (elo >= 1025) {
			division = CC.GRAY + "Silver III";
		}
		if (elo >= 1050) {
			division = CC.GRAY + "Silver II";
		}
		if (elo >= 1075) {
			division = CC.GRAY + "Silver I";
		}
		if (elo >= 1100) {
			division = CC.YELLOW + "Gold V";
		}
		if (elo >= 1125) {
			division = CC.YELLOW + "Gold IV";
		}
		if (elo >= 1150) {
			division = CC.YELLOW + "Gold III";
		}
		if (elo >= 1175) {
			division = CC.YELLOW + "Gold II";
		}
		if (elo >= 1200) {
			division = CC.YELLOW + "Gold I";
		}
		if (elo >= 1250) {
			division = CC.AQUA + "Platinum V";
		}
		if (elo >= 1325) {
			division = CC.AQUA + "Platinum IV";
		}
		if (elo >= 1425) {
			division = CC.AQUA + "Platinum III";
		}
		if (elo >= 1450) {
			division = CC.AQUA + "Platinum II";
		}
		if (elo >= 1475) {
			division = CC.AQUA + "Platinum I";
		}
		if (elo >= 2000) {
			division = CC.GOLD + "Champion";
		}

		return division;
	}

	public static void hook() {
		Assemble assemble = new Assemble(HyPractice.get(), new BoardAdapter());
		assemble.setTicks(2);
	}
}
