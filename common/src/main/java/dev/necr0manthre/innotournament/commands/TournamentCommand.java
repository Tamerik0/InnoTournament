package dev.necr0manthre.innotournament.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import dev.necr0manthre.innotournament.tournament.Tournament;
import dev.necr0manthre.innotournament.tournament.TournamentData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class TournamentCommand {
	private static Path selectedTournamentPath;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		dispatcher.register(Commands.literal("tournament")
				                    .then(Commands.literal("select")
						                          .then(Commands.argument("filePath", StringArgumentType.string())
								                                .suggests((context, builder) -> {
									                                var path = Platform.getConfigFolder().resolve("inno/tournaments");
									                                if (!Files.exists(path)) {
										                                try {
											                                Files.createDirectory(path);
										                                } catch (IOException ignored) {
										                                }
									                                }
									                                try (var list = Files.list(path)) {
										                                list.forEach(p -> {
											                                if (p.toString().endsWith(".json"))
												                                builder.suggest(p.getFileName().toString());
										                                });
									                                } catch (IOException ignored) {
									                                }
									                                return builder.buildFuture();
								                                }).executes(ctx -> {
									                          String filePath = StringArgumentType.getString(ctx, "filePath");
									                          selectedTournamentPath = Platform.getConfigFolder().resolve("inno/tournaments").resolve(filePath);
									                          return 1;
								                          })))
				                    .then(Commands.literal("prepare")
						                          .executes(ctx -> {
									                          if (Tournament.get(ctx.getSource().getServer()) == null)
										                          useSelectedData(ctx, selectedData -> {
										                          });
									                          if (Tournament.get(ctx.getSource().getServer()) != null)
										                          try {
											                          Tournament.get(ctx.getSource().getServer()).prepare(ctx.getSource().getServer());
											                          ctx.getSource().sendSuccess(() -> Component.literal("Tournament prepared"), true);
										                          } catch (Exception e) {
											                          ctx.getSource().sendFailure(Component.literal("Something went wrong while preparing tournament: " + e.getMessage()));
											                          ctx.getSource().sendSystemMessage(Component.literal("Reseting tournament..."));
											                          try {
												                          Tournament.setTournament(ctx.getSource().getServer(), null);
												                          useSelectedData(ctx, selectedData -> {
												                          });
												                          Tournament.get(ctx.getSource().getServer()).prepare(ctx.getSource().getServer());
												                          ctx.getSource().sendSuccess(() -> Component.literal("Tournament was prepared!!!"), true);
											                          } catch (Exception e2) {
												                          ctx.getSource().sendFailure(Component.literal("Something went wrong, yes, again: " + e2.getMessage()));
											                          }
										                          }
									                          return 1;
								                          }
						                          ))
				                    .then(Commands.literal("start")
						                          .executes(ctx -> {
							                          if (selectedTournamentPath == null) {
								                          ctx.getSource().sendFailure(Component.literal("No tournament selected"));
								                          return 1;
							                          } else if (Tournament.get(ctx.getSource().getServer()) == null) {
								                          useSelectedData(ctx, selectedData -> {
								                          });
								                          if (Tournament.get(ctx.getSource().getServer()) != null)
									                          Tournament.get(ctx.getSource().getServer()).prepare(ctx.getSource().getServer());
							                          }
							                          try {
								                          Tournament.get(ctx.getSource().getServer()).start();
								                          ctx.getSource().sendSuccess(() -> Component.literal("Tournament started"), true);
							                          } catch (Exception e) {
								                          ctx.getSource().sendFailure(Component.literal("Something went wrong while starting tournament: " + e.getMessage()));
								                          ctx.getSource().sendSystemMessage(Component.literal("Reseting tournament..."));
								                          try {
									                          Tournament.setTournament(ctx.getSource().getServer(), null);
									                          useSelectedData(ctx, selectedData -> {
									                          });
									                          Tournament.get(ctx.getSource().getServer()).prepare(ctx.getSource().getServer());
									                          Tournament.get(ctx.getSource().getServer()).start();
									                          ctx.getSource().sendSuccess(() -> Component.literal("Tournament started!!!"), true);
								                          } catch (Exception e2) {
									                          ctx.getSource().sendFailure(Component.literal("Something went wrong, yes, again: " + e2.getMessage()));
								                          }
							                          }
							                          return 1;
						                          }))
				                    .then(Commands.literal("stop")
						                          .executes(ctx -> {
							                          Tournament.setTournament(ctx.getSource().getServer(), null);
							                          return 1;
						                          }))
				                    .then(Commands.literal("set")
						                          .then(Commands.literal("tournamentPreSpawn")
								                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> selectedData.tournamentPreSpawn = BlockPosArgument.getBlockPos(ctx, "pos"))
										                                      )))
						                          .then(Commands.literal("tournamentPreSpawnDimension")
								                                .then(Commands.argument("dimension", DimensionArgument.dimension())
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> {
											                                      try {
												                                      selectedData.tournamentPreSpawnDimension = DimensionArgument.getDimension(ctx, "dimension").dimension();
											                                      } catch (CommandSyntaxException e) {
												                                      throw new RuntimeException(e);
											                                      }
										                                      }))
								                                ))
						                          .then(Commands.literal("tournamentSpawn")
								                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> selectedData.tournamentSpawn = BlockPosArgument.getBlockPos(ctx, "pos"))
										                                      )))
						                          .then(Commands.literal("tournamentSpawnDimension")
								                                .then(Commands.argument("dimension", DimensionArgument.dimension())
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> {
											                                      try {
												                                      selectedData.tournamentSpawnDimension = DimensionArgument.getDimension(ctx, "dimension").dimension();
											                                      } catch (CommandSyntaxException e) {
												                                      throw new RuntimeException(e);
											                                      }
										                                      }))
								                                ))
						                          .then(Commands.literal("startBoxStructureResourceLocation")
								                                .then(Commands.argument("template", ResourceLocationArgument.id()).suggests((commandContext, suggestionsBuilder) -> {
											                                StructureTemplateManager structureTemplateManager = (commandContext.getSource()).getLevel().getStructureManager();
											                                return SharedSuggestionProvider.suggestResource(structureTemplateManager.listTemplates(), suggestionsBuilder);
										                                })
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> {
											                                      selectedData.startBoxStructureResourceLocation = ResourceLocationArgument.getId(ctx, "template");
										                                      }))))
						                          .then(Commands.literal("startBoxPos")
								                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
										                                      .executes(ctx -> useSelectedData(ctx, selectedData -> selectedData.startBoxPos = BlockPosArgument.getBlockPos(ctx, "pos"))
										                                      ))))
				                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4)));

	}

	private static int useSelectedData(CommandContext<CommandSourceStack> ctx, Consumer<TournamentData> action) {
		if (selectedTournamentPath == null) {
			ctx.getSource().sendFailure(Component.literal("No tournament selected"));
			return 1;
		}
		if (!Files.exists(selectedTournamentPath)) {
			ctx.getSource().sendFailure(Component.literal("Selected tournament does not exist"));
			return 1;
		}
		TournamentData selectedData;
		try {
			var dataResult = TournamentData.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(Files.newBufferedReader(selectedTournamentPath)));
			selectedData = dataResult.getPartialOrThrow().getFirst();
			if (dataResult.error().isPresent()) {
				ctx.getSource().sendFailure(Component.literal("Something got wrong while reading tournament data: " + dataResult.error().get().message()));
				ctx.getSource().sendSystemMessage(Component.literal("... but anyway data was loaded"));
			}
		} catch (IOException e) {
			ctx.getSource().sendFailure(Component.literal("Cannot read tournament data: " + e.getMessage()));
			return 1;
		}
		action.accept(selectedData);
		var json = TournamentData.CODEC.encode(selectedData, JsonOps.INSTANCE, new JsonObject()).getOrThrow();
		try {
			Files.writeString(selectedTournamentPath, json.toString());
			if (Tournament.get(ctx.getSource().getServer()) == null || Tournament.get(ctx.getSource().getServer()).getPhase() == 0)
				Tournament.setTournament(ctx.getSource().getServer(), selectedData.createTournament(selectedTournamentPath));
		} catch (IOException e) {
			ctx.getSource().sendFailure(Component.literal("Cannot write tournament data: " + e.getMessage()));
		}
		return 1;
	}

}
