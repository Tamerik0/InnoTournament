package dev.necr0manthre.innotournament.teams;

public class PlayerJoinResult {

    public static final PlayerJoinResult SUCCESS = new PlayerJoinResult(null);
    public static final PlayerJoinResult ALREADY_IN_TEAM = new PlayerJoinResult(null);
    public static final PlayerJoinResult CANCELLED = new PlayerJoinResult(null);
    private final PlayerKickResult kickResult;

    private PlayerJoinResult(PlayerKickResult kickResult) {
        this.kickResult = kickResult;
    }

    public static PlayerJoinResult cannotKick(PlayerKickResult kickResult) {
        return new PlayerJoinResult(kickResult);
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
