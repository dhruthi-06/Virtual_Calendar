package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
 * Test suite for EditCommandParser.
 * Tests parsing of edit commands for calendars and events.
 */
public class EditCommandParserTest {

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
  public void testEditCalendar() {
    String commands = "create calendar --name OldName --timezone UTC\n"
        + "edit calendar --name OldName --property name NewName\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertFalse(model.calendarExists("OldName"));
    assertTrue(model.calendarExists("NewName"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone America/New_York\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals("America/New_York", model.getCalendar("Work").getTimezone().getId());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --property name NewName --name Work\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("NewName"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name A --timezone UTC\n"
        + "edit calendar --name A --property name B\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("B"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone Europe/London\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals("Europe/London", model.getCalendar("Work").getTimezone().getId());
  }

  @Test
  public void testEditCalendarErrors() {
    String commands = "edit\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --property timezone America/New_York\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "edit invalid\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));
  }

  @Test
  public void testEditEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event OldMeeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject OldMeeting from 2025-11-15T10:00 with NewMeeting\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertNotNull(model.getCalendar("Work").findEvent("NewMeeting",
        java.time.LocalDateTime.parse("2025-11-15T10:00")));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event start Meeting from 2025-11-15T10:00 with 2025-11-15T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        java.time.LocalDateTime.parse("2025-11-15T09:00")));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with \"Conference Room A\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with \"Important discussion\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Meeting from 2025-11-15T10:00 with private\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with Room\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event end Meeting from 2025-11-15T10:00 to 2025-11-15T11:00 with 2025-11-15T12:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));
  }

  @Test
  public void testEditEventErrors() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event subject Nonexistent from 2025-11-15T10:00 with NewName\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Event not found"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event subject Meeting with New\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Missing 'from'"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Meeting from 2025-11-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Missing 'with'"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event location\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testEditSeries() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-15T09:00 to 2025-11-15T09:30 repeats MWF for 5 times\n"
        + "edit events location Standup from 2025-11-17T09:00 with RoomB\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Events in series"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-17T10:00 to 2025-11-17T11:00 repeats MTWRF for 5 times\n"
        + "edit series location Daily from 2025-11-17T10:00 with Building2\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Entire event series"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-15T09:00 to 2025-11-15T09:30 repeats MWF for 5 times\n"
        + "edit events location Standup from 2025-11-17T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Missing 'with'"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-17T10:00 to 2025-11-17T11:00 repeats MTWRF for 5 times\n"
        + "edit series location Daily from 2025-11-17T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Missing 'with'"));
  }

  @Test
  public void testEditInvalidMode() {
    EditCommandParser parser = new EditCommandParser();
    try {
      parser.parse(new String[]{"edit", "invalidmode"},
          "edit invalidmode location Meeting from 2025-11-15T10:00 with Room");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid edit sub-command"));
    }
  }
}