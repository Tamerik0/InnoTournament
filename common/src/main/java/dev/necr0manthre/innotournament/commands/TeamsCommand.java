package dev.necr0manthre.innotournament.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.necr0manthre.innotournament.tournament.TournamentPlayerManager;
import dev.necr0manthre.innotournament.tournament.TournamentTeamManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collection;

public class TeamsCommand {
	private static final SimpleCommandExceptionType ADD_DUPLICATE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.team.add.duplicate"));

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("leave").executes((context) -> {
					                    var teamName = context.getSource().getPlayer().getTeam().getDisplayName().getString();
					                    context.getSource().getServer().getScoreboard().removePlayerFromTeam(context.getSource().getPlayer().getScoreboardName(), context.getSource().getPlayer().getTeam());
					                    context.getSource().sendSuccess(() -> Component.literal("You left from team " + teamName), false);
					                    return 1;
				                    })));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("get").executes((context) -> {
					                    context.getSource().sendSuccess(() -> Component.literal("You team is " + context.getSource().getPlayer().getTeam().getDisplayName().getString()), false);
					                    return 1;
				                    })));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("delete").executes((context) -> {
					                    var team = context.getSource().getPlayer().getTeam();
					                    if (team.getPlayers().size() == 1) {
						                    context.getSource().getServer().getScoreboard().removePlayerTeam(context.getSource().getPlayer().getTeam());
						                    context.getSource().sendSuccess(() -> Component.literal("You team was removed"), false);
					                    } else {
						                    context.getSource().sendSuccess(() -> Component.literal("You can't delete your team"), false);
					                    }
					                    return 1;
				                    })));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("join")
						                          .then(Commands.argument("team", TeamArgument.team()).executes((context -> {
							                          var server = context.getSource().getServer();
							                          var player = context.getSource().getPlayer();
							                          var team = TeamArgument.getTeam(context, "team");
							                          var tournamentTeam = TournamentTeamManager.get(server).get(team);
							                          if (tournamentTeam.acceptAll) {
								                          tournamentTeam.addPlayer(TournamentPlayerManager.get(server).get(player));
								                          context.getSource().sendSuccess(() -> Component.literal("You joined team " + team.getDisplayName().getString()), false);
							                          } else {
								                          context.getSource().sendFailure(Component.literal("You can't join this team"));
							                          }
							                          return 1;
						                          })))));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("request")
						                          .then(Commands.argument("team", TeamArgument.team()).executes((context -> {
							                          var server = context.getSource().getServer();
							                          var player = context.getSource().getPlayer();
							                          var team = TeamArgument.getTeam(context, "team");
							                          var tournamentTeam = TournamentTeamManager.get(server).get(team);
							                          if (tournamentTeam.acceptAll) {
								                          tournamentTeam.addPlayer(TournamentPlayerManager.get(server).get(player));
								                          context.getSource().sendSuccess(() -> Component.literal("You joined team " + team.getDisplayName().getString()), false);
							                          } else {
								                          for (var tournamentPlayer : tournamentTeam.getPlayers(TournamentPlayerManager.get(server))) {
									                          tournamentPlayer.player.sendSystemMessage(Component.literal("Player " + player.getDisplayName().getString() + " requested to join your team " + team.getDisplayName().getString()));
								                          }
								                          tournamentTeam.requests.add(TournamentPlayerManager.get(server).get(player));
								                          context.getSource().sendSuccess(() -> Component.literal("You requested to join team " + team.getDisplayName().getString()), false);
							                          }
							                          return 1;
						                          })))));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("accept")
						                          .then(
								                          Commands.argument("player", StringArgumentType.word())
										                          .suggests((context, builder) -> {
											                          var teamManager = TournamentTeamManager.get(context.getSource().getServer());
											                          if (context.getSource().getPlayer().getTeam() == null)
												                          return builder.buildFuture();
											                          var team = teamManager.get(context.getSource().getPlayer().getTeam());
											                          for (var player : team.requests) {
												                          builder.suggest(player.player.getScoreboardName());
											                          }
											                          return builder.buildFuture();
										                          }).executes(ctx -> {
											                          var teamManager = TournamentTeamManager.get(ctx.getSource().getServer());
											                          if (ctx.getSource().getPlayer().getTeam() == null)
												                          return 0;
											                          var team = teamManager.get(ctx.getSource().getPlayer().getTeam());
											                          for (var request : team.requests) {
												                          if (request.player.getScoreboardName().equals(StringArgumentType.getString(ctx, "player"))) {
													                          team.addPlayer(request);
													                          team.requests.remove(request);
													                          ctx.getSource().sendSuccess(() -> Component.literal("You accepted player " + request.player.getDisplayName().getString() + " to your team " + team.playerTeam.getDisplayName().getString()), false);
													                          return 1;
												                          }
											                          }
											                          return 1;
										                          })
										                          .then(
												                          Commands.argument("displayName", ComponentArgument.textComponent(registryAccess))
														                          .executes(
																                          context -> executeAdd(context.getSource(), StringArgumentType.getString(context, "team"), ComponentArgument.getResolvedComponent(context, "displayName"))
														                          )
										                          )
						                          )));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("add")
						                          .then(
								                          Commands.argument("team", StringArgumentType.word())
										                          .executes(context -> executeAdd(context.getSource(), StringArgumentType.getString(context, "team")))
										                          .then(
												                          Commands.argument("displayName", ComponentArgument.textComponent(registryAccess))
														                          .executes(
																                          context -> executeAdd(context.getSource(), StringArgumentType.getString(context, "team"), ComponentArgument.getResolvedComponent(context, "displayName"))
														                          )
										                          )
						                          )));
		dispatcher.register(Commands.literal("teams").then(
				Commands.literal("list")
						.executes(context -> executeListTeams(context.getSource()))
						.then(
								Commands.argument("team", TeamArgument.team())
										.executes(context -> executeListMembers(context.getSource(), TeamArgument.getTeam(context, "team")))
						)
		));

	}

	private static int executeAdd(CommandSourceStack source, String team) throws CommandSyntaxException {
		return executeAdd(source, team, Component.literal(team));
	}

	private static int executeAdd(CommandSourceStack source, String team, Component displayName) throws CommandSyntaxException {
		ServerScoreboard scoreboard = source.getServer().getScoreboard();
		if (scoreboard.getPlayersTeam(team) != null) {
			throw ADD_DUPLICATE_EXCEPTION.create();
		} else {
			PlayerTeam team2 = scoreboard.addPlayerTeam(team);
			team2.setDisplayName(displayName);
			source.sendSuccess(() -> Component.translatable("commands.team.add.success", team2.getDisplayName()), true);
			return scoreboard.getPlayerTeams().size();
		}
	}

	private static int executeListMembers(CommandSourceStack source, PlayerTeam team) {
		Collection<String> collection = team.getPlayers();
		if (collection.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("commands.team.list.members.empty", team.getDisplayName()), false);
		} else {
			source.sendSuccess(
					() -> Component.translatable("commands.team.list.members.success", team.getDisplayName(), collection.size(), ComponentUtils.formatList(collection)), false
			);
		}

		return collection.size();
	}

	private static int executeListTeams(CommandSourceStack source) {
		Collection<PlayerTeam> collection = source.getServer().getScoreboard().getPlayerTeams();
		if (collection.isEmpty()) {
			source.sendSuccess(() -> Component.translatable("commands.team.list.teams.empty"), false);
		} else {
			source.sendSuccess(() -> Component.translatable("commands.team.list.teams.success", collection.size(), ComponentUtils.formatList(collection, PlayerTeam::getDisplayName)), false);
		}

		return collection.size();
	}
}
