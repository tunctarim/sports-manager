package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.EndCondition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseSportTest {
    @Test
    void testBaseSportLogic() {
        // Creating a quick anonymous class for testing
        BaseSport mockSport = new BaseSport("Test", 3, 1, 11, 2, 40, EndCondition.TIME_LIMIT, 15) {
            @Override
            public List<String> getRequiredStats() {
                return List.of("Speed");
            }
        };

        assertEquals(80, mockSport.getTotalMatchLength(), "Total length should be segments * limit");
        assertEquals("Test", mockSport.getSportName());
    }
}
