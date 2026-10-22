package no.oskott.towerdefbackend.run;

import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Sanity checks mirroring the frontend game constants (towerdef-io/src/game/constants.ts, upgrades.ts).
 * Cannot prove a score is genuine, only reject impossible ones.
 */
@Service
public class PlausibilityService {

    static final Map<String, Integer> TOWER_COSTS = Map.of(
            "blaster", 100, "cannon", 200, "laser", 300, "ray", 400, "god", 1000, "money-printer", 500);

    static final int INITIAL_GOLD = 250;
    static final int INITIAL_LIVES = 20;
    static final double SPAWN_INTERVAL = 0.7;
    static final int MAX_GAME_SPEED = 3;
    static final int MAX_REWARD_PER_KILL = 12;
    static final int MAX_WAVE = 100_000;
    static final int UPGRADE_STAT_COUNT = 5;

    static int enemiesForWave(int wave) {
        return 6 + (int) Math.floor(wave * 1.5);
    }

    static long enemiesThroughWave(int wave) {
        long total = 0;
        for (int w = 1; w <= wave; w++) {
            total += enemiesForWave(w);
        }
        return total;
    }

    /** Returns null if plausible, otherwise a short reason. */
    public String check(int wave, int kills, String towerId, int towerLevel, double elapsedSeconds) {
        Integer towerCost = TOWER_COSTS.get(towerId);
        if (towerCost == null) {
            return "unknown_tower";
        }
        if (wave < 1 || wave > MAX_WAVE || kills < 0) {
            return "out_of_range";
        }
        // Deliberately lenient: only reject clearly impossible values.
        if (kills > enemiesThroughWave(wave) * 2) {
            return "too_many_kills";
        }
        if (towerLevel < 0 || towerLevel > 2000) {
            return "implausible_tower_level";
        }
        return null;
    }

    /** Minimum gold needed to buy the tower and `levels` upgrades, spread evenly over all stats. */
    static double cheapestSpend(int towerCost, int levels) {
        double total = towerCost;
        for (int n = 0; n < levels; n++) {
            int statLevel = n / UPGRADE_STAT_COUNT;
            total += Math.round(towerCost * 0.5 * Math.pow(1.5, statLevel));
            if (Double.isInfinite(total)) {
                break;
            }
        }
        return total;
    }
}
