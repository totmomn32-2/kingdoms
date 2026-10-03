package dev.kingdoms;

public record Quest(
        String id,
        String name,
        QuestType type,
        String target,
        long amount,
        int minContributors,
        long points,
        long coins,
        long core
) {
}
