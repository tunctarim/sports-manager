# League Module – Implementation Notes

## New Files

### Fixture.java
Represents a scheduled match between two teams. Stores home and away team references. A team cannot be scheduled against itself.

### FixtureGenerator.java
Generates round-robin fixture lists for a league. Supports single-leg (each pair plays once) and double-leg (each pair plays twice) scheduling.

### StandingRecord.java
Represents one row in the league standings table. Reads stats directly from the team so it always reflects the current state. Supports sorting by points, goal difference, and points scored.

## Modified Files

### League.java
The original League class only had basic fields and methods. The following were added:

- Input validation on constructor arguments
- `removeTeam()` method
- `getTeamCount()` helper
- `containsTeam()` helper
- `getStandings()` returns a sorted standings list
- `generateFixtures()` generates a single-leg schedule
- `generateDoubleLegged()` generates a double-leg schedule
- `getTeams()` now returns an unmodifiable list

## Design Decisions

**Null and duplicate team on addTeam** – treated as a no-op instead of throwing an exception. Accidental duplicate calls should not crash the application.

**StandingRecord reads live from ITeam** – no need to refresh manually. The record always reflects the team's current state.

**Fixture generation is in a separate class** – keeps League as a data container and lets the scheduling logic evolve independently.

## Tests

| File | Tests |
|---|---|
| LeagueTest.java | Constructor, team management, standings, fixture generation, toString |
| StandingRecordTest.java | Stat accessors, null stats, sorting, toString |
| FixtureGeneratorTest.java | Single-leg, double-leg, edge cases, Fixture object |