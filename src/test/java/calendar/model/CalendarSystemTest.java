package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CalendarSystem.
 * Tests the calendar system model with multiple calendars.
 */
public class CalendarSystemTest {
  private CalendarSystem system;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    system = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testInit() {
    assertNotNull(system);
    assertTrue(system.getAllCalendarNames().isEmpty());
  }

  @Test
  public void testCreateCalendar() {
    String commands = "create calendar --name Work --timezone America/New_York\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();

    assertTrue(system.calendarExists("Work"));
    assertEquals("Work", system.getCalendar("Work").getName());
    assertEquals(ZoneId.of("America/New_York"), system.getCalendar("Work").getTimezone());
  }

  @Test
  public void testCreateMultipleCalendars() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "create calendar --name Personal --timezone America/Los_Angeles\n"
        + "create calendar --name School --timezone UTC\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();

    List<String> names = system.getAllCalendarNames();
    assertEquals(3, names.size());
    assertTrue(names.contains("Work"));
    assertTrue(names.contains("Personal"));
    assertTrue(names.contains("School"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCalendarName() {
    system.createCalendar(null, "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyCalendarName() {
    system.createCalendar("", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWhitespaceCalendarName() {
    system.createCalendar("   ", "UTC");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateCalendar() {
    system.createCalendar("Work", "UTC");
    system.createCalendar("Work", "America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimezone() {
    system.createCalendar("Work", "InvalidTimezone");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNonexistentCalendar() {
    system.getCalendar("NonExistent");
  }

  @Test
  public void testCalendarExists() {
    system.createCalendar("Work", "UTC");
    assertTrue(system.calendarExists("Work"));
    assertFalse(system.calendarExists("NonExistent"));
  }

  @Test
  public void testEditCalendarName() {
    system.createCalendar("Work", "UTC");
    system.editCalendarName("Work", "NewWork");

    assertFalse(system.calendarExists("Work"));
    assertTrue(system.calendarExists("NewWork"));
    assertEquals("NewWork", system.getCalendar("NewWork").getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditNonexistentCalendarName() {
    system.editCalendarName("NonExistent", "NewName");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarNameToDuplicate() {
    system.createCalendar("Work", "UTC");
    system.createCalendar("Personal", "UTC");
    system.editCalendarName("Work", "Personal");
  }

  @Test
  public void testEditCalendarTimezone() {
    system.createCalendar("Work", "UTC");
    system.editCalendarTimezone("Work", "America/New_York");
    assertEquals(ZoneId.of("America/New_York"), system.getCalendar("Work").getTimezone());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditTimezoneInvalid() {
    system.createCalendar("Work", "UTC");
    system.editCalendarTimezone("Work", "InvalidTimezone");
  }

  @Test
  public void testDeleteCalendar() {
    String commands = "create calendar --name Work --timezone UTC\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();
    system.deleteCalendar("Work");
    assertFalse(system.calendarExists("Work"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteNonexistentCalendar() {
    system.deleteCalendar("NonExistent");
  }

  @Test
  public void testCreateSimpleEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();

    InterfaceCalendar calendar = system.getCalendar("Work");
    assertEquals(1, calendar.getEventCount());
    InterfaceEvent event = calendar.getAllEvents().get(0);
    assertEquals("Meeting", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 11, 15, 10, 0), event.getStart());
    assertEquals(LocalDateTime.of(2025, 11, 15, 11, 0), event.getEnd());
  }

  @Test
  public void testCreateEventWithProperties() {
    system.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 14, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 15, 30);

    EventCreationRequest request = new EventCreationRequest.Builder("Meeting", start, end)
        .description("Q4Review")
        .location("Room101")
        .isPublic(false)
        .build();
    system.createEvent("Work", request);

    InterfaceEvent event = system.getCalendar("Work").getAllEvents().get(0);
    assertEquals("Meeting", event.getSubject());
    assertEquals("Q4Review", event.getDescription());
    assertEquals("Room101", event.getLocation());
    assertFalse(event.isPublic());
    assertEquals(90, event.getDurationMinutes());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventNonexistentCalendar() {
    system.createEvent("NonExistent", "Meeting",
        LocalDateTime.now(), LocalDateTime.now().plusHours(1));
  }

  @Test
  public void testCreateRecurringWithCount() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to "
        + "2025-11-10T09:30 repeats MWF for 6\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();

    InterfaceCalendar calendar = system.getCalendar("Work");
    assertEquals(6, calendar.getEventCount());
    List<InterfaceEvent> events = calendar.getAllEvents();
    String seriesId = events.get(0).getSeriesId();
    for (InterfaceEvent event : events) {
      assertEquals("Standup", event.getSubject());
      assertTrue(event.isPartOfSeries());
      assertEquals(seriesId, event.getSeriesId());
    }
  }

  @Test
  public void testCreateRecurringWithUntil() {
    system.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 10, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 10, 11, 0);
    LocalDateTime until = LocalDateTime.of(2025, 12, 1, 23, 59);

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Weekly", start, end, "M")
        .repeatUntil(until)
        .build();
    system.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = system.getCalendar("Work");
    assertTrue(calendar.getEventCount() > 0);
    for (InterfaceEvent event : calendar.getAllEvents()) {
      assertTrue(event.getStart().isBefore(until) || event.getStart().equals(until));
    }
  }

  @Test
  public void testCopySingleEvent() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");
    LocalDateTime sourceStart = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2025, 11, 15, 11, 0);

    EventCreationRequest request = new EventCreationRequest.Builder("Meeting",
        sourceStart, sourceEnd)
        .description("Important")
        .location("Room101")
        .isPublic(false)
        .build();
    system.createEvent("Source", request);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 20, 14, 0);

    EventCopyRequest copyRequest = new EventCopyRequest.Builder("Meeting")
        .sourceDateTime(sourceStart)
        .targetCalendar("Target")
        .targetDateTime(targetStart)
        .build();
    system.copyEvent("Source", copyRequest);

    assertEquals(1, system.getCalendar("Source").getEventCount());
    InterfaceCalendar target = system.getCalendar("Target");
    assertEquals(1, target.getEventCount());
    InterfaceEvent copied = target.getAllEvents().get(0);
    assertEquals("Meeting", copied.getSubject());
    assertEquals(targetStart, copied.getStart());
    assertEquals(60, copied.getDurationMinutes());
    assertEquals("Important", copied.getDescription());
    assertEquals("Room101", copied.getLocation());
    assertFalse(copied.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyNonexistentEvent() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");

    EventCopyRequest copyRequest = new EventCopyRequest.Builder("NonExistent")
        .sourceDateTime(LocalDateTime.now())
        .targetCalendar("Target")
        .targetDateTime(LocalDateTime.now())
        .build();
    system.copyEvent("Source", copyRequest);
  }

  @Test
  public void testCopyEventSameTimezone() {
    system.createCalendar("Cal1", "UTC");
    system.createCalendar("Cal2", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);
    system.createEvent("Cal1", "Meeting", start, end);

    LocalDateTime targetStart = LocalDateTime.of(2025, 11, 20, 14, 0);
    EventCopyRequest copyRequest = new EventCopyRequest.Builder("Meeting")
        .sourceDateTime(start)
        .targetCalendar("Cal2")
        .targetDateTime(targetStart)
        .build();
    system.copyEvent("Cal1", copyRequest);

    InterfaceEvent copied = system.getCalendar("Cal2").getAllEvents().get(0);
    assertEquals(targetStart, copied.getStart());
    assertEquals(60, copied.getDurationMinutes());
  }

  @Test
  public void testCopyEventDifferentTimezones() {
    String commands = "create calendar --name PST --timezone America/Los_Angeles\n"
        + "create calendar --name EST --timezone America/New_York\n"
        + "use calendar --name PST\n"
        + "create event Meeting from 2025-06-15T10:00 to 2025-06-15T11:00\n"
        + "copy event Meeting on 2025-06-15T10:00 --target EST to 2025-06-15T10:00\nexit\n";
    new CalendarController(system, view, new StringReader(commands)).run();

    InterfaceEvent copied = system.getCalendar("EST").getAllEvents().get(0);
    assertEquals(LocalDateTime.of(2025, 6, 15, 13, 0), copied.getStart());
    assertEquals(LocalDateTime.of(2025, 6, 15, 14, 0), copied.getEnd());
    assertEquals(60, copied.getDurationMinutes());
  }

  @Test
  public void testCopyRecurringGeneratesNewSeriesId() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Standup",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 9, 30),
        "M")
        .repeatCount(4)
        .build();
    system.createRecurringEvent("Source", request);

    LocalDate sourceDate = LocalDate.of(2025, 11, 10);
    LocalDate targetDate = LocalDate.of(2025, 12, 1);
    String originalSeriesId = system.getCalendar("Source").getAllEvents().get(0).getSeriesId();

    DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(sourceDate, targetDate);
    system.copyEventsInRange("Source", "Target", copyRequest);

    String newSeriesId = system.getCalendar("Target").getAllEvents().get(0).getSeriesId();
    assertNotEquals(originalSeriesId, newSeriesId);
    assertTrue(system.getCalendar("Target").getAllEvents().get(0).isPartOfSeries());
  }

  @Test
  public void testCopyEventsOnDate() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");
    system.createEvent("Source", "Meeting1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    system.createEvent("Source", "Meeting2",
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 30));

    DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(
        LocalDate.of(2025, 11, 15),
        LocalDate.of(2025, 11, 20));
    int count = system.copyEventsInRange("Source", "Target", copyRequest);

    assertEquals(2, count);
    InterfaceCalendar target = system.getCalendar("Target");
    assertEquals(2, target.getEventCount());
    List<InterfaceEvent> events = target.getAllEvents();
    assertEquals("Meeting1", events.get(0).getSubject());
    assertEquals(20, events.get(0).getStart().getDayOfMonth());
    assertEquals("Meeting2", events.get(1).getSubject());
    assertEquals(20, events.get(1).getStart().getDayOfMonth());
  }

  @Test
  public void testCopyEventsNoEvents() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");

    DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(
        LocalDate.of(2025, 11, 15),
        LocalDate.of(2025, 11, 20));
    int count = system.copyEventsInRange("Source", "Target", copyRequest);

    assertEquals(0, count);
    assertEquals(0, system.getCalendar("Target").getEventCount());
  }

  @Test
  public void testCopyEventsSkipsConflicts() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");
    system.createEvent("Source", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    system.createEvent("Target", "Meeting",
        LocalDateTime.of(2025, 11, 20, 10, 0),
        LocalDateTime.of(2025, 11, 20, 11, 0));

    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));

    try {
      DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(
          LocalDate.of(2025, 11, 15),
          LocalDate.of(2025, 11, 20));
      int count = system.copyEventsInRange("Source", "Target", copyRequest);

      assertEquals(0, count);
      String errorOutput = errContent.toString();
      assertTrue(errorOutput.contains("Skipped"));
    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  public void testCopyEventsBetween() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");
    system.createEvent("Source", "Event1",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0));
    system.createEvent("Source", "Event2",
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    system.createEvent("Source", "Event3",
        LocalDateTime.of(2025, 11, 25, 11, 0),
        LocalDateTime.of(2025, 11, 25, 12, 0));

    DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(
        LocalDate.of(2025, 11, 1),
        LocalDate.of(2025, 11, 20),
        LocalDate.of(2025, 12, 1));
    int count = system.copyEventsInRange("Source", "Target", copyRequest);

    assertEquals(2, count);
    InterfaceCalendar target = system.getCalendar("Target");
    assertEquals(2, target.getEventCount());
    for (InterfaceEvent event : target.getAllEvents()) {
      assertEquals(12, event.getStart().getMonthValue());
    }
  }

  @Test
  public void testCopyRecurringSeries() {
    system.createCalendar("Source", "UTC");
    system.createCalendar("Target", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Standup",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 9, 30),
        "MWF")
        .repeatCount(6)
        .build();
    system.createRecurringEvent("Source", request);

    DateRangeCopyRequest copyRequest = new DateRangeCopyRequest(
        LocalDate.of(2025, 11, 10),
        LocalDate.of(2025, 11, 15),
        LocalDate.of(2025, 12, 1));
    int count = system.copyEventsInRange("Source", "Target", copyRequest);

    assertTrue(count >= 3);
    InterfaceCalendar target = system.getCalendar("Target");
    List<InterfaceEvent> events = target.getAllEvents();
    String newSeriesId = events.get(0).getSeriesId();
    assertNotNull(newSeriesId);
    for (InterfaceEvent event : events) {
      assertTrue(event.isPartOfSeries());
      assertEquals(newSeriesId, event.getSeriesId());
    }
    String originalSeriesId = system.getCalendar("Source").getAllEvents().get(0).getSeriesId();
    assertNotEquals(originalSeriesId, newSeriesId);
  }

  @Test
  public void testCopyRecurringToSameCalendar() {
    system.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Weekly",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 9, 30),
        "M")
        .repeatCount(3)
        .build();
    system.createRecurringEvent("Work", request);

    EventCopyRequest copyRequest = new EventCopyRequest.Builder("Weekly")
        .sourceDateTime(LocalDateTime.of(2025, 11, 10, 9, 0))
        .targetCalendar("Work")
        .targetDateTime(LocalDateTime.of(2025, 12, 1, 10, 0))
        .build();
    system.copyEvent("Work", copyRequest);

    assertEquals(4, system.getCalendar("Work").getEventCount());
  }

  @Test
  public void testTimezoneConversion() {
    system.createCalendar("PST", "America/Los_Angeles");
    system.createCalendar("EST", "America/New_York");
    LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 15, 11, 0);
    system.createEvent("PST", "Meeting", start, end);

    EventCopyRequest copyRequest = new EventCopyRequest.Builder("Meeting")
        .sourceDateTime(start)
        .targetCalendar("EST")
        .targetDateTime(LocalDateTime.of(2025, 6, 15, 10, 0))
        .build();
    system.copyEvent("PST", copyRequest);

    InterfaceEvent copied = system.getCalendar("EST").getAllEvents().get(0);
    assertEquals(LocalDateTime.of(2025, 6, 15, 13, 0), copied.getStart());
  }
}