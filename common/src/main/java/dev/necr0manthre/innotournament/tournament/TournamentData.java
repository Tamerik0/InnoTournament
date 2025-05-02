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
import java.util.stream.Stream;

@AllArgsConstructor
public class TournamentData implements Cloneable {
	public static final Codec<TournamentData> CODEC =
			new MapCodec<TournamentData>() {

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
									"mainEventsPath")
							       .map(ops::createString);
				}

				@Override
				public <T> DataResult<TournamentData> decode(DynamicOps<T> ops, MapLike<T> input) {
					var tournamentData = new TournamentData();
					try {
						tournamentData.tournamentPreSpawn = BlockPos.CODEC.decode(ops, input.get("tournamentPreSpawn")).getOrThrow().getFirst();
						tournamentData.tournamentPreSpawnDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.CODEC.decode(ops, input.get("tournamentPreSpawnDimension")).getOrThrow().getFirst());
						tournamentData.tournamentSpawn = BlockPos.CODEC.decode(ops, input.get("tournamentSpawn")).getOrThrow().getFirst();
						tournamentData.tournamentSpawnDimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.CODEC.decode(ops, input.get("tournamentSpawnDimension")).getOrThrow().getFirst());
						tournamentData.startBoxStructureResourceLocation = ResourceLocation.CODEC.decode(ops, input.get("startBoxStructureResourceLocation")).getOrThrow().getFirst();
						tournamentData.startBoxPos = BlockPos.CODEC.decode(ops, input.get("startBoxPos")).getOrThrow().getFirst();
						tournamentData.preStartGameMode = GameType.CODEC.decode(ops, input.get("preStartGameMode")).getOrThrow().getFirst();
						tournamentData.preEventsPath = Path.of(Codec.STRING.decode(ops, input.get("preEventsPath")).getOrThrow().getFirst());
						tournamentData.mainEventsPath = Path.of(Codec.STRING.decode(ops, input.get("mainEventsPath")).getOrThrow().getFirst());
						return DataResult.success(tournamentData);
					} catch (Exception e) {
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
				EventLoader.loadEvents(tournamentDataPath.getParent().resolve(mainEventsPath)));
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
				mainEventsPath);
	}
}
