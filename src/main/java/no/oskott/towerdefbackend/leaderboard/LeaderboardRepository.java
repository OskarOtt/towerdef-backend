package no.oskott.towerdefbackend.leaderboard;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findByRunId(UUID runId);

    @Query("select e from LeaderboardEntry e order by e.wave desc, e.kills desc, e.createdAt asc")
    List<LeaderboardEntry> top(Pageable pageable);

    @Query("""
            select count(e) from LeaderboardEntry e
            where e.wave > :wave
               or (e.wave = :wave and e.kills > :kills)
               or (e.wave = :wave and e.kills = :kills and e.createdAt < :createdAt)
            """)
    long countAhead(int wave, int kills, Instant createdAt);
}
