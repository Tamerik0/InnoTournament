package dev.necr0manthre.innotournament.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.necr0manthre.innotournament.tournament.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collection;

public class TeamsCommand {
	private static final SimpleCommandExceptionType ADD_DUPLICATE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.team.add.duplicate"));

	public static TournamentPlayerManager playerManager(CommandContext<CommandSourceStack> ctx) {
		return TournamentPlayerManager.get(ctx.getSource().getServer());
	}

	public static TournamentTeamManager teamManager(CommandContext<CommandSourceStack> ctx) {
		return TournamentTeamManager.get(ctx.getSource().getServer());
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("leave").executes(ctx -> {
					                    if (!checkTeam(ctx))
						                    return 1;
					                    var player = getPlayer(ctx);
					                    var team = getTeam(ctx);
					                    var oldOwner = team.owner;
					                    team.removePlayer(player);
					                    ctx.getSource().sendSuccess(() -> Component.literal("You left from team ").append(team.getPlayerTeam().getDisplayName()), false);
					                    if (team.getPlayerTeam().getPlayers().isEmpty())
						                    ctx.getSource().getServer().getScoreboard().removePlayerTeam(team.getPlayerTeam());
					                    else {
						                    for (var player1 : team.getPlayers()) {
							                    player1.player.sendSystemMessage(Component.literal(player.player.getScoreboardName() + " left from your team"));
						                    }
						                    if (oldOwner != team.owner) {
							                    team.owner.player.sendSystemMessage(Component.literal("You are owner of team ").withStyle(ChatFormatting.GOLD).append(team.getPlayerTeam().getDisplayName()));
						                    }
					                    }
					                    return 1;
				                    })));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("myTeam").executes((ctx) -> {
					                    if (!checkTeam(ctx))
						                    return 1;
					                    var player = playerManager(ctx).get(ctx.getSource().getPlayer());
					                    var team = teamManager(ctx).getTeam(player);

					                    var ans = Component.empty();
					                    ans.append(Component.literal("\n===============\n").withStyle(ChatFormatting.GRAY));
					                    boolean isOwner = team.owner == player;

					                    ans.append("Your team:\n");

					                    ans.append(Component.literal("Name: ").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
					                    ans.append(team.getPlayerTeam().getName());
					                    if (isOwner) {
						                    ans.append(" ");
						                    ans.append(Component.literal("[Edit]").withStyle(Style.EMPTY
								                                                                     .applyFormat(ChatFormatting.GRAY)
								                                                                     .withClickEvent(new ClickEvent.SuggestCommand("/teams edit name " + team.getPlayerTeam().getName()))
								                                                                     .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to edit name of your team")))));
					                    }
					                    ans.append("\n");

					                    ans.append(Component.literal("DisplayName: ").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
					                    ans.append(team.getPlayerTeam().getDisplayName());
					                    if (isOwner) {
						                    ans.append(" ");
						                    ans.append(Component.literal("[Edit]").withStyle(ChatFormatting.GRAY)
								                               .withStyle(Style.EMPTY
										                                          .withClickEvent(new ClickEvent.SuggestCommand("/teams edit displayName " + team.getPlayerTeam().getDisplayName().getString()))
										                                          .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to edit displayName of your team")))));
					                    }
					                    ans.append("\n");

					                    ans.append(Component.literal("Members: \n").withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.BOLD));
					                    ans.append(" ");
					                    ans.append(player.player.getDisplayName());
					                    ans.append(Component.literal(isOwner ? " (Owner, You) " : " (You) ").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
					                    ans.append(Component.literal("[Leave]").withStyle(ChatFormatting.RED)
							                               .withStyle(Style.EMPTY
									                                          .withClickEvent(new ClickEvent.RunCommand("/teams tryLeave"))
									                                          .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to leave your team")))));
					                    ans.append("\n");

					                    for (var player1 : team.getPlayers()) {
						                    if (player1 == player)
							                    continue;
						                    ans.append(" ");
						                    ans.append(player1.player.getDisplayName());
						                    if (isOwner) {
							                    ans.append(Component.literal("[Kick]").withStyle(ChatFormatting.RED)
									                               .withStyle(Style.EMPTY
											                                          .withClickEvent(new ClickEvent.RunCommand("/teams tryKick " + player1.player.getScoreboardName()))
											                                          .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to kick ").append(player1.player.getDisplayName()).append(" from your team")))));
						                    }
						                    if (player1 == team.owner) {
							                    ans.append(Component.literal(" (Owner)").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
						                    }
						                    ans.append("\n");
					                    }
					                    if (team.acceptAll) {
						                    ans.append(Component.literal("Accept all: ").withStyle(ChatFormatting.AQUA)
								                               .append(Component.literal("true").withStyle(ChatFormatting.GREEN))
								                               .withStyle(Style.EMPTY
										                                          .withHoverEvent(
												                                          new HoverEvent.ShowText(Component.literal("Your team accepts all player, invites and requests and not needed")))));
						                    if (isOwner) {
							                    ans.append(Component.literal(" [Change]")
									                               .withStyle(ChatFormatting.GRAY)
									                               .withStyle(Style.EMPTY
											                                          .withClickEvent(new ClickEvent.RunCommand("/teams acceptAll false"))
											                                          .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to make your team not accept all players, instead you will use invites or requests")))));
						                    }

					                    } else {
						                    ans.append(Component.literal("Accept all: ").withStyle(ChatFormatting.AQUA)
								                               .append(Component.literal("false").withStyle(ChatFormatting.DARK_GRAY))
								                               .withStyle(Style.EMPTY
										                                          .withHoverEvent(
												                                          new HoverEvent.ShowText(Component.literal("Your team accepts players only from invites or requests")))));
						                    if (isOwner) {
							                    ans.append(Component.literal(" [Change]")
									                               .withStyle(ChatFormatting.GRAY)
									                               .withStyle(Style.EMPTY
											                                          .withClickEvent(new ClickEvent.RunCommand("/teams acceptAll true"))
											                                          .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to make your team accept all players who typed /teams join <Your team name>")))));
						                    }
					                    }

					                    ans.append(Component.literal("\n===============").withStyle(ChatFormatting.GRAY));
					                    ctx.getSource().sendSystemMessage(ans);
					                    return 1;
				                    })));

		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("join")
						                          .then(Commands.argument("team", TeamArgument.team()).executes((ctx -> {
							                          var player = TournamentPlayerManager.getStatic(ctx.getSource().getPlayer());
							                          var team = teamManager(ctx).get(TeamArgument.getTeam(ctx, "team"));
							                          var invites = player.invites.stream().filter(inv -> inv.isValid() && inv.getTeam() == team).toList();
							                          if (!team.acceptAll && invites.isEmpty()) {
								                          ctx.getSource().sendFailure(Component.literal("You can't join this team"));
								                          return 1;
							                          }
							                          var tournament = Tournament.get(ctx.getSource().getServer());
							                          if (tournament == null)
								                          team.addPlayer(player);
							                          else
								                          tournament.addPlayerToTeam(player, team);
							                          player.invites.clear();
							                          sendJoinPlayer(ctx, player, team);
							                          return 1;
						                          })))));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("create")
						                          .then(
								                          Commands.argument("team", StringArgumentType.word())
										                          .executes(context -> executeAdd(context, StringArgumentType.getString(context, "team")))
										                          .then(
												                          Commands.argument("displayName", ComponentArgument.textComponent(registryAccess))
														                          .executes(
																                          context -> executeAdd(context, StringArgumentType.getString(context, "team"), ComponentArgument.getResolvedComponent(context, "displayName"))
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
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("reject")
						                          .then(Commands.argument("player", EntityArgument.player())
								                                .executes(ctx -> {
									                                var teamManager = TournamentTeamManager.get(ctx.getSource().getServer());
									                                var playerManager = TournamentPlayerManager.get(ctx.getSource().getServer());
									                                var self = playerManager.get(ctx.getSource().getPlayerOrException());
									                                var team = teamManager.getTeam(self);
									                                if (team == null) {
										                                ctx.getSource().sendFailure(Component.literal("You can't do it because you don't have team"));
										                                return 1;
									                                }
									                                var huy = TournamentPlayerManager.getStatic(EntityArgument.getPlayer(ctx, "player"));
									                                var invites = huy.invites.stream().filter(inv -> inv.isValid() && inv.getTeam() == team).toList();
									                                if (invites.isEmpty()) {
										                                ctx.getSource().sendFailure(Component.literal("You can't do it because this player has no join request to your team"));
										                                return 1;
									                                }
									                                huy.invites.removeAll(invites);
									                                return 1;
								                                }))
				                    ));
		dispatcher.register(Commands.literal("teams").then(
				Commands.literal("invite")
						.then(Commands.argument("player", EntityArgument.player())
								      .executes(ctx -> {
									      var teamManager = TournamentTeamManager.get(ctx.getSource().getServer());
									      var playerManager = TournamentPlayerManager.get(ctx.getSource().getServer());
									      var self = playerManager.get(ctx.getSource().getPlayerOrException());
									      var team = teamManager.getTeam(self);
									      if (team == null) {
										      team = teamManager.get(ctx.getSource().getServer().getScoreboard().addPlayerTeam(teamManager.createNewUnnamedTeamName()));
										      team.addPlayer(self);
									      }
									      var huy = EntityArgument.getPlayer(ctx, "player");
									      playerManager.get(huy).invite(teamManager.getTeam(self), 1000);
									      huy.sendSystemMessage(Component.literal(self.player.getScoreboardName() + " invites you to team ").append(team.getPlayerTeam().getDisplayName()).append(" ")
											                            .append(Component.literal("[Принять]").withStyle(Style.EMPTY.withColor(0xFF00).withClickEvent(new ClickEvent.RunCommand("/teams join " + team.getPlayerTeam().getName()))))
											                            .append(Component.literal("[Отклонить]").withStyle(Style.EMPTY.withColor(0xFF0000).withClickEvent(new ClickEvent.RunCommand("/teams reject " + team.getPlayerTeam().getName())))));
									      return 1;
								      }))));

		dispatcher.register(Commands.literal("teams").then(
				Commands.literal("tryLeave")
						.executes(ctx -> {
							if (!checkTeam(ctx))
								return 1;
							ctx.getSource().sendSuccess(() -> Component.literal("Are you sure? ")
									                                  .append(Component.literal("[Leave]").withStyle(ChatFormatting.RED)
											                                          .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to leave your team")))
													                                                     .withClickEvent(new ClickEvent.RunCommand("/teams leave")))), false);
							return 1;
						})));

		dispatcher.register(Commands.literal("teams").then(
				Commands.literal("tryKick")
						.then(Commands.argument("player", EntityArgument.player())
								      .executes(ctx -> {
									      if (!checkTeam(ctx))
										      return 1;
									      if (!checkOwner(ctx))
										      return 1;
									      var player = TournamentPlayerManager.getStatic(EntityArgument.getPlayer(ctx, "player"));
									      ctx.getSource().sendSuccess(() -> Component.literal("Are you sure? ")
											                                        .append(Component.literal(" [Kick ").append(player.player.getDisplayName()).append(" ]").withStyle(ChatFormatting.RED)
													                                                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to kick ").append(player.player.getDisplayName()).append(" from your team")))
															                                                                    .withClickEvent(new ClickEvent.RunCommand("/teams kick " + player.player.getScoreboardName())))), false);
									      return 1;
								      }))));

		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("edit")
						                          .then(Commands.literal("name")
								                                .then(Commands.argument("name", StringArgumentType.word())
										                                      .executes(ctx -> {
											                                      if (!checkTeam(ctx))
												                                      return 1;
											                                      if (!checkOwner(ctx))
												                                      return 1;
											                                      getTeam(ctx).changeName(StringArgumentType.getString(ctx, "name"));
											                                      return 1;
										                                      })))
						                          .then(Commands.literal("displayName")
								                                .then(Commands.argument("displayName", ComponentArgument.textComponent(registryAccess))
										                                      .executes(ctx -> {
											                                      if (!checkTeam(ctx))
												                                      return 1;
											                                      if (!checkOwner(ctx))
												                                      return 1;
											                                      getTeam(ctx).getPlayerTeam().setDisplayName(ComponentArgument.getResolvedComponent(ctx, "displayName"));
											                                      return 1;
										                                      })))));
		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("acceptAll")
						                          .then(Commands.argument("acceptAll", BoolArgumentType.bool())
								                                .executes(ctx -> {
									                                if (!checkTeam(ctx))
										                                return 1;
									                                if (!checkOwner(ctx))
										                                return 1;
									                                getTeam(ctx).acceptAll = BoolArgumentType.getBool(ctx, "acceptAll");
									                                return 1;
								                                }))));

		dispatcher.register(Commands.literal("teams")
				                    .then(Commands.literal("kick")
						                          .then(Commands.argument("player", EntityArgument.player())
								                                .executes(ctx -> {
									                                if (!checkTeam(ctx))
										                                return 1;
									                                if (!checkOwner(ctx))
										                                return 1;
									                                var player = TournamentPlayerManager.getStatic(EntityArgument.getPlayer(ctx, "player"));
									                                getTeam(ctx).removePlayer(player);
									                                player.player.sendSystemMessage(Component.literal("You was kicked from team"));
									                                TournamentTeamManager.broadcastToTeam(Component.empty().append(player.player.getDisplayName()).append(" was kicked from team"), getTeam(ctx), playerManager(ctx));
									                                return 1;
								                                }))));
	}

	private static int executeAdd(CommandContext<CommandSourceStack> ctx, String team) throws CommandSyntaxException {
		return executeAdd(ctx, team, Component.literal(team));
	}

	private static int executeAdd(CommandContext<CommandSourceStack> ctx, String team, Component displayName) throws CommandSyntaxException {
		ServerScoreboard scoreboard = ctx.getSource().getServer().getScoreboard();
		if (scoreboard.getPlayersTeam(team) != null) {
			throw ADD_DUPLICATE_EXCEPTION.create();
		} else {
			PlayerTeam team2 = scoreboard.addPlayerTeam(team);
			team2.setDisplayName(displayName);
			teamManager(ctx).get(team2).addPlayer(getPlayer(ctx));
			ctx.getSource().sendSuccess(() -> Component.translatable("commands.team.add.success", team2.getDisplayName()), true);
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

	private static void sendJoinPlayer(CommandContext<CommandSourceStack> ctx, TournamentPlayer player, TournamentTeam team) {
		player.player.sendSystemMessage(Component.literal("You joined team ").append(team.getPlayerTeam().getDisplayName()));
		for (var player1 : team.getPlayers()) {
			if (player1 == player)
				continue;
			player1.player.sendSystemMessage(Component.empty().append(player.player.getDisplayName()).append(Component.literal(" joined your team")));
		}
	}

	private static boolean checkTeam(CommandContext<CommandSourceStack> ctx) {
		if (getTeam(ctx) == null) {
			ctx.getSource().sendFailure(Component.literal("You're not on the team."));
			return false;
		}
		return true;
	}

	private static boolean checkOwner(CommandContext<CommandSourceStack> ctx) {
		if (getTeam(ctx).owner == getPlayer(ctx))
			return true;
		ctx.getSource().sendFailure(Component.literal("You can't do this because you are not the owner of team"));
		return false;
	}

	private static TournamentPlayer getPlayer(CommandContext<CommandSourceStack> ctx) {
		return playerManager(ctx).get(ctx.getSource().getPlayer());
	}

	private static TournamentTeam getTeam(CommandContext<CommandSourceStack> ctx) {
		return teamManager(ctx).getTeam(getPlayer(ctx));
	}
}
