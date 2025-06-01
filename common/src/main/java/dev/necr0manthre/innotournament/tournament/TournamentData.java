package dev.necr0manthre.innotournament.tournament;

import com.mojang.serialization.*;
import dev.necr0manthre.innotournament.tournament_events.EventLoader;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class TournamentData implements Cloneable {
	public static final Codec<TournamentData> CODEC =
			new MapCodec<TournamentData>() {
				private Queue<Throwable> errors = new ArrayDeque<>();

				private <T> Optional<T> getAndCollectErrors(Supplier<T> supplier) {
					try {
						return Optional.ofNullable(supplier.get());
					} catch (Throwable e) {
						errors.add(e);
						return Optional.empty();
					}
				}

				private <T, V> Optional<T> getAndCollectErrors(Codec<T> codec, DynamicOps<V> ops, MapLike<V> map, String key) {
					return getAndCollectErrors(() -> codec.decode(ops, map.get(key)).getOrThrow().getFirst());
				}

				private void throwAll() throws Throwable {
					if (errors.isEmpty())
						return;
					var errors = this.errors.toArray(Throwable[]::new);
					this.errors.clear();
					if (errors.length == 1)
						throw errors[0];
					throw new RuntimeException("Multiple errors occurred: " + Arrays.stream(errors).map(Throwable::getMessage).collect(Collectors.joining("\n")));
				}

				@Override
				public <T> Stream<T> keys(DynamicOps<T> ops) {
					return Stream.of(
									"tournamentPreSpawn",
									"tournamentPreSpawnDimension",
									"tournamentSpawn",
									"tournamentSpawnDimension",
									"startBoxStructureResourceLocation",
									"startBoxPos",
									"preStartGameMode",
									"preEventsPath",
									"mainEventsPath",
									"teamScoreModifiers")
							       .map(ops::createString);
				}

				@Override
				public <T> DataResult<TournamentData> decode(DynamicOps<T> ops, MapLike<T> input) {
					var tournamentData = new TournamentData();
					try {
						tournamentData.tournamentPreSpawn = getAndCollectErrors(BlockPos.CODEC, ops, input, "tournamentPreSpawn").orElse(BlockPos.ZERO);
						tournamentData.tournamentPreSpawnDimension = getAndCollectErrors(ResourceLocation.CODEC, ops, input, "tournamentPreSpawnDimension").map(rl -> ResourceKey.create(Registries.DIMENSION, rl)).orElse(Level.OVERWORLD);
						tournamentData.tournamentSpawn = getAndCollectErrors(BlockPos.CODEC, ops, input, "tournamentSpawn").orElse(BlockPos.ZERO);
						tournamentData.tournamentSpawnDimension = getAndCollectErrors(ResourceLocation.CODEC, ops, input, "tournamentSpawnDimension").map(rl -> ResourceKey.create(Registries.DIMENSION, rl)).orElse(Level.OVERWORLD);
						tournamentData.startBoxStructureResourceLocation = getAndCollectErrors(ResourceLocation.CODEC, ops, input, "startBoxStructureResourceLocation").orElse(null);
						tournamentData.startBoxPos = getAndCollectErrors(BlockPos.CODEC, ops, input, "startBoxPos").orElse(BlockPos.ZERO);
						tournamentData.preStartGameMode = getAndCollectErrors(GameType.CODEC, ops, input, "preStartGameMode").orElse(GameType.SURVIVAL);
						tournamentData.preEventsPath = getAndCollectErrors(Codec.STRING, ops, input, "preEventsPath").map(Path::of).orElse(Path.of("/"));
						tournamentData.mainEventsPath = getAndCollectErrors(Codec.STRING, ops, input, "mainEventsPath").map(Path::of).orElse(Path.of("/"));
						tournamentData.teamScoreModifiers = getAndCollectErrors(() -> ops.getStream(input.get("teamScoreModifiers")).getOrThrow().mapToDouble(v -> ops.getNumberValue(v).getOrThrow().doubleValue()).toArray()).orElse(tournamentData.teamScoreModifiers);
						tournamentData.lastTeamScore = getAndCollectErrors(Codec.FLOAT, ops, input, "lastTeamScore").orElse(0f);
						throwAll();
						return DataResult.success(tournamentData);
					} catch (Throwable e) {
						return DataResult.error(e::getMessage, tournamentData);
					}
				}

				@Override
				public <T> RecordBuilder<T> encode(TournamentData input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
					prefix.add("tournamentPreSpawn", input.tournamentPreSpawn, BlockPos.CODEC);
					prefix.add("tournamentPreSpawnDimension", input.tournamentPreSpawnDimension.location(), ResourceLocation.CODEC);
					prefix.add("tournamentSpawn", input.tournamentSpawn, BlockPos.CODEC);
					prefix.add("tournamentSpawnDimension", input.tournamentSpawnDimension.location(), ResourceLocation.CODEC);
					prefix.add("startBoxStructureResourceLocation", input.startBoxStructureResourceLocation, ResourceLocation.CODEC);
					prefix.add("startBoxPos", input.startBoxPos, BlockPos.CODEC);
					prefix.add("preStartGameMode", input.preStartGameMode, GameType.CODEC);
					prefix.add("preEventsPath", input.preEventsPath.toString(), Codec.STRING);
					prefix.add("mainEventsPath", input.mainEventsPath.toString(), Codec.STRING);
					prefix.add("teamScoreModifiers", ops.createList(Arrays.stream(input.teamScoreModifiers).mapToObj(ops::createDouble)));
					prefix.add("lastTeamScore", ops.createFloat(input.lastTeamScore));
					return prefix;
				}
			}.codec();

	public BlockPos tournamentSpawn = BlockPos.ZERO;
	public ResourceKey<Level> tournamentSpawnDimension = Level.OVERWORLD;
	public BlockPos tournamentPreSpawn = BlockPos.ZERO;
	public ResourceKey<Level> tournamentPreSpawnDimension = Level.OVERWORLD;
	public ResourceLocation startBoxStructureResourceLocation = ResourceLocation.fromNamespaceAndPath("", "");
	public BlockPos startBoxPos = BlockPos.ZERO;
	public GameType preStartGameMode = GameType.SURVIVAL;
	public Path preEventsPath = Path.of("");
	public Path mainEventsPath = Path.of("");
	public double[] teamScoreModifiers = new double[]{2.43, 1.45, 1, 0.7241170, 0.4530344, 0.3645};
	public float lastTeamScore = 0;

	private TournamentData() {
	}

	public Tournament createTournament(Path tournamentDataPath) {
		return new Tournament(
				tournamentSpawn,
				tournamentSpawnDimension,
				tournamentPreSpawn,
				tournamentPreSpawnDimension,
				startBoxStructureResourceLocation,
				startBoxPos,
				preStartGameMode,
				EventLoader.loadEvents(tournamentDataPath.getParent().resolve(preEventsPath)),
				EventLoader.loadEvents(tournamentDataPath.getParent().resolve(mainEventsPath)),
				teamScoreModifiers,
				lastTeamScore);
	}

//	public void applyToTournament(Tournament tournament, Path tournamentDataPath){
//		if(tournament.phase!=0)
//			throw new IllegalStateException("Tournament is already started");
//		tournament.tournamentSpawn = tournamentSpawn;
//		tournament.tournamentSpawnDimension = tournamentSpawnDimension;
//		tournament.tournamentPreSpawn = tournamentPreSpawn;
//		tournament.tournamentPreSpawnDimension = tournamentPreSpawnDimension;
//		tournament.startBoxStructureResourceLocation = startBoxStructureResourceLocation;
//		tournament.startBoxPos = startBoxPos;
//		tournament.preStartEvents = EventLoader.loadEvents(tournamentDataPath.getParent().resolve(preEventsPath));
//		tournament.mainEvents = EventLoader.loadEvents(tournamentDataPath.getParent().resolve(mainEventsPath));
//	}

	@Override
	public TournamentData clone() {
		return new TournamentData(
				tournamentSpawn,
				tournamentSpawnDimension,
				tournamentPreSpawn,
				tournamentPreSpawnDimension,
				startBoxStructureResourceLocation,
				startBoxPos,
				preStartGameMode,
				preEventsPath,
				mainEventsPath,
				teamScoreModifiers.clone(),
				lastTeamScore);
	}
}
