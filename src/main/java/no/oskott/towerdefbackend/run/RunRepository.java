package no.oskott.towerdefbackend.run;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RunRepository extends JpaRepository<Run, UUID> {

    /** Atomically marks the run as used; returns 1 only for the first caller. */
    @Modifying
    @Transactional
    @Query("update Run r set r.consumed = true where r.id = :id and r.consumed = false")
    int consume(UUID id);
}
