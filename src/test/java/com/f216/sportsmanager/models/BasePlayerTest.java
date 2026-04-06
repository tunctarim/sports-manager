package com.f216.sportsmanager.models;

import com.f216.sportsmanager.enums.Gender;
import com.f216.sportsmanager.interfaces.PlayerPosition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class BasePlayerTest {
    enum TestPosition implements PlayerPosition {
        FORWARD("FW", "Forward", false), GOALKEEPER("GK", "Goalkeeper", true), MIDFIELDER("MF", "Midfielder", false);
        private final String code;
        private final String displayName;
        private final boolean goalkeeperRole;

        TestPosition(String code, String displayName, boolean goalkeeperRole) {
            this.code = code;
            this.displayName = displayName;
            this.goalkeeperRole = goalkeeperRole;
        }

        @Override
        public String  getCode(){
            return code;
        }

        @Override
        public String  getDisplayName(){
            return displayName;
        }

        @Override
        public boolean isGoalkeeperRole(){
            return goalkeeperRole;
        }
    }

    static class TestPlayer extends BasePlayer {
        TestPlayer(String name, int age, Gender gender, PlayerPosition position) {
            super(name, age, gender, position);
        }
    }

    private TestPlayer player;

    @BeforeEach
    void setUp() {
        player = new TestPlayer("Ali", 25, Gender.MALE, TestPosition.FORWARD);
        player.setStat("shooting", 80);
        player.setStat("pace", 75);
        player.setStat("dribbling", 70);
        player.setStat("passing", 60);
        player.setStat("defending", 40);
    }

    @Nested
    class ConstructorTests {
        @Test
        void testValidConstructor() {
            assertNotNull(player);
            assertEquals("Ali", player.getName());
            assertEquals(25, player.getAge());
            assertEquals(Gender.MALE, player.getGender());
            assertEquals(TestPosition.FORWARD, player.getPosition());
        }

        @Test
        void testIdIsAutoAssigned() {
            assertNotNull(player.getID());
            assertFalse(player.getID().isBlank());
        }

        @Test
        void testInitialHealthIsMax() {
            assertEquals(100, player.getHealth());
        }

        @Test
        void testInitiallyNotInjured() {
            assertFalse(player.isInjured());
            assertEquals(0, player.getInjuryMatchesRemaining());
        }

        @Test
        void testNullNameThrows() {
            assertThrows(IllegalArgumentException.class, () -> new TestPlayer(null, 25, Gender.MALE, TestPosition.FORWARD));
        }

        @Test
        void testBlankNameThrows() {
            assertThrows(IllegalArgumentException.class, () -> new TestPlayer("   ", 25, Gender.MALE, TestPosition.FORWARD));
        }

        @ParameterizedTest(name = "Invalid age: {0}")
        @ValueSource(ints = {0, -1, -18})
        void testInvalidAgeThrows(int invalidAge) {
            assertThrows(IllegalArgumentException.class, () ->
                    new TestPlayer("Test", invalidAge, Gender.MALE, TestPosition.FORWARD));
        }

        @Test
        void testNullGenderThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TestPlayer("Test", 25, null, TestPosition.FORWARD));
        }

        @Test
        void testNullPositionThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new TestPlayer("Test", 25, Gender.MALE, null));
        }
    }

    @Nested
    class IdentityTests {

        @Test
        void testUniqueIds() {
            TestPlayer other = new TestPlayer("Other Player", 22, Gender.FEMALE, TestPosition.MIDFIELDER);
            assertNotEquals(player.getID(), other.getID());
        }

        @Test
        void testGetName() {
            assertEquals("Ali", player.getName());
        }

        @Test
        void testGetAge() {
            assertEquals(25, player.getAge());
        }

        @Test
        void testGetGender() {
            assertEquals(Gender.MALE, player.getGender());
        }

        @Test
        void testGetGenderFemale() {
            TestPlayer fp = new TestPlayer("Ayşe", 23, Gender.FEMALE, TestPosition.MIDFIELDER);
            assertEquals(Gender.FEMALE, fp.getGender());
        }

        @Test
        void testGetPosition() {
            assertEquals(TestPosition.FORWARD, player.getPosition());
        }

        @Test
        void testToString() {
            String str = player.toString();
            assertTrue(str.contains("Ali "));
            assertTrue(str.contains("FW"));
            assertTrue(str.contains("OVR:"));
            assertTrue(str.contains("HP:"));
        }
    }

    @Nested
    class StatTests {

        @Test
        void testSetAndGetStat() {
            player.setStat("shooting", 85);
            assertEquals(85, player.getStat("shooting"));
        }

        @Test
        void testCaseInsensitiveGet() {
            assertEquals(80, player.getStat("shooting"));
            assertEquals(80, player.getStat("Shooting"));
            assertEquals(80, player.getStat("SHOOTING"));
        }

        @Test
        void testCaseInsensitiveSet() {
            player.setStat("PACE", 90);
            assertEquals(90, player.getStat("pace"));
        }

        @Test
        void testUnknownStatReturnsZero() {
            assertEquals(0, player.getStat("nonexistent"));
        }

        @Test
        void testNullStatNameReturnsZero() {
            assertEquals(0, player.getStat(null));
        }

        @Test
        void testSetNullStatNameThrows() {
            assertThrows(IllegalArgumentException.class, () -> player.setStat(null, 50));
        }

        @Test
        void testSetBlankStatNameThrows() {
            assertThrows(IllegalArgumentException.class, () -> player.setStat("  ", 50));
        }

        @ParameterizedTest(name = "Entrence: {0} → Expected: {1}")
        @CsvSource({
                "101, 100",
                "200, 100",
                "100, 100",
                "50,   50",
                "1,     1",
                "0,     1",
                "-10,   1"
        })

        void testSetStatClamping(int input, int expected) {
            player.setStat("shooting", input);
            assertEquals(expected, player.getStat("shooting"));
        }

        @Test
        void testGetStatsMap() {
            Map<String, Integer> map = player.getStatsMap();
            assertEquals(80, map.get("shooting"));
            assertEquals(75, map.get("pace"));
        }

        @Test
        void testGetStatsMapIsUnmodifiable() {
            assertThrows(UnsupportedOperationException.class,
                    () -> player.getStatsMap().put("hack", 99));
        }

        @Test
        void testSetStatOverwrites() {
            player.setStat("shooting", 95);
            assertEquals(95, player.getStat("shooting"));
        }
    }

    @Nested
    class OverallRatingTests {

        @Test
        void testEmptyStatsRatingIsZero() {
            TestPlayer empty = new TestPlayer("Null", 20, Gender.MALE, TestPosition.FORWARD);
            assertEquals(0, empty.getOverallRating());
        }

        @Test
        void testAverageRating() {
            assertEquals(65, player.getOverallRating());
        }

        @Test
        void testAllMaxRating() {
            TestPlayer max = new TestPlayer("Veli", 25, Gender.MALE, TestPosition.FORWARD);
            max.setStat("a", 100);
            max.setStat("b", 100);
            max.setStat("c", 100);
            assertEquals(100, max.getOverallRating());
        }

        @Test
        void testSingleStatRating() {
            TestPlayer single = new TestPlayer("Odd", 20, Gender.FEMALE, TestPosition.MIDFIELDER);
            single.setStat("speed", 70);
            assertEquals(70, single.getOverallRating());
        }
    }

    @Nested
    class HealthTests {

        @Test
        void testInitialHealth() {
            assertEquals(100, player.getHealth());
        }

        @Test
        void testUpdateHealth() {
            player.updateHealth(70);
            assertEquals(70, player.getHealth());
        }

        @Test
        void testZeroHealthCausesInjury() {
            player.updateHealth(0);
            assertTrue(player.isInjured());
        }

        @ParameterizedTest(name = "Entrance: {0} → Expected health: {1}")
        @CsvSource({
                "150, 100",
                "100, 100",
                "50,   50",
                "0,     0",
                "-10,   0"
        })

        void testUpdateHealthClamping(int input, int expected) {
            player.updateHealth(input);
            assertEquals(expected, player.getHealth());
        }
    }

    @Nested
    class InjuryTests {

        @Test
        void testSetInjury() {
            player.setInjury(3);
            assertTrue(player.isInjured());
            assertEquals(3, player.getInjuryMatchesRemaining());
        }

        @Test
        void testSetInjuryZeroClears() {
            player.setInjury(5);
            player.setInjury(0);
            assertFalse(player.isInjured());
            assertEquals(0, player.getInjuryMatchesRemaining());
        }

        @ParameterizedTest(name = "Negative value: {0}")
        @ValueSource(ints = {-1, -5, -100})

        void testSetInjuryNegativeThrows(int negative) {
            assertThrows(IllegalArgumentException.class, () -> player.setInjury(negative));
        }

        @Test
        void testDecrementCounter() {
            player.setInjury(3);
            player.decrementInjuryCounter();
            assertEquals(2, player.getInjuryMatchesRemaining());
            player.decrementInjuryCounter();
            assertEquals(1, player.getInjuryMatchesRemaining());
            player.decrementInjuryCounter();
            assertEquals(0, player.getInjuryMatchesRemaining());
            assertFalse(player.isInjured());
        }

        @Test
        void testDecrementDoesNotGoBelowZero() {
            player.setInjury(0);
            player.decrementInjuryCounter();
            player.decrementInjuryCounter();
            assertEquals(0, player.getInjuryMatchesRemaining());
        }

        @Test
        void testIsInjuredWhenHealthZero() {
            player.updateHealth(0);
            assertTrue(player.isInjured());
        }

        @Test
        void testFullInjuryCycle() {
            assertFalse(player.isInjured());
            player.setInjury(2);
            assertTrue(player.isInjured());
            player.decrementInjuryCounter();
            assertTrue(player.isInjured());
            player.decrementInjuryCounter();
            assertFalse(player.isInjured());
            assertEquals(0, player.getInjuryMatchesRemaining());
        }

        @Test
        void testLongInjury() {
            player.setInjury(10);
            for (int i = 10; i > 0; i--) {
                assertEquals(i, player.getInjuryMatchesRemaining());
                player.decrementInjuryCounter();
            }
            assertFalse(player.isInjured());
        }
    }

    @Nested
    class EqualityTests {

        @Test
        void testEqualsReflexive() {
            assertEquals(player, player);
        }

        @Test
        void testDifferentPlayersNotEqual() {
            TestPlayer other = new TestPlayer("Başka", 22, Gender.FEMALE, TestPosition.MIDFIELDER);
            assertNotEquals(player, other);
        }

        @Test
        void testSameNameDifferentIdNotEqual() {
            TestPlayer twin = new TestPlayer("Ali Yılmaz", 25, Gender.MALE, TestPosition.FORWARD);
            assertNotEquals(player, twin);
        }

        @Test
        void testNotEqualToNull() {
            assertNotEquals(null, player);
        }

        @Test
        void testHashCodeConsistent() {
            assertEquals(player.hashCode(), player.hashCode());
        }
    }

    @Nested
    class IntegrationTests {

        @Test
        void testStatImprovementRaisesRating() {
            int before = player.getOverallRating();
            player.setStat("shooting", player.getStat("shooting") + 10);
            assertTrue(player.getOverallRating() > before);
        }

        @Test
        void testReInjuryOverwrites() {
            player.setInjury(5);
            player.setInjury(2);
            assertEquals(2, player.getInjuryMatchesRemaining());
        }

        @Test
        void testHealthRecovery() {
            player.updateHealth(0);
            assertTrue(player.isInjured());

            player.updateHealth(100);
            assertFalse(player.isInjured());
        }

        @Test
        void testBothInjuryConditions() {
            player.updateHealth(0);
            player.setInjury(3);
            assertTrue(player.isInjured());

            player.updateHealth(100);
            assertTrue(player.isInjured());

            player.setInjury(0);
            assertFalse(player.isInjured());
        }
    }
}


