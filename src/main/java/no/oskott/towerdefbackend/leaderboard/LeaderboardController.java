package no.oskott.towerdefbackend.leaderboard;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    public record Row(int rank, String name, int wave, int kills, String topTowerId, int topTowerLevel,
                      int achievements) {
    }

    private final LeaderboardRepository repo;

    public LeaderboardController(LeaderboardRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Row> top(@RequestParam(defaultValue = "10") int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        List<LeaderboardEntry> entries = repo.top(PageRequest.of(0, size));
        Row[] rows = new Row[entries.size()];
        for (int i = 0; i < rows.length; i++) {
            LeaderboardEntry e = entries.get(i);
            rows[i] = new Row(i + 1, e.getName(), e.getWave(), e.getKills(), e.getTopTowerId(), e.getTopTowerLevel(),
                    e.getAchievements());
        }
        return List.of(rows);
    }

    @GetMapping("/run/{runId}")
    public Row placement(@PathVariable UUID runId) {
        LeaderboardEntry e = repo.findByRunId(runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no_entry"));
        int rank = (int) repo.countAhead(e.getWave(), e.getKills(), e.getCreatedAt()) + 1;
        return new Row(rank, e.getName(), e.getWave(), e.getKills(), e.getTopTowerId(), e.getTopTowerLevel(),
                e.getAchievements());
    }
}
