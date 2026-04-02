package calendar.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.Event;
import calendar.model.EventCreationRequest;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CalendarTextView.
 * Tests text-based view output formatting.
 */
public class CalendarTextViewTest {
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

  @Test(expected = IllegalArgumentException.class)
  public void testNullOutput() {
    new CalendarTextView(null);
  }

  @Test
  public void testConstructors() {
    assertNotNull(new CalendarTextView());

    CalendarTextView testView = new CalendarTextView(new PrintStream(outputStream));
    assertNotNull(testView.getOutputStream());
  }

  @Test
  public void testDisplayMessageAndError() {
    String commands = "create calendar --name Work --timezone UTC\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Calendar created"));

    outputStream.reset();
    commands = "create calendar --name Work\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR:"));
  }

  @Test
  public void testDisplayEventsEmpty() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events on 2025-11-15\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("No events"));
  }

  @Test
  public void testDisplayMultipleEvents() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "print events on 2025-11-15\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Meeting1"));
    assertTrue(output.contains("Meeting2"));
  }

  @Test
  public void testDisplayAllDayEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "print events on 2025-12-25\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Holiday"));
    assertTrue(output.contains("All Day"));
  }

  @Test
  public void testDisplayEventWithLocation() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with Room101\n"
        + "print events on 2025-11-15\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Room101"));
    assertTrue(output.contains("at"));
  }

  @Test
  public void testDisplayRecurringEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 repeats M for 3\n"
        + "print events on 2025-11-10\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Standup"));
    assertTrue(output.contains("Recurring"));
  }

  @Test
  public void testDisplayRangeEmpty() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("No events found between"));
  }

  @Test
  public void testDisplayEventsInRange() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Events between"));
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("starting on"));
    assertTrue(output.contains("ending on"));
  }

  @Test
  public void testDisplayEventDescription() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with Important\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Important"));
    assertTrue(output.contains(" - "));
  }

  @Test
  public void testDisplayPrivateEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Private from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Private from 2025-11-15T10:00 with private\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("[Private]"));
  }

  @Test
  public void testDisplayStatus() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:30\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Status at"));
    assertTrue(output.contains("BUSY"));

    InterfaceCalendarSystem model2 = new CalendarSystem();
    ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
    InterfaceCalendarView view2 = new CalendarTextView(new PrintStream(outputStream2));

    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "show status on 2025-11-15T10:30\nexit\n";
    new CalendarController(model2, view2, new StringReader(commands)).run();
    output = outputStream2.toString();
    assertTrue(output.contains("Status at"));
    assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testDisplayWelcome() {
    view.displayWelcome();
    String output = outputStream.toString();
    assertTrue(output.contains("Welcome"));
    assertTrue(output.contains("Calendar Application"));
    assertTrue(output.contains("help"));
    assertTrue(output.contains("exit"));

    long lineCount = output.lines().count();
    assertTrue(lineCount >= 2);

    long emptyLines = output.lines().filter(String::isEmpty).count();
    assertEquals(1, emptyLines);
  }

  @Test
  public void testDisplayHelp() {
    outputStream.reset();
    view.displayHelp();
    String output = outputStream.toString();
    String[] lines = output.split("\n");
    assertTrue(lines.length > 30);
    assertTrue(output.contains("AVAILABLE COMMANDS"));
    assertTrue(output.contains("CALENDAR MANAGEMENT:"));
    assertTrue(output.contains("EVENT CREATION:"));
    assertTrue(output.contains("EVENT EDITING:"));
    assertTrue(output.contains("EVENT COPYING:"));
    assertTrue(output.contains("QUERIES:"));
    assertTrue(output.contains("EXPORT:"));
    assertTrue(output.contains("OTHER:"));
    assertTrue(output.contains("NOTES:"));
    assertTrue(output.contains("create calendar --name"));
    assertTrue(output.contains("edit calendar --name"));
    assertTrue(output.contains("use calendar --name"));
    assertTrue(output.contains("create event <subject> from"));
    assertTrue(output.contains("create event <subject> on"));
    assertTrue(output.contains("repeats <weekdays> for"));
    assertTrue(output.contains("repeats <weekdays> until"));
    assertTrue(output.contains("edit event <property>"));
    assertTrue(output.contains("edit events <property>"));
    assertTrue(output.contains("edit series <property>"));
    assertTrue(output.contains("copy event <name>"));
    assertTrue(output.contains("copy events on"));
    assertTrue(output.contains("copy events between"));
    assertTrue(output.contains("print events on"));
    assertTrue(output.contains("print events from"));
    assertTrue(output.contains("show status on"));
    assertTrue(output.contains("export cal <filename.csv>"));
    assertTrue(output.contains("export cal <filename.ical>"));
    assertTrue(output.contains("Weekday Codes:"));
    assertTrue(output.contains("Multi-word subjects"));
    assertTrue(output.contains("Timezones use IANA"));
    assertTrue(output.contains("Properties:"));
    assertTrue(output.contains("help"));
    assertTrue(output.contains("exit"));

    long emptyLinesCount = output.lines().filter(String::isEmpty).count();
    assertTrue(emptyLinesCount >= 8);

    String[] linesArray = output.split("\n", -1);
    int emptyLineCount = 0;
    for (String line : linesArray) {
      if (line.trim().isEmpty()) {
        emptyLineCount++;
      }
    }
    assertTrue(emptyLineCount >= 11);
  }

  @Test
  public void testDisplayCalendarList() {
    view.displayCalendarList(model.getAllCalendarNames());
    assertTrue(outputStream.toString().contains("No calendars found"));

    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Personal --timezone UTC\n"
        + "create calendar --name School --timezone UTC\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    outputStream.reset();
    view.displayCalendarList(model.getAllCalendarNames());
    String output = outputStream.toString();
    assertTrue(output.contains("Work"));
    assertTrue(output.contains("Personal"));
    assertTrue(output.contains("School"));
    assertTrue(output.contains("Available Calendars:"));
  }

  @Test
  public void testDisplayCalendarInfo() {
    outputStream.reset();
    view.displayCalendarInfo("Work", "America/New_York", 2);
    String output = outputStream.toString();
    assertTrue(output.contains("Calendar: Work"));
    assertTrue(output.contains("Timezone: America/New_York"));
    assertTrue(output.contains("Events: 2"));

    String[] lines = output.split("\n");
    assertTrue(lines.length >= 3);

    outputStream.reset();
    view.displayCalendarInfo("TestCal", "UTC", 5);
    output = outputStream.toString();
    assertTrue(output.contains("TestCal"));
    assertTrue(output.contains("UTC"));
    assertTrue(output.contains("5"));
  }

  @Test
  public void testNullHandling() {
    view.displayMessage("");
    view.displayMessage(null);
    view.displayError("");
    view.displayError(null);
    view.displayEvents(null);
    view.displayCalendarList(null);
    String output = outputStream.toString();
    assertTrue(output.isEmpty() || output.contains("No"));

    outputStream.reset();
    view.displayEvents(null);
    assertTrue(outputStream.toString().contains("No events found"));

    outputStream.reset();
    view.displayEvents(new ArrayList<>());
    assertTrue(outputStream.toString().contains("No events found"));
  }

  @Test
  public void testCompleteWorkflow() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Morning from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Lunch from 2025-11-15T12:00 to 2025-11-15T13:00\n"
        + "print events on 2025-11-15\n"
        + "show status on 2025-11-15T09:30\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    String output = outputStream.toString();
    assertTrue(output.contains("Morning"));
    assertTrue(output.contains("Lunch"));
    assertTrue(output.contains("BUSY"));
  }

  @Test
  public void testDisplayEventsForDate() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    outputStream.reset();
    view.displayEventsForDate(model.getCalendar("Work").getAllEvents(), "2025-11-15");
    String output = outputStream.toString();
    assertTrue(output.contains("Events on 2025-11-15"));
    assertTrue(output.contains("Meeting"));

    outputStream.reset();
    view.displayEventsForDate(new ArrayList<>(), "2025-11-15");
    assertTrue(outputStream.toString().contains("No events"));
  }

  @Test
  public void testDisplayRangeWithProperties() {
    model.createCalendar("Work", "UTC");

    EventCreationRequest request = new EventCreationRequest.Builder(
        "Review",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0))
        .location("Office")
        .description("Important")
        .isPublic(true)
        .build();
    model.createEvent("Work", request);

    outputStream.reset();
    view.displayEventsInRange(model.getCalendar("Work").getAllEvents(),
        "2025-11-01", "2025-11-30");
    String output = outputStream.toString();
    assertTrue(output.contains("Office"));
    assertTrue(output.contains(" at "));
    assertTrue(output.contains("Important"));
    assertTrue(output.contains(" - "));

    EventCreationRequest privateRequest = new EventCreationRequest.Builder(
        "Secret",
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0))
        .isPublic(false)
        .build();
    model.createEvent("Work", privateRequest);

    outputStream.reset();
    view.displayEventsInRange(model.getCalendar("Work").getAllEvents(),
        "2025-11-01", "2025-11-30");
    assertTrue(outputStream.toString().contains("[Private]"));
  }

  @Test
  public void testDisplayRangeRecurring() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-10T09:00 to 2025-11-10T09:30 repeats M for 2\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Recurring"));
  }

  @Test
  public void testFormatEventNoOptionalFields() {
    model.createCalendar("Work", "UTC");
    InterfaceEvent event = new Event("Regular",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    model.getCalendar("Work").addEvent(event);

    outputStream.reset();
    view.displayEventsForDate(model.getCalendar("Work").getAllEvents(), "2025-11-15");
    String output = outputStream.toString();
    assertTrue(output.contains("Regular"));
    assertFalse(output.contains("[Recurring]"));

    model.createCalendar("Test", "UTC");
    model.createEvent("Test", "Sync",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    outputStream.reset();
    view.displayEventsInRange(model.getCalendar("Test").getAllEvents(),
        "2025-11-01", "2025-11-30");
    output = outputStream.toString();
    assertTrue(output.contains("Sync"));
    int atCount = output.split(" at ", -1).length - 1;
    assertTrue(atCount == 2);
    assertFalse(output.contains(" - "));
    assertFalse(output.contains("[Recurring]"));
  }

  @Test
  public void testDisplayEventsGeneric() {
    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    model.createEvent("Work", "Lunch",
        LocalDateTime.of(2025, 11, 15, 12, 0),
        LocalDateTime.of(2025, 11, 15, 13, 0));

    InterfaceCalendar cal = model.getCalendar("Work");
    List<InterfaceEvent> events = cal.getAllEvents();

    ByteArrayOutputStream viewOutput = new ByteArrayOutputStream();
    InterfaceCalendarView testView = new CalendarTextView(new PrintStream(viewOutput));
    testView.displayEvents(events);

    String output = viewOutput.toString();
    assertTrue(output.contains("Events:"));
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Lunch"));
  }

  @Test
  public void testFormatEventAllProperties() {
    model.createCalendar("Work", "UTC");

    EventCreationRequest request = new EventCreationRequest.Builder(
        "CompleteEvent",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0))
        .location("Room 101")
        .description("Team sync")
        .isPublic(false)
        .build();
    model.createEvent("Work", request);

    InterfaceCalendar calendar = model.getCalendar("Work");
    List<InterfaceEvent> events = calendar.getAllEvents();
    InterfaceEvent event = events.get(0);
    event.setSeriesId("test-series");

    outputStream.reset();
    view.displayEvents(events);
    String output = outputStream.toString();

    assertTrue(output.contains("CompleteEvent"));
    assertTrue(output.contains("Room 101"));
    assertTrue(output.contains("Team sync"));
    assertTrue(output.contains("[Recurring]"));
    assertTrue(output.contains("[Private]"));

    model.createCalendar("Test", "UTC");
    model.createEvent("Test", "NoLocation",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    outputStream.reset();
    view.displayEvents(model.getCalendar("Test").getAllEvents());
    output = outputStream.toString();
    assertTrue(output.contains("NoLocation"));
    int atCount = output.split(" at ", -1).length - 1;
    assertEquals(2, atCount);

    model.createCalendar("Test2", "UTC");
    model.createEvent("Test2", "NoDesc",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    outputStream.reset();
    view.displayEvents(model.getCalendar("Test2").getAllEvents());
    output = outputStream.toString();
    assertTrue(output.contains("NoDesc"));
    assertFalse(output.contains(" - "));

    model.createCalendar("Test3", "UTC");
    EventCreationRequest publicRequest = new EventCreationRequest.Builder(
        "PublicEvent",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0))
        .isPublic(true)
        .build();
    model.createEvent("Test3", publicRequest);

    outputStream.reset();
    view.displayEvents(model.getCalendar("Test3").getAllEvents());
    output = outputStream.toString();
    assertTrue(output.contains("PublicEvent"));
    assertFalse(output.contains("[Private]"));
  }

  @Test
  public void testDisplayRangeEmptyWrapper() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CalendarTextView view = new CalendarTextView(new PrintStream(outputStream));

    view.displayEventsInRange(new ArrayList<>(), "2025-11-15", "2025-11-20");

    String output = outputStream.toString();
    assertTrue(output.contains("No events found between"));
    assertTrue(output.contains("2025-11-15"));
    assertTrue(output.contains("2025-11-20"));
  }
}