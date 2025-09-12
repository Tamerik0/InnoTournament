package dev.necr0manthre.innotournament.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.dominion.ecs.api.Entity;
import dev.necr0manthre.innotournament.players.PlayerManager;
import dev.necr0manthre.innotournament.teams.components.TeamOwner;
import dev.necr0manthre.innotournament.teams.components.TeamSettings;
import dev.necr0manthre.innotournament.teams.core.TeamManager;
import dev.necr0manthre.innotournament.teams.invites.InviteSystem;
import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.util.CommandRegisterer;
import dev.necr0manthre.innotournament.util.TournamentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collection;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public class TeamsCommand {
    private static final SimpleCommandExceptionType ADD_DUPLICATE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.team.add.duplicate"));


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        var registerer = new CommandRegisterer(dispatcher, "teams", "p");
        registerer.register(Commands.literal("leave").executes(ctx -> {
            if (!checkTeam(ctx))
                return 1;
            var player = player(ctx).orElseThrow();
            var team = team(ctx).orElseThrow();
            var oldOwner = TeamOwner.getTeamOwner(team);
            boolean success = TeamManager.tryKickPlayerFromTeam(team, player).isSuccess();
            if (success)
                ctx.getSource().sendSuccess(() -> Component.literal("You left from team ").append(TeamManager.getPlayerTeam(team).getDisplayName()), false);
            else
                return 1;
            if (TeamManager.getPlayers(team).isEmpty())
                ctx.getSource().getServer().getScoreboard().removePlayerTeam(TeamManager.getPlayerTeam(team));
            else {
                for (var player1 : TeamManager.getPlayers(team)) {
                    PlayerManager.getServerPlayer(player1).ifPresent(p -> p.sendSystemMessage(Component.literal(player.getName() + " left from your team")));
                }
                if (oldOwner != TeamOwner.getTeamOwner(team)) {
                    PlayerManager.getServerPlayer(TeamOwner.getTeamOwner(team)).ifPresent(p -> p.sendSystemMessage(Component.literal("You are owner of team ").withStyle(ChatFormatting.GOLD).append(TeamManager.getPlayerTeam(team).getDisplayName())));
                }
            }
            return 1;
        }));
        registerer.register(Commands.literal("myTeam").executes((ctx) -> {
            if (!checkTeam(ctx))
                return 1;
            var player = player(ctx).orElseThrow();
            var team = team(ctx).orElseThrow();

            var ans = Component.empty();
            ans.append(Component.literal("\n===============\n").withStyle(ChatFormatting.GRAY));
            boolean isOwner = TeamOwner.getTeamOwner(team) == player;

            ans.append("Your team:\n");

            ans.append(Component.literal("Name: ").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
            ans.append(TeamManager.getPlayerTeam(team).getDisplayName());
            if (isOwner) {
                ans.append(" ");
                ans.append(Component.literal("[Edit]").withStyle(Style.EMPTY
                        .applyFormat(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent.SuggestCommand("/teams edit name " + TeamManager.getPlayerTeam(team).getName()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to edit name of your team")))));
            }
            ans.append("\n");

            ans.append(Component.literal("Members: \n").withStyle(ChatFormatting.DARK_AQUA).withStyle(ChatFormatting.BOLD));
            ans.append(" ");
            ans.append(PlayerManager.getDisplayName(player).orElse(Component.literal("Unknown player")));
            ans.append(Component.literal(isOwner ? " (Owner, You) " : " (You) ").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
            ans.append(Component.literal("[Leave]").withStyle(ChatFormatting.RED)
                    .withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.RunCommand("/teams tryLeave"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to leave your team")))));
            ans.append("\n");

            for (var player1 : TeamManager.getPlayers(team)) {
                if (player1 == player)
                    continue;
                ans.append(" ");
                ans.append(PlayerManager.getDisplayName(player1).orElse(Component.literal("Unknown player")));
                if (isOwner) {
                    ans.append(Component.literal(" [Kick]").withStyle(ChatFormatting.RED)
                            .withStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent.RunCommand("/teams tryKick " + player1.getName()))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to kick ").append(PlayerManager.getDisplayName(player1).orElse(Component.literal("Unknown player"))).append(" from your team")))));
                }
                if (player1 == TeamOwner.getTeamOwner(team)) {
                    ans.append(Component.literal(" (Owner)").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
                }
                ans.append("\n");
            }
            if (TeamSettings.getSettings(team).acceptAll) {
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
        }));

        registerer.register(Commands.literal("join")
                .then(Commands.argument("team", TeamArgument.team()).executes((ctx -> {
                    if (player(ctx).isEmpty())
                        return 1;
                    var player = player(ctx).orElseThrow();
                    var team = teamManager(ctx).getEntity(TeamArgument.getTeam(ctx, "team"));
                    if (!TeamSettings.getSettings(team).acceptAll && !InviteSystem.isPlayerInvitedToTeam(player, team) ||
                            !TeamManager.tryAddPlayerToTeam(team, player).isSuccess()) {
                        ctx.getSource().sendFailure(Component.literal("You can't join this team"));
                        return 1;
                    }
                    sendJoinPlayer(ctx, player, team);
                    updateSidebars(ctx);
                    return 1;
                }))));
        registerer.register(Commands.literal("create")
                .then(Commands.argument("displayName", ComponentArgument.textComponent(registryAccess))
                        .executes(ctx -> {
                            ServerScoreboard scoreboard = ctx.getSource().getServer().getScoreboard();
                            var displayName = ComponentArgument.getResolvedComponent(ctx, "displayName");
                            var name = displayName.getString();
                            if (scoreboard.getPlayerTeam(name) != null)
                                throw ADD_DUPLICATE_EXCEPTION.create();
                            PlayerTeam team = scoreboard.addPlayerTeam(name);
                            team.setDisplayName(displayName);
                            var teamEntity = teamManager(ctx).getEntity(team);
                            var playerEntity = player(ctx).orElseThrow();
                            TeamManager.forceAddPlayer(teamEntity, playerEntity);
                            TeamOwner.setTeamOwner(teamEntity, playerEntity);
                            ctx.getSource().sendSuccess(() -> Component.translatable("commands.team.add.success", displayName), true);
                            return 1;
                        })
                )
        );
        registerer.register(
                Commands.literal("list")
                        .executes(context -> executeListTeams(context.getSource()))
                        .then(
                                Commands.argument("team", TeamArgument.team())
                                        .executes(context -> executeListMembers(context.getSource(), TeamArgument.getTeam(context, "team")))
                        )
        );
        registerer.register(Commands.literal("reject")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            var team = team(ctx);
                            if (team.isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal("You can't do it because you don't have team"));
                                return 1;
                            }
                            var huy = PlayerManager.getEntity(EntityArgument.getPlayer(ctx, "player"));
                            if (!InviteSystem.isPlayerInvitedToTeam(huy, team.get())) {
                                ctx.getSource().sendFailure(Component.literal("You can't do it because this player has no join request to your team"));
                                return 1;
                            }
                            InviteSystem.rejectInvite(huy, team.get());
                            ctx.getSource().sendSuccess(() -> Component.literal("You rejected join request of ").append(PlayerManager.getDisplayName(huy).orElseThrow()), false);
                            PlayerManager.getServerPlayer(huy).ifPresent(p -> p.sendSystemMessage(Component.literal("Your join request to team ").append(TeamManager.getPlayerTeam(team.get()).getDisplayName()).append(" was rejected")));
                            return 1;
                        }))
        );
        registerer.register(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            var inviterOpt = player(ctx);
                            if (inviterOpt.isEmpty())
                                return 1;
                            var inviter = inviterOpt.get();
                            var invitedSp = EntityArgument.getPlayer(ctx, "player");
                            var invited = PlayerManager.getEntity(invitedSp);
                            if (invited == inviter) {
                                ctx.getSource().sendFailure(Component.literal("You cannot invite yourself"));
                                return 1;
                            }
                            var teamOpt = team(ctx);
                            var server = server(ctx);
                            var teamEntity = teamOpt.orElseGet(() -> {
                                var scoreboard = server.getScoreboard();
                                var base = PlayerManager.getName(inviter).orElse("team");
                                String name = base;
                                int i = 1;
                                while (scoreboard.getPlayerTeam(name) != null) name = base + "_" + (i++);
                                var pTeam = scoreboard.addPlayerTeam(name);
                                var entity = teamManager(ctx).getEntity(pTeam);
                                TeamOwner.setTeamOwner(entity, inviter);
                                TeamManager.forceAddPlayer(entity, inviter);
                                return entity;
                            });
                            InviteSystem.invitePlayerToTeam(invited, teamEntity, 1000);
                            PlayerManager.getServerPlayer(invited).ifPresent(p -> p.sendSystemMessage(
                                    Component.empty()
                                            .append(PlayerManager.getDisplayName(inviter).orElseThrow())
                                            .append(Component.literal(" invites you to team "))
                                            .append(TeamManager.getPlayerTeam(teamEntity).getDisplayName())
                                            .append(" ")
                                            .append(Component.literal("[Accept]").withStyle(Style.EMPTY.withColor(0x00FF00).withClickEvent(new ClickEvent.RunCommand("/teams join " + TeamManager.getPlayerTeam(teamEntity).getName()))))
                                            .append(" ")
                                            .append(Component.literal("[Reject]").withStyle(Style.EMPTY.withColor(0xFF0000).withClickEvent(new ClickEvent.RunCommand("/teams reject " + TeamManager.getPlayerTeam(teamEntity).getName()))))
                            ));
                            ctx.getSource().sendSuccess(() -> Component.literal("Invite sent"), false);
                            return 1;
                        })));

        registerer.register(Commands.literal("tryLeave")
                .executes(ctx -> {
                    if (!checkTeam(ctx)) return 1;
                    ctx.getSource().sendSuccess(() -> Component.literal("Are you sure? ")
                            .append(Component.literal("[Leave]").withStyle(ChatFormatting.RED)
                                    .withStyle(Style.EMPTY
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to leave your team")))
                                            .withClickEvent(new ClickEvent.RunCommand("/teams leave")))), false);
                    return 1;
                }));

        registerer.register(Commands.literal("tryKick")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            if (!checkTeam(ctx)) return 1;
                            if (!checkOwner(ctx)) return 1;
                            var target = PlayerManager.getEntity(EntityArgument.getPlayer(ctx, "player"));
                            var nameComp = PlayerManager.getDisplayName(target).orElse(Component.literal("Unknown"));
                            var hover = new HoverEvent.ShowText(Component.literal("Click to kick ").append(nameComp.copy()).append(" from your team"));
                            var click = new ClickEvent.RunCommand("/teams kick " + PlayerManager.getName(target).orElse(""));
                            MutableComponent action = Component.literal(" [Kick ").append(nameComp).append(" ]").withStyle(ChatFormatting.RED)
                                    .withStyle(style -> style.withHoverEvent(hover).withClickEvent(click));
                            ctx.getSource().sendSuccess(() -> Component.literal("Are you sure? ").append(action), false);
                            return 1;
                        })));

        registerer.register(Commands.literal("edit")
                .then(Commands.literal("name")
                        .then(Commands.argument("name", ComponentArgument.textComponent(registryAccess))
                                .executes(ctx -> {
                                    if (!checkTeam(ctx)) return 1;
                                    if (!checkOwner(ctx)) return 1;
                                    if (!TournamentUtils.changeTeamName(team(ctx).orElseThrow(), ComponentArgument.getResolvedComponent(ctx, "name"))) {
                                        ctx.getSource().sendFailure(Component.literal("Team with this name already exists"));
                                    }
                                    updateSidebars(ctx);
                                    return 1;
                                }))));

        registerer.register(Commands.literal("acceptAll")
                .then(Commands.argument("acceptAll", BoolArgumentType.bool())
                        .executes(ctx -> {
                            if (!checkTeam(ctx)) return 1;
                            if (!checkOwner(ctx)) return 1;
                            var t = team(ctx).orElseThrow();
                            TeamSettings.getSettings(t).acceptAll = BoolArgumentType.getBool(ctx, "acceptAll");
                            return 1;
                        })));

        registerer.register(Commands.literal("kick")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            if (!checkTeam(ctx)) return 1;
                            if (!checkOwner(ctx)) return 1;
                            var t = team(ctx).orElseThrow();
                            var target = PlayerManager.getEntity(EntityArgument.getPlayer(ctx, "player"));
                            var result = TeamManager.tryKickPlayerFromTeam(t, target);
                            if (!result.isSuccess()) {
                                ctx.getSource().sendFailure(Component.literal("Unable to kick player"));
                                return 1;
                            }
                            PlayerManager.getServerPlayer(target).ifPresent(p -> p.sendSystemMessage(Component.literal("You were kicked from team")));
                            TeamManager.getPlayers(t).forEach(member -> {
                                if (member != target) {
                                    PlayerManager.getServerPlayer(member).ifPresent(p -> p.sendSystemMessage(Component.empty()
                                            .append(PlayerManager.getDisplayName(target).orElse(Component.literal("Unknown")))
                                            .append(" was kicked from team")));
                                }
                            });
                            updateSidebars(ctx);
                            return 1;
                        })));
        registerer.register(Commands.literal("removeAll")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                .executes(ctx -> {
                    var server = ctx.getSource().getServer();
                    var scoreboard = server.getScoreboard();
                    for (var team : scoreboard.getPlayerTeams()) {
                        scoreboard.removePlayerTeam(team);
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal("All teams removed"), true);
                    return Command.SINGLE_SUCCESS;
                }));
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

    private static void sendJoinPlayer(CommandContext<CommandSourceStack> ctx, Entity player, Entity team) {
        PlayerManager.getServerPlayer(player).ifPresent(p -> p.sendSystemMessage(Component.literal("You joined team ").append(TeamManager.getPlayerTeam(team).getDisplayName())));
        for (var player1 : TeamManager.getPlayers(team)) {
            if (player1 == player)
                continue;
            PlayerManager.getServerPlayer(player1).ifPresent(p -> p.sendSystemMessage(Component.empty().append(PlayerManager.getDisplayName(player1).orElseThrow()).append(Component.literal(" joined your team"))));
        }
    }

    private static boolean checkTeam(CommandContext<CommandSourceStack> ctx) {
        if (team(ctx).isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("You're not on the team."));
            return false;
        }
        return true;
    }

    private static boolean checkOwner(CommandContext<CommandSourceStack> ctx) {
        var team = team(ctx);
        var player = player(ctx);
        if (team.isEmpty() || player.isEmpty())
            return false;
        return TeamOwner.getTeamOwner(team.get()) == player.get();
    }

    private static Optional<Entity> player(CommandContext<CommandSourceStack> ctx) {
        var player = ctx.getSource().getPlayer();
        if (player == null)
            return Optional.empty();
        return Optional.of(PlayerManager.getEntity(player));
    }

    private static Optional<Entity> team(CommandContext<CommandSourceStack> ctx) {
        var name = player(ctx).flatMap(PlayerManager::getName);
        if (name.isEmpty())
            return Optional.empty();
        var playerTeam = server(ctx).getScoreboard().getPlayersTeam(name.get());
        if (playerTeam == null)
            return Optional.empty();
        return Optional.of(teamManager(ctx).getEntity(playerTeam));
    }

    public static MinecraftServer server(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getServer();
    }

    public static PlayerManager playerManager(CommandContext<CommandSourceStack> ctx) {
        return PlayerManager.get(server(ctx));
    }

    public static TeamManager teamManager(CommandContext<CommandSourceStack> ctx) {
        return TeamManager.get(server(ctx));
    }

    private static void updateSidebars(CommandContext<CommandSourceStack> ctx) {
        var tournament = Tournament.get(ctx.getSource().getServer());
        if (tournament != null)
            tournament.updateSidebars();
    }
}
