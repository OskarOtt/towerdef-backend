package no.oskott.towerdefbackend.leaderboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leaderboard_entries")
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true)
    private UUID runId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int wave;

    @Column(nullable = false)
    private int kills;

    @Column(name = "top_tower_id", nullable = false)
    private String topTowerId;

    @Column(name = "top_tower_level", nullable = false)
    private int topTowerLevel;

    @Column(nullable = false)
    private int achievements;

    @Column(name = "game_version", nullable = false)
    private int gameVersion = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LeaderboardEntry() {
    }

    public LeaderboardEntry(UUID runId, String name, int wave, int kills, String topTowerId, int topTowerLevel,
                            int gameVersion, Instant createdAt) {
        this.runId = runId;
        this.name = name;
        this.wave = wave;
        this.kills = kills;
        this.topTowerId = topTowerId;
        this.topTowerLevel = topTowerLevel;
        this.gameVersion = gameVersion;
        this.createdAt = createdAt;
    }

    public void update(String name, int wave, int kills, String topTowerId, int topTowerLevel) {
        this.name = name;
        this.wave = wave;
        this.kills = kills;
        this.topTowerId = topTowerId;
        this.topTowerLevel = topTowerLevel;
    }

    public int getGameVersion() {
        return gameVersion;
    }

    public int getAchievements() {
        return achievements;
    }

    /** Achievements only ever grow, regardless of whether the score improved. */
    public void raiseAchievements(int count) {
        this.achievements = Math.max(this.achievements, count);
    }

    public boolean isBetterOrEqual(int wave, int kills) {
        return wave > this.wave || (wave == this.wave && kills >= this.kills);
    }

    public String getName() {
        return name;
    }

    public int getWave() {
        return wave;
    }

    public int getKills() {
        return kills;
    }

    public String getTopTowerId() {
        return topTowerId;
    }

    public int getTopTowerLevel() {
        return topTowerLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
