package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for RecurringEvent.
 * Tests recurring event generation and validation.
 */
public class RecurringEventTest {
  private LocalDateTime start;
  private LocalDateTime end;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    start = LocalDateTime.of(2025, 11, 10, 9, 0);
    end = LocalDateTime.of(2025, 11, 10, 9, 30);
  }

  @Test
  public void testConstructorCount() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();

    assertEquals("Standup", event.getSubject());
    assertEquals(start, event.getStartTime());
    assertEquals(end, event.getEndTime());
    assertEquals(Integer.valueOf(6), event.getRepeatCount());
    assertNull(event.getRepeatUntil());
    assertNotNull(event.getSeriesId());

    Set<String> weekdays = event.getWeekdays();
    assertEquals(3, weekdays.size());
    assertTrue(weekdays.contains("M"));
    assertTrue(weekdays.contains("W"));
    assertTrue(weekdays.contains("F"));

    InterfaceEvent base = event.getBaseEvent();
    assertNotNull(base);
    assertTrue(base.isPartOfSeries());
  }

  @Test
  public void testConstructorUntil() {
    LocalDateTime until = LocalDateTime.of(2025, 12, 1, 23, 59);
    RecurringEvent event = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();

    assertEquals("Weekly", event.getSubject());
    assertNull(event.getRepeatCount());
    assertEquals(until, event.getRepeatUntil());
    assertNotNull(event.getSeriesId());
  }

  @Test
  public void testConstructorAllProperties() {
    RecurringEvent event = new RecurringEvent.Builder("Meeting", start, end, "MWF")
        .description("Important")
        .location("Room101")
        .isPublic(false)
        .repeatCount(5)
        .build();

    InterfaceEvent base = event.getBaseEvent();
    assertEquals("Important", base.getDescription());
    assertEquals("Room101", base.getLocation());
    assertFalse(base.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleDays() {
    LocalDateTime multiEnd = LocalDateTime.of(2025, 11, 11, 9, 30);
    new RecurringEvent.Builder("Invalid", start, multiEnd, "M")
        .repeatCount(5)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoRepeatInfo() {
    new RecurringEvent.Builder("Invalid", start, end, "M")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBothRepeatInfos() {
    LocalDateTime until = LocalDateTime.of(2025, 12, 1, 23, 59);
    new RecurringEvent.Builder("Invalid", start, end, "M")
        .repeatCount(5)
        .repeatUntil(until)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCount() {
    new RecurringEvent.Builder("Invalid", start, end, "M")
        .repeatCount(-1)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroCount() {
    new RecurringEvent.Builder("Invalid", start, end, "M")
        .repeatCount(0)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUntilBeforeStart() {
    LocalDateTime past = LocalDateTime.of(2025, 11, 1, 23, 59);
    new RecurringEvent.Builder("Invalid", start, end, "M")
        .repeatUntil(past)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullWeekdays() {
    new RecurringEvent.Builder("Invalid", start, end, null)
        .repeatCount(5)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyWeekdays() {
    new RecurringEvent.Builder("Invalid", start, end, "")
        .repeatCount(5)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCode() {
    new RecurringEvent.Builder("Invalid", start, end, "MXF")
        .repeatCount(5)
        .build();
  }

  @Test
  public void testWeekdaysParsing() {
    RecurringEvent single = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatCount(4)
        .build();
    Set<String> weekdays = single.getWeekdays();
    assertEquals(1, weekdays.size());
    assertTrue(weekdays.contains("M"));

    RecurringEvent all = new RecurringEvent.Builder("Daily", start, end, "MTWRFSU")
        .repeatCount(7)
        .build();
    assertEquals(7, all.getWeekdays().size());

    RecurringEvent lowercase = new RecurringEvent.Builder("Meetings", start, end, "mwf")
        .repeatCount(5)
        .build();
    weekdays = lowercase.getWeekdays();
    assertEquals(3, weekdays.size());
    assertTrue(weekdays.contains("M"));
    assertTrue(weekdays.contains("W"));
    assertTrue(weekdays.contains("F"));

    assertNotSame(lowercase.getWeekdays(), lowercase.getWeekdays());
  }

  @Test
  public void testGenerateCount() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();

    assertEquals(6, occurrences.size());
    String seriesId = event.getSeriesId();
    for (InterfaceEvent occ : occurrences) {
      assertEquals("Standup", occ.getSubject());
      assertEquals(30, occ.getDurationMinutes());
      assertTrue(occ.isPartOfSeries());
      assertEquals(seriesId, occ.getSeriesId());
    }
  }

  @Test
  public void testGenerateDates() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();

    assertEquals(DayOfWeek.MONDAY, occurrences.get(0).getStart().getDayOfWeek());
    assertEquals(10, occurrences.get(0).getStart().getDayOfMonth());
    assertEquals(DayOfWeek.WEDNESDAY, occurrences.get(1).getStart().getDayOfWeek());
    assertEquals(12, occurrences.get(1).getStart().getDayOfMonth());
    assertEquals(DayOfWeek.FRIDAY, occurrences.get(2).getStart().getDayOfWeek());
    assertEquals(14, occurrences.get(2).getStart().getDayOfMonth());
    assertEquals(DayOfWeek.MONDAY, occurrences.get(3).getStart().getDayOfWeek());
    assertEquals(17, occurrences.get(3).getStart().getDayOfMonth());
  }

  @Test
  public void testSingleOccurrences() {
    RecurringEvent single = new RecurringEvent.Builder("Once", start, end, "M")
        .repeatCount(1)
        .build();
    List<InterfaceEvent> occurrences = single.generateOccurrences();

    assertEquals(1, occurrences.size());
    assertEquals(10, occurrences.get(0).getStart().getDayOfMonth());

    RecurringEvent weekly = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatCount(4)
        .build();
    occurrences = weekly.generateOccurrences();

    assertEquals(4, occurrences.size());
    for (InterfaceEvent occ : occurrences) {
      assertEquals(DayOfWeek.MONDAY, occ.getStart().getDayOfWeek());
    }
    assertEquals(17, occurrences.get(1).getStart().getDayOfMonth());
    assertEquals(24, occurrences.get(2).getStart().getDayOfMonth());
  }

  @Test
  public void testGenerateUntil() {
    LocalDateTime until = LocalDateTime.of(2025, 11, 30, 23, 59);
    RecurringEvent event = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();

    assertTrue(occurrences.size() > 0);
    for (InterfaceEvent occ : occurrences) {
      assertTrue(occ.getStart().isBefore(until) || occ.getStart().equals(until));
      assertEquals(DayOfWeek.MONDAY, occ.getStart().getDayOfWeek());
    }

    LocalDateTime shortUntil = LocalDateTime.of(2025, 11, 15, 23, 59);
    RecurringEvent brief = new RecurringEvent.Builder("Brief", start, end, "MWF")
        .repeatUntil(shortUntil)
        .build();
    assertEquals(3, brief.generateOccurrences().size());
  }

  @Test
  public void testInRange() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(10)
        .build();
    LocalDate rangeStart = LocalDate.of(2025, 11, 12);
    LocalDate rangeEnd = LocalDate.of(2025, 11, 20);

    List<InterfaceEvent> filtered = event.generateOccurrencesInRange(rangeStart, rangeEnd);

    assertTrue(filtered.size() > 0);
    assertTrue(filtered.size() < 10);
    for (InterfaceEvent occ : filtered) {
      LocalDate date = occ.getStart().toLocalDate();
      assertFalse(date.isBefore(rangeStart));
      assertFalse(date.isAfter(rangeEnd));
    }

    RecurringEvent noMatch = new RecurringEvent.Builder("Standup", start, end, "M")
        .repeatCount(4)
        .build();
    List<InterfaceEvent> empty = noMatch.generateOccurrencesInRange(
        LocalDate.of(2025, 11, 11), LocalDate.of(2025, 11, 13));
    assertTrue(empty.isEmpty());
  }

  @Test
  public void testHasOccurrenceOn() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "M")
        .repeatCount(4)
        .build();

    assertTrue(event.hasOccurrenceOn(LocalDate.of(2025, 11, 10)));
    assertTrue(event.hasOccurrenceOn(LocalDate.of(2025, 11, 17)));
    assertFalse(event.hasOccurrenceOn(LocalDate.of(2025, 11, 11)));
    assertFalse(event.hasOccurrenceOn(LocalDate.of(2025, 11, 3)));

    LocalDateTime until = LocalDateTime.of(2025, 11, 20, 23, 59);
    RecurringEvent withUntil = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    assertFalse(withUntil.hasOccurrenceOn(LocalDate.of(2025, 12, 1)));

    RecurringEvent limited = new RecurringEvent.Builder("Limited", start, end, "M")
        .repeatCount(2)
        .build();
    assertFalse(limited.hasOccurrenceOn(LocalDate.of(2025, 11, 24)));
  }

  @Test
  public void testOccurrenceDates() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();
    assertEquals(LocalDate.of(2025, 11, 10), event.getFirstOccurrenceDate());

    List<InterfaceEvent> occurrences = event.generateOccurrences();
    LocalDate expected = occurrences.get(occurrences.size() - 1).getStart().toLocalDate();
    assertEquals(expected, event.getLastOccurrenceDate());

    LocalDateTime until = LocalDateTime.of(2025, 11, 30, 23, 59);
    RecurringEvent withUntil = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    assertFalse(withUntil.getLastOccurrenceDate().isAfter(until.toLocalDate()));
  }

  @Test
  public void testOccurrenceCount() {
    RecurringEvent withCount = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();
    assertEquals(6, withCount.getOccurrenceCount());

    LocalDateTime until = LocalDateTime.of(2025, 11, 30, 23, 59);
    RecurringEvent withUntil = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    assertEquals(withUntil.generateOccurrences().size(), withUntil.getOccurrenceCount());
  }

  @Test
  public void testToString() {
    RecurringEvent withCount = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();
    String str = withCount.toString();
    assertTrue(str.contains("Recurring Event"));
    assertTrue(str.contains("Standup"));
    assertTrue(str.contains("for"));
    assertTrue(str.contains("6"));
    assertTrue(str.contains("times"));

    LocalDateTime until = LocalDateTime.of(2025, 11, 30, 23, 59);
    RecurringEvent withUntil = new RecurringEvent.Builder("Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    str = withUntil.toString();
    assertTrue(str.contains("Recurring Event"));
    assertTrue(str.contains("Weekly"));
    assertTrue(str.contains("until"));
  }

  @Test
  public void testMultipleWeekdays() {
    RecurringEvent event = new RecurringEvent.Builder("Multi", start, end, "MTWRFSU")
        .repeatCount(14)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();

    assertEquals(14, occurrences.size());
    for (int i = 0; i < occurrences.size() - 1; i++) {
      LocalDate current = occurrences.get(i).getStart().toLocalDate();
      LocalDate next = occurrences.get(i + 1).getStart().toLocalDate();
      assertEquals(1, next.toEpochDay() - current.toEpochDay());
    }
  }

  @Test
  public void testAcrossBoundaries() {
    LocalDateTime novEnd = LocalDateTime.of(2025, 11, 28, 9, 0);
    RecurringEvent acrossMonth = new RecurringEvent.Builder("Weekly", novEnd,
        LocalDateTime.of(2025, 11, 28, 9, 30), "F")
        .repeatCount(3)
        .build();

    List<InterfaceEvent> occurrences = acrossMonth.generateOccurrences();
    assertEquals(3, occurrences.size());
    assertEquals(11, occurrences.get(0).getStart().getMonthValue());
    assertEquals(12, occurrences.get(1).getStart().getMonthValue());
    assertEquals(12, occurrences.get(2).getStart().getMonthValue());

    LocalDateTime decEnd = LocalDateTime.of(2025, 12, 29, 9, 0);
    RecurringEvent acrossYear = new RecurringEvent.Builder("Weekly", decEnd,
        LocalDateTime.of(2025, 12, 29, 9, 30), "M")
        .repeatCount(3)
        .build();

    occurrences = acrossYear.generateOccurrences();
    assertEquals(3, occurrences.size());
    assertEquals(2025, occurrences.get(0).getStart().getYear());
    assertEquals(2026, occurrences.get(1).getStart().getYear());
    assertEquals(2026, occurrences.get(2).getStart().getYear());
  }

  @Test
  public void testPreserveProperties() {
    RecurringEvent event = new RecurringEvent.Builder("Standup", start, end, "MWF")
        .repeatCount(6)
        .build();

    String seriesId = event.getSeriesId();
    for (InterfaceEvent occ : event.generateOccurrences()) {
      assertEquals(9, occ.getStart().getHour());
      assertEquals(0, occ.getStart().getMinute());
      assertEquals(9, occ.getEnd().getHour());
      assertEquals(30, occ.getEnd().getMinute());
      assertEquals(seriesId, occ.getSeriesId());
      assertTrue(occ.isPartOfSeries());
    }
  }

  @Test
  public void testWeekendWeekday() {
    RecurringEvent weekend = new RecurringEvent.Builder("Weekend", start, end, "SU")
        .repeatCount(4)
        .build();
    for (InterfaceEvent occ : weekend.generateOccurrences()) {
      DayOfWeek day = occ.getStart().getDayOfWeek();
      assertTrue(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    }

    RecurringEvent weekday = new RecurringEvent.Builder("Weekday", start, end, "MTWRF")
        .repeatCount(10)
        .build();
    for (InterfaceEvent occ : weekday.generateOccurrences()) {
      DayOfWeek day = occ.getStart().getDayOfWeek();
      assertFalse(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    }
  }

  @Test
  public void testLeapYear() {
    LocalDateTime leapStart = LocalDateTime.of(2024, 2, 26, 9, 0);
    RecurringEvent event = new RecurringEvent.Builder("Leap", leapStart,
        LocalDateTime.of(2024, 2, 26, 9, 30), "MTWRF")
        .repeatCount(5)
        .build();

    boolean hasLeap = event.generateOccurrences().stream()
        .anyMatch(e -> e.getStart().getDayOfMonth() == 29
            && e.getStart().getMonthValue() == 2);
    assertTrue(hasLeap);
  }

  @Test
  public void testLongSeries() {
    RecurringEvent event = new RecurringEvent.Builder("Daily", start, end, "MTWRFSU")
        .repeatCount(100)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();

    assertEquals(100, occurrences.size());
    assertEquals(LocalDate.of(2025, 11, 10), occurrences.get(0).getStart().toLocalDate());
    assertEquals(2026, occurrences.get(99).getStart().getYear());
  }

  @Test
  public void testBuilderProperties() {
    RecurringEvent event = new RecurringEvent.Builder("Test", start, end, "M")
        .description("Important")
        .location("Office")
        .isPublic(false)
        .repeatCount(5)
        .build();

    assertEquals("Important", event.getBaseEvent().getDescription());
    assertEquals("Office", event.getBaseEvent().getLocation());
    assertFalse(event.getBaseEvent().isPublic());
    assertEquals(Integer.valueOf(5), event.getRepeatCount());

    LocalDateTime until = LocalDateTime.of(2025, 12, 1, 23, 59);
    RecurringEvent withUntil = new RecurringEvent.Builder("Test", start, end, "M")
        .repeatUntil(until)
        .build();
    assertEquals(until, withUntil.getRepeatUntil());
  }

  @Test
  public void testPropertiesInOccurrences() {
    RecurringEvent event = new RecurringEvent.Builder("Test", start, end, "M")
        .description("Desc")
        .location("Loc")
        .isPublic(false)
        .repeatCount(3)
        .build();
    List<InterfaceEvent> occurrences = event.generateOccurrences();
    for (InterfaceEvent occ : occurrences) {
      assertEquals("Desc", occ.getDescription());
      assertEquals("Loc", occ.getLocation());
      assertFalse(occ.isPublic());
    }
  }
}