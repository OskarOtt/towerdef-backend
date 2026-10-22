package no.oskott.towerdefbackend.run;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "runs")
public class Run {

    @Id
    private UUID id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ip_hash", nullable = false)
    private String ipHash;

    @Column(nullable = false)
    private boolean consumed;

    @Column(name = "last_submit_at")
    private Instant lastSubmitAt;

    protected Run() {
    }

    public Run(UUID id, Instant startedAt, String ipHash) {
        this.id = id;
        this.startedAt = startedAt;
        this.ipHash = ipHash;
    }

    public UUID getId() {
        return id;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public String getIpHash() {
        return ipHash;
    }

    public Instant getLastSubmitAt() {
        return lastSubmitAt;
    }

    public void setLastSubmitAt(Instant lastSubmitAt) {
        this.lastSubmitAt = lastSubmitAt;
    }

    public boolean isConsumed() {
        return consumed;
    }
}
