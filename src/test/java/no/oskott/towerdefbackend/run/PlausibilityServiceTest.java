package no.oskott.towerdefbackend.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PlausibilityServiceTest {

    private final PlausibilityService s = new PlausibilityService();

    @Test
    void acceptsReasonableRun() {
        // waves 1..9 => 9 waves of ~ (6+1.5w) enemies
        assertNull(s.check(10, 120, "blaster", 5, 600));
    }

    @Test
    void rejectsUnknownTower() {
        assertEquals("unknown_tower", s.check(1, 0, "nope", 0, 10));
    }

    @Test
    void rejectsTooManyKills() {
        assertEquals("too_many_kills", s.check(2, 1000, "blaster", 0, 600));
    }

    @Test
    void acceptsLowKillCount() {
        assertNull(s.check(50, 0, "blaster", 0, 100_000));
    }

    @Test
    void doesNotRejectOnElapsedTime() {
        org.junit.jupiter.api.Assertions.assertNull(s.check(30, 800, "blaster", 0, 5));
    }

    @Test
    void rejectsHugeTowerLevel() {
        assertEquals("implausible_tower_level", s.check(5, 40, "blaster", 2001, 600));
    }
}
