package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for Event.
 * Tests individual event functionality and validation.
 */
public class EventTest {

  private LocalDateTime start;
  private LocalDateTime end;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    start = LocalDateTime.of(2025, 11, 15, 10, 0);
    end = LocalDateTime.of(2025, 11, 15, 11, 0);
  }

  @Test
  public void testBasicConstructor() {
    Event event = new Event("Meeting", start, end);
    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
    assertEquals("", event.getDescription());
    assertEquals("", event.getLocation());
    assertTrue(event.isPublic());
    assertNull(event.getSeriesId());
  }

  @Test
  public void testAllDayConstructor() {
    LocalDateTime date = LocalDateTime.of(2025, 11, 15, 12, 30);
    Event event = new Event("Holiday", date);

    assertEquals("Holiday", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 11, 15, 8, 0), event.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 15, 17, 0), event.getEnd());
    assertTrue(event.isAllDay());
  }

  @Test
  public void testBuilderPattern() {
    Event event = new Event.Builder("Conference", start, end)
        .description("Annual conference")
        .location("Convention Center")
        .isPublic(false)
        .build();

    assertEquals("Conference", event.getSubject());
    assertEquals("Annual conference", event.getDescription());
    assertEquals("Convention Center", event.getLocation());
    assertFalse(event.isPublic());

    Event simple = new Event.Builder("Simple", start, end).build();
    assertEquals("", simple.getDescription());
    assertEquals("", simple.getLocation());
    assertTrue(simple.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullSubject() {
    new Event(null, start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptySubject() {
    new Event("", start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWhitespaceSubject() {
    new Event("   ", start, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullStart() {
    new Event("Meeting", null, end);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullEnd() {
    new Event("Meeting", start, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndBeforeStart() {
    new Event("Meeting", end, start);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEndEqualsStart() {
    new Event("Meeting", start, start);
  }

  @Test
  public void testUpdateSubject() {
    Event event = new Event("Old", start, end);
    event.updateSubject("New Subject");
    assertEquals("New Subject", event.getSubject());

    event.updateSubject("  Trimmed  ");
    assertEquals("Trimmed", event.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSubjectNull() {
    Event event = new Event("Meeting", start, end);
    event.updateSubject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateSubjectEmpty() {
    Event event = new Event("Meeting", start, end);
    event.updateSubject("");
  }

  @Test
  public void testUpdateStart() {
    Event event = new Event("Meeting", start, end);
    LocalDateTime newStart = LocalDateTime.of(2025, 11, 15, 9, 0);
    event.updateStart(newStart);
    assertEquals(newStart, event.getStart());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateStartNull() {
    Event event = new Event("Meeting", start, end);
    event.updateStart(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateStartAfterEnd() {
    Event event = new Event("Meeting", start, end);
    event.updateStart(LocalDateTime.of(2025, 11, 15, 12, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateStartEqualsEnd() {
    Event event = new Event("Meeting", start, end);
    event.updateStart(end);
  }

  @Test
  public void testUpdateEnd() {
    Event event = new Event("Meeting", start, end);
    LocalDateTime newEnd = LocalDateTime.of(2025, 11, 15, 12, 0);
    event.updateEnd(newEnd);
    assertEquals(newEnd, event.getEnd());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateEndNull() {
    Event event = new Event("Meeting", start, end);
    event.updateEnd(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateEndBeforeStart() {
    Event event = new Event("Meeting", start, end);
    event.updateEnd(LocalDateTime.of(2025, 11, 15, 9, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateEndEqualsStart() {
    Event event = new Event("Meeting", start, end);
    event.updateEnd(start);
  }

  @Test
  public void testUpdateStatus() {
    Event event = new Event("Meeting", start, end);
    event.updateStatus("private");
    assertFalse(event.isPublic());

    event.updateStatus("public");
    assertTrue(event.isPublic());

    event.updateStatus("PUBLIC");
    assertTrue(event.isPublic());

    event.updateStatus("PRIVATE");
    assertFalse(event.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateStatusInvalid() {
    Event event = new Event("Meeting", start, end);
    event.updateStatus("invalid");
  }

  @Test
  public void testOverlapsWith() {
    Event event1 = new Event("Meeting1", start, end);
    Event event2 = new Event("Meeting2",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));

    assertTrue(event1.overlapsWith(event2));
    assertTrue(event2.overlapsWith(event1));

    Event event3 = new Event("Meeting3",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));
    assertFalse(event1.overlapsWith(event3));

    assertFalse(event1.overlapsWith(null));
  }

  @Test
  public void testSpansMultipleDays() {
    Event overnight = new Event("Overnight",
        LocalDateTime.of(2025, 11, 15, 23, 0),
        LocalDateTime.of(2025, 11, 16, 1, 0));
    assertTrue(overnight.spansMultipleDays());

    Event sameDay = new Event("Meeting", start, end);
    assertFalse(sameDay.spansMultipleDays());
  }

  @Test
  public void testGetDurationMinutes() {
    Event event = new Event("Meeting", start, end);
    assertEquals(60, event.getDurationMinutes());

    Event longEvent = new Event("Conference", start,
        LocalDateTime.of(2025, 11, 15, 15, 30));
    assertEquals(330, longEvent.getDurationMinutes());
  }

  @Test
  public void testIsAllDay() {
    Event allDay = new Event("Holiday",
        LocalDateTime.of(2025, 11, 15, 8, 0),
        LocalDateTime.of(2025, 11, 15, 17, 0));
    assertTrue(allDay.isAllDay());

    Event notAllDay = new Event("Meeting", start, end);
    assertFalse(notAllDay.isAllDay());

    Event wrongStart = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 9, 0),
        LocalDateTime.of(2025, 11, 15, 17, 0));
    assertFalse(wrongStart.isAllDay());

    Event wrongEnd = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 8, 0),
        LocalDateTime.of(2025, 11, 15, 16, 0));
    assertFalse(wrongEnd.isAllDay());
  }

  @Test
  public void testIsPartOfSeries() {
    Event event = new Event("Meeting", start, end);
    assertFalse(event.isPartOfSeries());

    event.setSeriesId("series-123");
    assertTrue(event.isPartOfSeries());
  }

  @Test
  public void testCopy() {
    Event original = new Event.Builder("Conference", start, end)
        .description("Important")
        .location("Hall A")
        .isPublic(false)
        .build();
    original.setSeriesId("series-123");

    InterfaceEvent copy = original.copy();

    assertEquals(original.getSubject(), copy.getSubject());
    assertEquals(original.getStart(), copy.getStart());
    assertEquals(original.getEnd(), copy.getEnd());
    assertEquals(original.getDescription(), copy.getDescription());
    assertEquals(original.getLocation(), copy.getLocation());
    assertEquals(original.isPublic(), copy.isPublic());
    assertEquals(original.getSeriesId(), copy.getSeriesId());

    assertNotSame(original, copy);
  }

  @Test
  public void testShiftTime() {
    Event event = new Event("Meeting", start, end);

    event.shiftTime(30);
    assertEquals(LocalDateTime.of(2025, 11, 15, 10, 30), event.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 15, 11, 30), event.getEnd());

    event.shiftTime(-30);
    assertEquals(LocalDateTime.of(2025, 11, 15, 10, 0), event.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 15, 11, 0), event.getEnd());

    event.shiftTime(0);
    assertEquals(start, event.getStart());
    assertEquals(end, event.getEnd());
  }

  @Test
  public void testSetDuration() {
    Event event = new Event("Meeting", start, end);
    event.setDuration(90);

    assertEquals(start, event.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 15, 11, 30), event.getEnd());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetDurationZero() {
    Event event = new Event("Meeting", start, end);
    event.setDuration(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetDurationNegative() {
    Event event = new Event("Meeting", start, end);
    event.setDuration(-30);
  }

  @Test
  public void testOccursOnDate() {
    Event event = new Event("Meeting", start, end);

    assertTrue(event.occursOnDate(LocalDateTime.of(2025, 11, 15, 14, 0)));
    assertTrue(event.occursOnDate(LocalDateTime.of(2025, 11, 15, 0, 0)));
    assertTrue(event.occursOnDate(LocalDateTime.of(2025, 11, 15, 23, 59)));
    assertFalse(event.occursOnDate(LocalDateTime.of(2025, 11, 16, 10, 0)));
  }

  @Test
  public void testIsActiveAt() {
    Event event = new Event("Meeting", start, end);

    assertTrue(event.isActiveAt(LocalDateTime.of(2025, 11, 15, 10, 30)));
    assertTrue(event.isActiveAt(start));
    assertFalse(event.isActiveAt(end));
    assertFalse(event.isActiveAt(LocalDateTime.of(2025, 11, 15, 9, 0)));
    assertFalse(event.isActiveAt(LocalDateTime.of(2025, 11, 15, 12, 0)));
  }

  @Test
  public void testMatches() {
    Event event1 = new Event("Meeting", start, end);
    Event event2 = new Event("Meeting", start, end);

    assertTrue(event1.matches(event2));
    assertTrue(event2.matches(event1));

    Event differentSubject = new Event("Conference", start, end);
    assertFalse(event1.matches(differentSubject));

    Event differentStart = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 9, 0), end);
    assertFalse(event1.matches(differentStart));

    Event differentEnd = new Event("Meeting", start,
        LocalDateTime.of(2025, 11, 15, 12, 0));
    assertFalse(event1.matches(differentEnd));

    assertFalse(event1.matches(null));
  }

  @Test
  public void testSetSubject() {
    Event event = new Event("Old", start, end);
    event.setSubject("New");
    assertEquals("New", event.getSubject());

    event.setSubject("  Trimmed  ");
    assertEquals("Trimmed", event.getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSubjectNull() {
    Event event = new Event("Meeting", start, end);
    event.setSubject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetSubjectEmpty() {
    Event event = new Event("Meeting", start, end);
    event.setSubject("");
  }

  @Test
  public void testSetStart() {
    Event event = new Event("Meeting", start, end);
    LocalDateTime newStart = LocalDateTime.of(2025, 11, 15, 9, 0);
    event.setStart(newStart);
    assertEquals(newStart, event.getStart());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStartNull() {
    Event event = new Event("Meeting", start, end);
    event.setStart(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStartAfterEnd() {
    Event event = new Event("Meeting", start, end);
    event.setStart(LocalDateTime.of(2025, 11, 15, 12, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetStartEqualsEnd() {
    Event event = new Event("Meeting", start, end);
    event.setStart(end);
  }

  @Test
  public void testSetEnd() {
    Event event = new Event("Meeting", start, end);
    LocalDateTime newEnd = LocalDateTime.of(2025, 11, 15, 12, 0);
    event.setEnd(newEnd);
    assertEquals(newEnd, event.getEnd());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEndNull() {
    Event event = new Event("Meeting", start, end);
    event.setEnd(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEndBeforeStart() {
    Event event = new Event("Meeting", start, end);
    event.setEnd(LocalDateTime.of(2025, 11, 15, 9, 0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetEndEqualsStart() {
    Event event = new Event("Meeting", start, end);
    event.setEnd(start);
  }

  @Test
  public void testSetDescription() {
    Event event = new Event("Meeting", start, end);
    event.setDescription("Important meeting");
    assertEquals("Important meeting", event.getDescription());

    event.setDescription(null);
    assertEquals("", event.getDescription());
  }

  @Test
  public void testSetLocation() {
    Event event = new Event("Meeting", start, end);
    event.setLocation("Room 101");
    assertEquals("Room 101", event.getLocation());

    event.setLocation(null);
    assertEquals("", event.getLocation());
  }

  @Test
  public void testSetPublic() {
    Event event = new Event("Meeting", start, end);
    event.setPublic(false);
    assertFalse(event.isPublic());

    event.setPublic(true);
    assertTrue(event.isPublic());
  }

  @Test
  public void testSetSeriesId() {
    Event event = new Event("Meeting", start, end);
    event.setSeriesId("series-123");
    assertEquals("series-123", event.getSeriesId());

    event.setSeriesId(null);
    assertNull(event.getSeriesId());
  }

  @Test
  public void testToString() {
    Event event = new Event("Meeting", start, end);
    String result = event.toString();

    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("from"));
    assertTrue(result.contains("to"));
    assertFalse(result.contains(" at "));

    event.setLocation("Room 101");
    result = event.toString();
    assertTrue(result.contains("at Room 101"));

    event.setSeriesId("series-123");
    result = event.toString();
    assertTrue(result.contains("[Series]"));
  }

  @Test
  public void testEquals() {
    Event event1 = new Event("Meeting", start, end);
    Event event2 = new Event("Meeting", start, end);

    assertTrue(event1.equals(event1));
    assertTrue(event1.equals(event2));
    assertTrue(event2.equals(event1));

    Event differentSubject = new Event("Conference", start, end);
    assertFalse(event1.equals(differentSubject));

    Event differentStart = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 9, 0), end);
    assertFalse(event1.equals(differentStart));

    Event differentEnd = new Event("Meeting", start,
        LocalDateTime.of(2025, 11, 15, 12, 0));
    assertFalse(event1.equals(differentEnd));

    assertFalse(event1.equals(null));
    assertFalse(event1.equals("Not an event"));
  }

  @Test
  public void testHashCode() {
    Event event = new Event("Meeting", start, end);
    int hash1 = event.hashCode();
    int hash2 = event.hashCode();
    assertEquals(hash1, hash2);

    Event event2 = new Event("Meeting", start, end);
    assertEquals(event.hashCode(), event2.hashCode());

    Event different = new Event("Conference", start, end);
    assertNotEquals(event.hashCode(), different.hashCode());
  }
}