package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.commands.InterfaceCommand;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CreateCommandParser.
 * Tests parsing of create commands for calendars and events.
 */
public class CreateCommandParserTest {

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
    assertEquals("America/New_York", model.getCalendar("Work").getTimezone().getId());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --timezone UTC --name Personal\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Personal"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC extra\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Work"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertFalse(model.calendarExists("Work"));
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Work --timezone UTC\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("already exists"));
  }

  @Test
  public void testCreateEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());
    assertTrue(outputStream.toString().contains("Event created"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Team Meeting\" from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("No calendar in use"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025/11/15 to 2025/11/15\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testCreateRecurring() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-15T09:00 "
        + "to 2025-11-15T09:30 repeats MWF for 5 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(5, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-17T12:00 "
        + "to 2025-11-17T13:00 repeats M until 2025-12-15\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.getCalendar("Work").getEventCount() >= 4);

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-11-15 repeats M for 2 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-15T10:00 to 2025-11-15T11:00 repeats MTWRF for 3 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(3, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDay on 2025-11-18 repeats T until 2025-12-09\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.getCalendar("Work").getEventCount() >= 3);
  }

  @Test
  public void testParserBoundaries() {
    CreateCommandParser parser = new CreateCommandParser();

    try {
      parser.parse(new String[]{"create"}, "create");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid"));
    }

    try {
      parser.parse(new String[]{"create", "calendar"}, "create calendar");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Usage"));
    }

    try {
      parser.parse(new String[]{"create", "calendar", "--name", "Work"},
          "create calendar --name Work");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Usage"));
    }

    try {
      parser.parse(new String[]{"create", "calendar", "--timezone", "UTC"},
          "create calendar --timezone UTC");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Usage"));
    }

    InterfaceCommand cmd = parser.parse(
        new String[]{"create", "event", "Holiday",
            "on", "2025-12-25", "repeats", "F", "for", "4", "times"},
        "create event Holiday on 2025-12-25 repeats F for 4 times");
    assertNotNull(cmd);
  }
}