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
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CommandParser.
 * Tests command parsing functionality with various command formats.
 */
public class CommandParserTest {
  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

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
  public void testParseEmpty() {
    CommandParser parser = new CommandParser();

    try {
      parser.parse(null);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Empty command"));
    }

    try {
      parser.parse("");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Empty command"));
    }

    try {
      parser.parse("   ");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Empty command"));
    }
  }

  @Test
  public void testParseUnknown() {
    CommandParser parser = new CommandParser();

    try {
      parser.parse("unknown");
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Unknown command"));
    }

    String commands = "invalidcommand\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));
  }

  @Test
  public void testParseHelp() {
    CommandParser parser = new CommandParser();
    InterfaceCommand cmd = parser.parse("help");
    assertNotNull(cmd);
    assertFalse(cmd.isExitCommand());

    cmd = parser.parse("HELP");
    assertNotNull(cmd);

    cmd = parser.parse("HeLp");
    assertNotNull(cmd);
  }

  @Test
  public void testParseExit() {
    CommandParser parser = new CommandParser();
    InterfaceCommand cmd = parser.parse("exit");
    assertNotNull(cmd);
    assertTrue(cmd.isExitCommand());

    cmd = parser.parse("EXIT");
    assertTrue(cmd.isExitCommand());

    cmd = parser.parse("ExIt");
    assertTrue(cmd.isExitCommand());
  }

  @Test
  public void testCreateCalendar() {
    String commands = "create calendar --name Work --timezone UTC\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Work"));

    outputStream.reset();
    commands = "create calendar --timezone UTC --name Personal\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Personal"));

    outputStream.reset();
    commands = "create calendar --name Test\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    commands = "create calendar --timezone UTC\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    commands = "create calendar\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    commands = "create\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));

    outputStream.reset();
    commands = "create invalid\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));
  }

  @Test
  public void testCreateEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Team Meeting\" from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2024-12-25\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Meeting from 2024-01-15T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testCreateRecurringEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2024-01-15T09:00 to 2"
        + "024-01-15T09:30 repeats MWF for 10 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.getCalendar("Work").getEventCount() >= 10);

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2024-01-15T10:00 to "
        + "2024-01-15T11:00 repeats Mon until 2024-06-01\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("created"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2024-01-01 repeats Mon until 2024-12-31\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("created"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T09:00 to 2024-01-15T10:00 repeats invalid\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testEditCalendar() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property name NewWork\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("NewWork"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone America/Chicago\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("changed")
        || outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "edit\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));

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
        + "create event Meeting from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "edit event subject Meeting from 2024-01-15T09:00 with NewMeeting\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "edit event location Meeting from 2024-01-15T09:00 with Room\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event subject Meeting with New\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testPrintEvents() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T10:00 to 2024-01-15T11:00\n"
        + "print events on 2024-01-15\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Meeting"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T10:00 to 2024-01-15T11:00\n"
        + "print events from 2024-01-15T09:00 to 2024-01-20T17:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Meeting"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "print events\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "print\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));
  }

  @Test
  public void testExportCommands() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal output.csv\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("exported")
        || outputStream.toString().contains("success"));
    new File("output.csv").delete();

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal output.ical\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("exported"));
    new File("output.ical").delete();

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "export calendar output.csv\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testShowStatus() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "show status on 2024-01-15T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("AVAILABLE")
        || outputStream.toString().contains("BUSY"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "show status\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "show status on\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testUseCalendar() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("using"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "use calendar --name\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "use calendar Work\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "use calendar\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testCopyEvent() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "copy event Meeting on 2024-01-15T09:00 --target Work to 2024-01-16T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("copied")
        || outputStream.toString().contains("Event"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Team Meeting\" from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "copy event \"Team Meeting\" on 2024-01-15T09:00 --target Work to 2024-01-16T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("copied")
        || outputStream.toString().contains("Event"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "copy\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "copy invalid\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid"));
  }

  @Test
  public void testCopyEvents() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T10:00 to 2024-01-15T11:00\n"
        + "copy events on 2024-01-15 --target Work to 2024-01-20\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("copied"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "copy events on 2024-01-15 to 2024-01-20\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2024-01-15T10:00 to 2024-01-15T11:00\n"
        + "copy events between 2024-01-15 and 2024-01-20 --target Work to 2024-02-01\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("copied"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "copy events between 2024-01-15 and 2024-01-20 to 2024-02-01\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testSpecialCharacters() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Meeting @ Office #1\" from 2024-01-15T09:00 to 2024-01-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name \"Work Calendar\" --timezone UTC\n"
        + "use calendar --name \"Work Calendar\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Work Calendar"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "   help   \nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().length() > 100);
  }
}