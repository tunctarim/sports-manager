package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.EndCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BaseSportTest {

    private static class MockSport extends BaseSport {
        public MockSport(String name, int ppW, int ppD, int roster, int segments, int limit, EndCondition condition, int tick) {
            super(name, ppW, ppD, roster, segments, limit, condition, tick);
        }

        @Override
        public List<String> getRequiredStats() {
            return List.of("Speed", "Stamina");
        }
    }

    @Test
    public void testStandardLogic() {
        BaseSport p = new MockSport("Football", 3, 1, 11, 2, 45, EndCondition.TIME_LIMIT, 30);

        assertEquals(90, p.getTotalMatchLength());
        assertEquals("Football", p.getSportName());
    }

    @Test
    public void testBoundaryValues() {
        BaseSport p = new MockSport("SinglePeriod", 3, 1, 11, 1, 10, EndCondition.TIME_LIMIT, 1);
        assertEquals(10, p.getTotalMatchLength());

        p = new MockSport("Instant", 3, 0, 1, 1, 0, EndCondition.TIME_LIMIT, 1);
        assertEquals(0, p.getTotalMatchLength());
    }

    @Test
    public void testNegativeAndInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MockSport("", 3, 1, 11, 2, 45, EndCondition.TIME_LIMIT, 30);
        }, "Should fail if name is empty");

        assertThrows(IllegalArgumentException.class, () -> {
            new MockSport("Test", 3, 1, -1, 2, 45, EndCondition.TIME_LIMIT, 30);
        }, "Should fail for negative roster size [cite: 650]");

        assertThrows(IllegalArgumentException.class, () -> {
            new MockSport("Test", 3, 1, 11, 0, 45, EndCondition.TIME_LIMIT, 30);
        }, "Should fail for zero segments");
    }

    @Test
    public void testRequiredStats() {
        BaseSport p = new MockSport("Mock", 3, 1, 11, 2, 45, EndCondition.TIME_LIMIT, 30);
        List<String> stats = p.getRequiredStats();

        assertEquals(2, stats.size());
        assertTrue(stats.contains("Speed"));
        assertTrue(stats.contains("Stamina"));
    }
}