package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for TimezoneConverter.
 * Tests timezone conversion functionality.
 */
public class TimezoneConverterTest {

  private LocalDateTime testTime;
  private ZoneId utc;
  private ZoneId newYork;
  private ZoneId losAngeles;
  private ZoneId tokyo;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    testTime = LocalDateTime.of(2025, 6, 15, 12, 0);
    utc = ZoneId.of("UTC");
    newYork = ZoneId.of("America/New_York");
    losAngeles = ZoneId.of("America/Los_Angeles");
    tokyo = ZoneId.of("Asia/Tokyo");
  }

  @Test
  public void testConstructorThrowsException() throws Exception {
    Constructor<TimezoneConverter> constructor =
        TimezoneConverter.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    try {
      constructor.newInstance();
      fail("Should have thrown UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
      assertEquals("Utility class cannot be instantiated", e.getCause().getMessage());
    }
  }

  @Test
  public void testConvertBetweenTimezones() {
    LocalDateTime result = TimezoneConverter.convertBetweenTimezones(
        testTime, utc, newYork);
    assertEquals(LocalDateTime.of(2025, 6, 15, 8, 0), result);

    LocalDateTime nyTime = LocalDateTime.of(2025, 6, 15, 10, 0);
    result = TimezoneConverter.convertBetweenTimezones(nyTime, newYork, utc);
    assertEquals(LocalDateTime.of(2025, 6, 15, 14, 0), result);

    LocalDateTime laTime = LocalDateTime.of(2025, 6, 15, 9, 0);
    result = TimezoneConverter.convertBetweenTimezones(laTime, losAngeles, newYork);
    assertEquals(LocalDateTime.of(2025, 6, 15, 12, 0), result);

    result = TimezoneConverter.convertBetweenTimezones(testTime, utc, tokyo);
    assertEquals(LocalDateTime.of(2025, 6, 15, 21, 0), result);

    result = TimezoneConverter.convertBetweenTimezones(testTime, utc, utc);
    assertEquals(testTime, result);
  }

  @Test
  public void testConvertPreservesDuration() {
    LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 15, 11, 30);

    LocalDateTime convertedStart = TimezoneConverter.convertBetweenTimezones(
        start, utc, newYork);
    LocalDateTime convertedEnd = TimezoneConverter.convertBetweenTimezones(
        end, utc, newYork);

    long originalDuration = java.time.Duration.between(start, end).toMinutes();
    long convertedDuration = java.time.Duration.between(convertedStart, convertedEnd).toMinutes();

    assertEquals(originalDuration, convertedDuration);
  }

  @Test
  public void testConvertDaylightSavingTime() {
    LocalDateTime dstTime = LocalDateTime.of(2025, 3, 10, 12, 0);
    LocalDateTime result = TimezoneConverter.convertBetweenTimezones(
        dstTime, utc, newYork);

    assertNotEquals(dstTime, result);
  }

  @Test
  public void testConvertAllEventsSameTimezone() {
    List<InterfaceEvent> events = new ArrayList<>();

    Event event1 = new Event("Meeting1",
        LocalDateTime.of(2025, 6, 15, 10, 0),
        LocalDateTime.of(2025, 6, 15, 11, 0));
    Event event2 = new Event("Meeting2",
        LocalDateTime.of(2025, 6, 15, 14, 0),
        LocalDateTime.of(2025, 6, 15, 15, 0));

    events.add(event1);
    events.add(event2);

    LocalDateTime originalStart1 = event1.getStart();
    LocalDateTime originalStart2 = event2.getStart();

    TimezoneConverter.convertAllEvents(events, utc, utc);

    assertEquals(originalStart1, event1.getStart());
    assertEquals(originalStart2, event2.getStart());
  }

  @Test
  public void testConvertAllEventsDifferentTimezones() {
    List<InterfaceEvent> events = new ArrayList<>();

    Event event1 = new Event("Meeting1",
        LocalDateTime.of(2025, 6, 15, 12, 0),
        LocalDateTime.of(2025, 6, 15, 13, 0));
    Event event2 = new Event("Meeting2",
        LocalDateTime.of(2025, 6, 15, 14, 0),
        LocalDateTime.of(2025, 6, 15, 15, 0));

    events.add(event1);
    events.add(event2);

    TimezoneConverter.convertAllEvents(events, utc, newYork);

    assertEquals(LocalDateTime.of(2025, 6, 15, 8, 0), event1.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 15, 9, 0), event1.getEnd());
    assertEquals(LocalDateTime.of(2025, 6, 15, 10, 0), event2.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 15, 11, 0), event2.getEnd());
  }

  @Test
  public void testConvertAllEventsEmpty() {
    List<InterfaceEvent> events = new ArrayList<>();
    TimezoneConverter.convertAllEvents(events, utc, newYork);
    assertEquals(0, events.size());
  }

  @Test
  public void testConvertAllEventsMultiple() {
    List<InterfaceEvent> events = new ArrayList<>();

    Event event1 = new Event("Meeting",
        LocalDateTime.of(2025, 6, 15, 10, 0),
        LocalDateTime.of(2025, 6, 15, 11, 0));
    Event event2 = new Event("Lunch",
        LocalDateTime.of(2025, 6, 15, 12, 0),
        LocalDateTime.of(2025, 6, 15, 13, 0));

    events.add(event1);
    events.add(event2);

    TimezoneConverter.convertAllEvents(events, utc, newYork);

    assertEquals(LocalDateTime.of(2025, 6, 15, 6, 0), event1.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 15, 8, 0), event2.getStart());
  }

  @Test
  public void testConvertAllEventsRecurring() {
    List<InterfaceEvent> events = new ArrayList<>();

    Event event1 = new Event("Series1",
        LocalDateTime.of(2025, 6, 15, 9, 0),
        LocalDateTime.of(2025, 6, 15, 10, 0));
    event1.setSeriesId("series-123");

    Event event2 = new Event("Series1",
        LocalDateTime.of(2025, 6, 17, 9, 0),
        LocalDateTime.of(2025, 6, 17, 10, 0));
    event2.setSeriesId("series-123");

    events.add(event1);
    events.add(event2);

    TimezoneConverter.convertAllEvents(events, utc, losAngeles);

    assertEquals(LocalDateTime.of(2025, 6, 15, 2, 0), event1.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 17, 2, 0), event2.getStart());

    assertEquals("series-123", event1.getSeriesId());
    assertEquals("series-123", event2.getSeriesId());
  }

  @Test
  public void testConvertOffsets() {
    LocalDateTime time = LocalDateTime.of(2025, 6, 15, 2, 0);
    LocalDateTime result = TimezoneConverter.convertBetweenTimezones(
        time, losAngeles, utc);
    assertEquals(LocalDateTime.of(2025, 6, 15, 9, 0), result);

    time = LocalDateTime.of(2025, 6, 15, 12, 0);
    result = TimezoneConverter.convertBetweenTimezones(time, utc, tokyo);
    assertEquals(LocalDateTime.of(2025, 6, 15, 21, 0), result);
  }

  @Test
  public void testConvertAllEventsWithProperties() {
    List<InterfaceEvent> events = new ArrayList<>();

    Event event = new Event.Builder("Conference",
        LocalDateTime.of(2025, 6, 15, 14, 0),
        LocalDateTime.of(2025, 6, 15, 16, 0))
        .description("Important")
        .location("Hall A")
        .isPublic(false)
        .build();

    events.add(event);

    TimezoneConverter.convertAllEvents(events, utc, newYork);

    assertEquals(LocalDateTime.of(2025, 6, 15, 10, 0), event.getStart());
    assertEquals("Important", event.getDescription());
    assertEquals("Hall A", event.getLocation());
    assertFalse(event.isPublic());
  }

  @Test
  public void testConvertAtBoundaries() {
    LocalDateTime midnight = LocalDateTime.of(2025, 6, 15, 0, 0);
    LocalDateTime result = TimezoneConverter.convertBetweenTimezones(
        midnight, utc, newYork);
    assertEquals(LocalDateTime.of(2025, 6, 14, 20, 0), result);

    LocalDateTime lateNight = LocalDateTime.of(2025, 6, 15, 23, 30);
    result = TimezoneConverter.convertBetweenTimezones(lateNight, newYork, utc);
    assertEquals(LocalDateTime.of(2025, 6, 16, 3, 30), result);
  }
}