package dev.necr0manthre.innotournament.teams;

public enum PlayerKickResult {
    SUCCESS,
    CANCELLED,
    NOT_IN_TEAM;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
