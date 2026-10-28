package no.oskott.towerdefbackend.run;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class RunDtos {

    private RunDtos() {
    }

    public record StartResponse(UUID runId, String token) {
    }

    public record ScoreRequest(
            @NotBlank String token,
            @NotBlank @Size(min = 1, max = 16) @Pattern(regexp = "[\\p{L}\\p{N} _.\\-]+") String name,
            @Min(1) @Max(100_000) int wave,
            @Min(0) @Max(100_000_000) int kills,
            @NotBlank @Size(max = 32) String topTowerId,
            @Min(0) @Max(2000) int topTowerLevel,
            @Min(0) @Max(1000) Integer achievements,
            @Min(1) @Max(1000) Integer gameVersion) {
    }

    public record ScoreResponse(long rank) {
    }
}
