package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
 * Test suite for Calendar.
 * Tests individual calendar functionality including events and timezone handling.
 */
public class CalendarTest {
  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up test fixtures before each test.
   */

  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testCreateCalendar() {
    String commands = "create calendar --name Work --timezone America/New_York\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    assertTrue(model.calendarExists("Work"));
    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals("Work", calendar.getName());
    assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());
  }

  @Test
  public void testEditCalendarName() {
    model.createCalendar("Work", "UTC");
    model.editCalendarName("Work", "NewWork");

    assertFalse(model.calendarExists("Work"));
    assertTrue(model.calendarExists("NewWork"));
  }

  @Test
  public void testEditTimezone() {
    model.createCalendar("Work", "UTC");
    model.editCalendarTimezone("Work", "America/Chicago");

    assertEquals(ZoneId.of("America/Chicago"), model.getCalendar("Work").getTimezone());
  }

  @Test
  public void testAddEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals(1, calendar.getEventCount());
    assertEquals("Meeting", calendar.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testAddMultipleEvents() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:00\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    assertEquals(2, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testAddRecurring() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to"
        + " 2025-11-10T09:30 repeats MWF for 6\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals(6, calendar.getEventCount());
    String seriesId = calendar.getAllEvents().get(0).getSeriesId();
    assertNotNull(seriesId);
    for (InterfaceEvent event : calendar.getAllEvents()) {
      assertTrue(event.isPartOfSeries());
      assertEquals(seriesId, event.getSeriesId());
    }
  }

  @Test
  public void testRemoveEvent() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertTrue(calendar.removeEvent("Meeting", start));
    assertEquals(0, calendar.getEventCount());
  }

  @Test
  public void testRemoveNotFound() {
    model.createCalendar("Work", "UTC");
    assertFalse(model.getCalendar("Work").removeEvent("NonExistent", LocalDateTime.now()));
  }

  @Test
  public void testEditSubject() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Meeting from 2025-11-15T10:00 with Updated\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent found = calendar.findEvent("Updated", LocalDateTime.of(2025, 11, 15, 10, 0));
    assertNotNull(found);
    assertEquals("Updated", found.getSubject());
  }

  @Test
  public void testEditLocation() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with Room101\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    InterfaceEvent found = model.getCalendar("Work").findEvent("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0));
    assertEquals("Room101", found.getLocation());
  }

  @Test
  public void testEditFromDate() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Meeting",
        LocalDateTime.of(2025, 11, 10, 14, 0),
        LocalDateTime.of(2025, 11, 10, 16, 0),
        "MWF")
        .repeatCount(6)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEventsFromDate("Meeting", LocalDateTime.of(2025, 11, 14, 14, 0),
        "start", "2025-11-14T15:00");

    List<InterfaceEvent> events = calendar.getAllEvents();
    assertTrue(events.size() >= 3);
    boolean hasChanged = events.stream().anyMatch(e -> e.getStart().getHour() == 15);
    assertTrue(hasChanged);
  }

  @Test
  public void testEditFromDateProperty() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Standup",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 9, 30),
        "MWF")
        .repeatCount(6)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEventsFromDate("Standup", LocalDateTime.of(2025, 11, 12, 9, 0),
        "location", "NewRoom");

    List<InterfaceEvent> events = calendar.getAllEvents();
    long withLocation = events.stream().filter(e -> !e.getLocation().isEmpty()).count();
    long withoutLocation = events.stream().filter(e -> e.getLocation().isEmpty()).count();
    assertTrue(withLocation > 0);
    assertTrue(withoutLocation > 0);
  }

  @Test
  public void testEditEntireSeries() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 repeats MWF for 6\n"
        + "edit series location Standup from 2025-11-10T09:00 with Room201\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    for (InterfaceEvent event : model.getCalendar("Work").getAllEvents()) {
      assertEquals("Room201", event.getLocation());
    }
  }

  @Test
  public void testEditNonSeries() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEntireSeries("Meeting", start, "location", "Room101");

    InterfaceEvent found = calendar.findEvent("Meeting", start);
    assertEquals("Room101", found.getLocation());
    assertFalse(found.isPartOfSeries());
  }

  @Test
  public void testGetOnDate() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting1",
        LocalDateTime.of(2025, 11, 15, 9, 0),
        LocalDateTime.of(2025, 11, 15, 10, 0));
    model.createEvent("Work", "Meeting2",
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    model.createEvent("Work", "Meeting3",
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0));

    List<InterfaceEvent> events = model.getCalendar("Work")
        .getEventsOnDate(LocalDate.of(2025, 11, 15));
    assertEquals(2, events.size());
  }

  @Test
  public void testGetInRange() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Event1",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0));
    model.createEvent("Work", "Event2",
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    model.createEvent("Work", "Event3",
        LocalDateTime.of(2025, 11, 25, 10, 0),
        LocalDateTime.of(2025, 11, 25, 11, 0));

    List<InterfaceEvent> events = model.getCalendar("Work").getEventsInRange(
        LocalDateTime.of(2025, 11, 1, 0, 0),
        LocalDateTime.of(2025, 11, 20, 23, 59));
    assertEquals(2, events.size());
  }

  @Test
  public void testIsBusy() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    assertTrue(model.getCalendar("Work").isBusyAt(LocalDateTime.of(2025, 11, 15, 10, 30)));
    assertFalse(model.getCalendar("Work").isBusyAt(LocalDateTime.of(2025, 11, 15, 9, 0)));
  }

  @Test
  public void testHasConflict() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceEvent conflicting = new Event("Meeting2",
        LocalDateTime.of(2025, 11, 15, 10, 30),
        LocalDateTime.of(2025, 11, 15, 11, 30));
    assertTrue(model.getCalendar("Work").hasConflict(conflicting));

    InterfaceEvent noConflict = new Event("Meeting2",
        LocalDateTime.of(2025, 11, 15, 11, 0),
        LocalDateTime.of(2025, 11, 15, 12, 0));
    assertFalse(model.getCalendar("Work").hasConflict(noConflict));
  }

  @Test
  public void testSplitSeriesDate() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Class",
        LocalDateTime.of(2025, 11, 10, 10, 0),
        LocalDateTime.of(2025, 11, 10, 12, 0),
        "MWF")
        .repeatCount(6)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEventsFromDate("Class", LocalDateTime.of(2025, 11, 14, 10, 0),
        "start", "2025-11-14T11:00");

    List<InterfaceEvent> events = calendar.getAllEvents();
    String firstId = events.get(0).getSeriesId();
    boolean multipleSeries = events.stream().anyMatch(e -> !e.getSeriesId().equals(firstId));
    assertTrue(multipleSeries);
  }

  @Test
  public void testConstructorValidation() {
    try {
      new Calendar(null, ZoneId.of("UTC"));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Calendar name cannot be null"));
    }

    try {
      new Calendar("  ", ZoneId.of("UTC"));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Calendar name cannot be null"));
    }

    try {
      new Calendar("Work", null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Timezone cannot be null"));
    }
  }

  @Test
  public void testSetterValidation() {
    model.createCalendar("Work", "UTC");
    InterfaceCalendar calendar = model.getCalendar("Work");

    try {
      calendar.setName(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Calendar name cannot be null"));
    }

    try {
      calendar.setName("  ");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Calendar name cannot be null"));
    }

    try {
      calendar.setTimezone(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Timezone cannot be null"));
    }

    try {
      calendar.addEvent(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Event cannot be null"));
    }

    try {
      calendar.addRecurringEvent(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Recurring event cannot be null"));
    }
  }

  @Test
  public void testFindEventNotFound() {
    model.createCalendar("Work", "UTC");
    assertNull(model.getCalendar("Work").findEvent("NoSuch", LocalDateTime.now()));
  }

  @Test
  public void testGetEventsEmpty() {
    model.createCalendar("Work", "UTC");
    assertTrue(model.getCalendar("Work").getEventsOnDate(LocalDate.now()).isEmpty());

    List<InterfaceEvent> events = model.getCalendar("Work").getEventsInRange(
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2025, 1, 31, 23, 59));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testSplitEntireSeriesPath() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-11T09:00 to 2025-11-11T10:00 repeats MTWRF for 5\n"
        + "edit series location Daily from 2025-11-11T09:00 with RoomB\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    List<InterfaceEvent> events = model.getCalendar("Work").getAllEvents();
    assertTrue(events.size() >= 5);
    String newSeriesId = events.get(0).getSeriesId();
    assertNotNull(newSeriesId);
    for (InterfaceEvent e : events) {
      assertEquals(newSeriesId, e.getSeriesId());
      assertEquals("RoomB", e.getLocation());
    }
  }

  @Test
  public void testEditEventNotFound() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event subject NonExistent from 2025-11-15T10:00 with New\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Event not found"));
  }

  @Test
  public void testPrintEventFeatures() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with RoomA\n"
        + "edit event description Meeting from 2025-11-15T10:00 with \"Important meeting\"\n"
        + "edit event status Meeting from 2025-11-15T10:00 with private\n"
        + "print events from 2025-11-15T00:00 to 2025-11-15T23:59\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("at RoomA"));
    assertTrue(output.contains("- Important meeting"));
    assertTrue(output.contains("[Private]"));
  }

  @Test
  public void testPrintRecurringSeries() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-15T10:00 to 2025-11-15T11:00 repeats M for 3\n"
        + "print events from 2025-11-15T00:00 to 2025-11-30T23:59\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("[Recurring]"));
  }

  @Test
  public void testHelpCommand() {
    String commands = "help\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("create event <subject> from <YYYY-MM-DDTHH:mm> to "
        + "<YYYY-MM-DDTHH:mm>"));
    assertTrue(output.contains("EVENT EDITING:"));
    assertTrue(output.contains("repeats <weekdays> for <N> times"));
    assertTrue(output.contains("EVENT COPYING:"));
    assertTrue(output.contains("repeats <weekdays> until <date>"));
    assertTrue(output.contains("QUERIES:"));
    assertTrue(output.contains("EXPORT:"));
    assertTrue(output.contains("export cal <filename.ical>"));
    assertTrue(output.contains("OTHER:"));
    assertTrue(output.contains("exit"));
    assertTrue(output.contains("NOTES:"));
    assertTrue(output.contains("Properties: subject, start, end, description, location, status"));
  }

  @Test
  public void testSplitEntireSeries() {
    InterfaceCalendar cal = new Calendar("Work", ZoneId.of("UTC"));

    InterfaceRecurringEvent recurring = new RecurringEvent.Builder(
        "Meeting",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0),
        "MWF")
        .repeatCount(3)
        .build();

    cal.addRecurringEvent(recurring);

    String originalSeriesId = cal.getAllEvents().get(0).getSeriesId();

    cal.editEntireSeries("Meeting", LocalDateTime.of(2025, 11, 10, 9, 0),
        "start", "2025-11-10T08:00");

    List<InterfaceEvent> events = cal.getAllEvents();

    for (InterfaceEvent event : events) {
      assertEquals(8, event.getStart().getHour());
      assertNotEquals(originalSeriesId, event.getSeriesId());
      assertTrue(event.isPartOfSeries());
    }

    String newSeriesId = events.get(0).getSeriesId();
    for (InterfaceEvent event : events) {
      assertEquals(newSeriesId, event.getSeriesId());
    }
  }

  @Test
  public void testRecurringDuplicates() {
    InterfaceCalendar cal = new Calendar("Work", ZoneId.of("UTC"));

    InterfaceEvent blocking = new Event("Meeting",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0));
    cal.addEvent(blocking);

    InterfaceRecurringEvent recurring = new RecurringEvent.Builder(
        "Meeting",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0),
        "MWF")
        .repeatCount(3)
        .build();

    try {
      cal.addRecurringEvent(recurring);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("creates duplicate event on"));
      assertTrue(e.getMessage().contains("2025-11-10"));
    }

    InterfaceCalendar cal2 = new Calendar("Work2", ZoneId.of("UTC"));
    InterfaceEvent blocking2 = new Event("Meeting",
        LocalDateTime.of(2025, 11, 12, 9, 0),
        LocalDateTime.of(2025, 11, 12, 10, 0));
    cal2.addEvent(blocking2);

    try {
      cal2.addRecurringEvent(recurring);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("creates duplicate event on"));
      assertTrue(e.getMessage().contains("2025-11-12"));
    }
  }

  @Test
  public void testSplitSeriesAllConditions() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "DailyStandup",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0),
        "MTWRF")
        .repeatCount(5)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEventsFromDate("DailyStandup", LocalDateTime.of(2025, 11, 12, 9, 0),
        "start", "2025-11-12T08:30");

    List<InterfaceEvent> events = calendar.getAllEvents();
    boolean hasOriginalTime = events.stream().anyMatch(e -> e.getStart().getHour() == 9);
    boolean hasNewTime = events.stream().anyMatch(e -> e.getStart().getHour() == 8);
    assertTrue(hasOriginalTime);
    assertTrue(hasNewTime);

    String firstSeriesId = events.get(0).getSeriesId();
    boolean hasMultipleSeries = events.stream().anyMatch(e ->
        !e.getSeriesId().equals(firstSeriesId));
    assertTrue(hasMultipleSeries);
  }

  @Test
  public void testEditEntireSeriesAllConditions() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "TeamMeeting",
        LocalDateTime.of(2025, 11, 10, 14, 0),
        LocalDateTime.of(2025, 11, 10, 16, 0),
        "MW")
        .repeatCount(4)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    String originalSeriesId = calendar.getAllEvents().get(0).getSeriesId();

    calendar.editEntireSeries("TeamMeeting", LocalDateTime.of(2025, 11, 10, 14, 0),
        "start", "2025-11-10T15:00");

    List<InterfaceEvent> events = calendar.getAllEvents();
    assertTrue(events.size() > 0);

    for (InterfaceEvent event : events) {
      assertEquals(15, event.getStart().getHour());
      assertNotEquals(originalSeriesId, event.getSeriesId());
      assertTrue(event.isPartOfSeries());
    }

    String newSeriesId = events.get(0).getSeriesId();
    for (InterfaceEvent event : events) {
      assertEquals(newSeriesId, event.getSeriesId());
    }
  }

  @Test
  public void testEditNonSeriesEvents() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");

    calendar.editEventsFromDate("Meeting", start, "location", "RoomZ");
    InterfaceEvent event = calendar.findEvent("Meeting", start);
    assertEquals("RoomZ", event.getLocation());
    assertFalse(event.isPartOfSeries());

    calendar.editEntireSeries("Meeting", start, "description", "Updated");
    event = calendar.findEvent("Meeting", start);
    assertEquals("Updated", event.getDescription());
    assertFalse(event.isPartOfSeries());
  }

  @Test
  public void testEditIgnoresOtherSeries() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request1 = new RecurringEventCreationRequest.Builder(
        "Series1",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0),
        "MWF")
        .repeatCount(3)
        .build();
    model.createRecurringEvent("Work", request1);

    RecurringEventCreationRequest request2 = new RecurringEventCreationRequest.Builder(
        "Series2",
        LocalDateTime.of(2025, 11, 10, 11, 0),
        LocalDateTime.of(2025, 11, 10, 12, 0),
        "MWF")
        .repeatCount(3)
        .build();
    model.createRecurringEvent("Work", request2);

    InterfaceCalendar calendar = model.getCalendar("Work");
    String series2OriginalId = calendar.findEvent("Series2",
        LocalDateTime.of(2025, 11, 10, 11, 0)).getSeriesId();

    calendar.editEventsFromDate("Series1", LocalDateTime.of(2025, 11, 12, 9, 0),
        "location", "Room1");

    for (InterfaceEvent e : calendar.getAllEvents()) {
      if (e.getSubject().equals("Series2")) {
        assertEquals("", e.getLocation());
        assertEquals(series2OriginalId, e.getSeriesId());
      }
    }

    calendar.editEntireSeries("Series1", LocalDateTime.of(2025, 11, 10, 9, 0),
        "location", "Building1");

    for (InterfaceEvent e : calendar.getAllEvents()) {
      if (e.getSubject().equals("Series2")) {
        assertEquals("", e.getLocation());
        assertEquals(series2OriginalId, e.getSeriesId());
      }
    }
  }

  @Test
  public void testEditInvalidProperty() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    try {
      calendar.editEvent("Meeting", start, "invalidProperty", "value");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid property"));
    }
  }

  @Test
  public void testHasConflictNullInputs() {
    model.createCalendar("Work", "UTC");
    InterfaceCalendar calendar = model.getCalendar("Work");

    assertFalse(calendar.hasConflict(null));
  }

  @Test
  public void testAddEventDuplicate() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);

    model.createEvent("Work", "Meeting", start, end);

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent duplicate = new Event("Meeting", start, end);

    try {
      calendar.addEvent(duplicate);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("already exists"));
    }
  }

  @Test
  public void testSetTimezoneSameTimezone() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime originalStart = calendar.getAllEvents().get(0).getStart();

    calendar.setTimezone(ZoneId.of("UTC"));

    assertEquals(originalStart, calendar.getAllEvents().get(0).getStart());
  }

  @Test
  public void testSetTimezoneDifferentTimezone() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.setTimezone(ZoneId.of("America/New_York"));

    InterfaceEvent event = calendar.getAllEvents().get(0);
    assertNotEquals(LocalDateTime.of(2025, 11, 15, 10, 0), event.getStart());
  }

  @Test
  public void testEditEntireSeriesNotFound() {
    model.createCalendar("Work", "UTC");
    InterfaceCalendar calendar = model.getCalendar("Work");

    try {
      calendar.editEntireSeries("NonExistent", LocalDateTime.of(2025, 11, 15, 10, 0),
          "location", "Room");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Event not found"));
    }
  }

  @Test
  public void testEditEventsFromDateNotFound() {
    model.createCalendar("Work", "UTC");
    InterfaceCalendar calendar = model.getCalendar("Work");

    try {
      calendar.editEventsFromDate("NonExistent", LocalDateTime.of(2025, 11, 15, 10, 0),
          "location", "Room");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Event not found"));
    }
  }

  @Test
  public void testEditDescriptionWithNull() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEvent("Meeting", start, "description", null);

    InterfaceEvent event = calendar.findEvent("Meeting", start);
    assertEquals("", event.getDescription());
  }

  @Test
  public void testEditLocationWithNull() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    model.createEvent("Work", "Meeting", start, LocalDateTime.of(2025, 11, 15, 11, 0));

    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEvent("Meeting", start, "location", null);

    InterfaceEvent event = calendar.findEvent("Meeting", start);
    assertEquals("", event.getLocation());
  }

  @Test
  public void testIsDuplicateAllFields() {
    model.createCalendar("Work", "UTC");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);

    model.createEvent("Work", "Meeting", start, end);

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent exactDuplicate = new Event("Meeting", start, end);

    try {
      calendar.addEvent(exactDuplicate);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("already exists"));
    }
  }

  @Test
  public void testEditSeriesWithStartProperty() {
    model.createCalendar("Work", "UTC");

    RecurringEventCreationRequest request = new RecurringEventCreationRequest.Builder(
        "Series",
        LocalDateTime.of(2025, 11, 10, 9, 0),
        LocalDateTime.of(2025, 11, 10, 10, 0),
        "MWF")
        .repeatCount(6)
        .build();
    model.createRecurringEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");

    calendar.editEventsFromDate("Series", LocalDateTime.of(2025, 11, 12, 9, 0),
        "start", "2025-11-12T08:30");

    List<InterfaceEvent> events = calendar.getAllEvents();
    boolean hasOriginal = events.stream()
        .anyMatch(e -> e.getSeriesId().equals(events.get(0).getSeriesId())
            && e.getStart().getHour() == 9);
    boolean hasChanged = events.stream()
        .anyMatch(e -> !e.getSeriesId().equals(events.get(0).getSeriesId())
            && e.getStart().getHour() == 8);

    assertTrue(hasOriginal || hasChanged);
  }
}