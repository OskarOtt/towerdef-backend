package no.oskott.towerdefbackend.run;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import no.oskott.towerdefbackend.config.TowerdefProperties;
import no.oskott.towerdefbackend.leaderboard.LeaderboardEntry;
import no.oskott.towerdefbackend.leaderboard.LeaderboardRepository;
import no.oskott.towerdefbackend.run.RunDtos.ScoreRequest;
import no.oskott.towerdefbackend.run.RunDtos.ScoreResponse;
import no.oskott.towerdefbackend.run.RunDtos.StartResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RunService {

    private static final Set<String> BLOCKED_WORDS = Set.of("fuck", "shit", "nigger", "nigga", "cunt", "faggot");

    private final RunRepository runs;
    private final LeaderboardRepository leaderboard;
    private final RunTokenService tokens;
    private final PlausibilityService plausibility;
    private final TowerdefProperties props;
    private final Clock clock;

    public RunService(RunRepository runs, LeaderboardRepository leaderboard, RunTokenService tokens,
                      PlausibilityService plausibility, TowerdefProperties props, Clock clock) {
        this.runs = runs;
        this.leaderboard = leaderboard;
        this.tokens = tokens;
        this.plausibility = plausibility;
        this.props = props;
        this.clock = clock;
    }

    public StartResponse start(String ipHash) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        Run run = runs.save(new Run(UUID.randomUUID(), now, ipHash));
        return new StartResponse(run.getId(), tokens.create(run.getId(), run.getStartedAt()));
    }

    /**
     * A run may submit repeatedly; it keeps a single leaderboard entry that only ever improves.
     * The run expires after {@code runTtlMinutes} without a submission.
     */
    public ScoreResponse submit(UUID runId, ScoreRequest req) {
        Run run = runs.findById(runId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid_token"));
        if (!tokens.verify(run.getId(), run.getStartedAt(), req.token())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid_token");
        }
        Instant now = clock.instant();
        Duration elapsed = Duration.between(run.getStartedAt(), now);
        Instant lastActive = run.getLastSubmitAt() != null ? run.getLastSubmitAt() : run.getStartedAt();
        if (Duration.between(lastActive, now).toMinutes() > props.security().runTtlMinutes()) {
            throw new ResponseStatusException(HttpStatus.GONE, "run_expired");
        }

        String name = req.name().trim();
        if (name.isEmpty() || isBlocked(name)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "invalid_name");
        }
        String reason = plausibility.check(req.wave(), req.kills(), req.topTowerId(), req.topTowerLevel(),
                elapsed.toMillis() / 1000.0);
        if (reason != null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, reason);
        }

        Instant created = now.truncatedTo(ChronoUnit.MILLIS);
        run.setLastSubmitAt(created);
        runs.save(run);
        LeaderboardEntry entry = leaderboard.findByRunId(runId).orElse(null);
        if (entry == null) {
            entry = new LeaderboardEntry(runId, name, req.wave(), req.kills(), req.topTowerId(),
                    req.topTowerLevel(), req.gameVersion() == null ? 1 : req.gameVersion(), created);
        } else if (entry.isBetterOrEqual(req.wave(), req.kills())) {
            entry.update(name, req.wave(), req.kills(), req.topTowerId(), req.topTowerLevel());
        }
        entry.raiseAchievements(req.achievements() == null ? 0 : req.achievements());
        entry = leaderboard.save(entry);
        long rank = leaderboard.countAhead(entry.getWave(), entry.getKills(), entry.getCreatedAt()) + 1;
        return new ScoreResponse(rank);
    }

    private static boolean isBlocked(String name) {
        String normalized = name.toLowerCase().replaceAll("[^\\p{L}]", "");
        return BLOCKED_WORDS.stream().anyMatch(normalized::contains);
    }
}
