package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.commands.InterfaceCommand;
import calendar.controller.commands.ShowCalendarDashboardCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for SimpleCommandParser.
 * Tests parsing of simple commands like export, show status, and use calendar.
 */
public class SimpleCommandParserTest {

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
  public void testExport() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "export cal testcal.csv\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("exported"));
    new File("testcal.csv").delete();

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal testcal.ical\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("exported"));
    new File("testcal.ical").delete();

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal test.txt\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Unsupported file format"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "export cal test.csv\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testExportErrors() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export cal\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid export command"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "export calendar test.csv\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Invalid export command"));
  }

  @Test
  public void testShowStatus() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:30\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("BUSY"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("AVAILABLE"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:00\n"
        + "show status on 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("BUSY"));
    assertTrue(outputStream.toString().contains("AVAILABLE"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "show status on 2025-11-15T10:00\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }


  @Test
  public void testUseCalendar() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Personal --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event WorkMeeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "use calendar --name Personal\n"
        + "create event Lunch from 2025-11-15T12:00 to 2025-11-15T13:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());
    assertEquals(1, model.getCalendar("Personal").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "use calendar --name Nonexistent\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("does not exist"));
  }

  @Test
  public void testUseCalendarErrors() {
    String commands = "use calendar --name\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Usage: use calendar --name"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "use cal --name Work\nexit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Usage: use calendar --name"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar name Work\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("Usage: use calendar --name"));
  }



  @Test
  public void testParseShowStatusSuccess() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "status", "on", "2025-11-15T10:30"};
    InterfaceCommand command = parser.parseShowStatus(tokens, "show status on 2025-11-15T10:30");
    
    assertNotNull(command);
    assertTrue(command instanceof ShowStatusCommand);
    ShowStatusCommand statusCommand = (ShowStatusCommand) command;
    LocalDateTime expected = LocalDateTime.of(2025, 11, 15, 10, 30);
    assertEquals(expected, statusCommand.getDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusTooFewTokens() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "status", "on"};
    parser.parseShowStatus(tokens, "show status on");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusWrongSecondToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "wrong", "on", "2025-11-15T10:30"};
    parser.parseShowStatus(tokens, "show wrong on 2025-11-15T10:30");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowStatusWrongThirdToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "status", "at", "2025-11-15T10:30"};
    parser.parseShowStatus(tokens, "show status at 2025-11-15T10:30");
  }



  @Test
  public void testParseUseCalendarSuccess() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"use", "calendar", "--name", "Work"};
    InterfaceCommand command = parser.parseUseCalendar(tokens, "use calendar --name Work");
    
    assertNotNull(command);
    assertTrue(command instanceof UseCalendarCommand);
    UseCalendarCommand useCommand = (UseCalendarCommand) command;
    assertEquals("Work", useCommand.getCalendarName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarTooFewTokens() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"use", "calendar", "--name"};
    parser.parseUseCalendar(tokens, "use calendar --name");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarWrongSecondToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"use", "cal", "--name", "Work"};
    parser.parseUseCalendar(tokens, "use cal --name Work");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUseCalendarWrongThirdToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"use", "calendar", "name", "Work"};
    parser.parseUseCalendar(tokens, "use calendar name Work");
  }

  
  @Test
  public void testParseShowDashboardSuccess() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "from", "2025-11-15", "to", "2025-11-20"};
    InterfaceCommand command = parser.parseShowDashboard(tokens, 
        "show calendar dashboard from 2025-11-15 to 2025-11-20");
    
    assertNotNull(command);
    assertTrue(command instanceof ShowCalendarDashboardCommand);
    ShowCalendarDashboardCommand dashboardCommand = (ShowCalendarDashboardCommand) command;
    LocalDate expectedStart = LocalDate.of(2025, 11, 15);
    LocalDate expectedEnd = LocalDate.of(2025, 11, 20);
    assertEquals(expectedStart, dashboardCommand.getStartDate());
    assertEquals(expectedEnd, dashboardCommand.getEndDate());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardTooFewTokens() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "from", "2025-11-15"};
    parser.parseShowDashboard(tokens, "show calendar dashboard from 2025-11-15");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardWrongSecondToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "cal", "dashboard", "from", "2025-11-15", "to", "2025-11-20"};
    parser.parseShowDashboard(tokens, "show cal dashboard from 2025-11-15 to 2025-11-20");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardWrongThirdToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dash", "from", "2025-11-15", "to", "2025-11-20"};
    parser.parseShowDashboard(tokens, "show calendar dash from 2025-11-15 to 2025-11-20");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardWrongFourthToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "start", "2025-11-15", "to", "2025-11-20"};
    parser.parseShowDashboard(tokens, "show calendar dashboard start 2025-11-15 to 2025-11-20");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardWrongSixthToken() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "from", "2025-11-15", "until",
        "2025-11-20"};
    parser.parseShowDashboard(tokens, "show calendar dashboard from 2025-11-15 "
        + "until 2025-11-20");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseShowDashboardMissingEndDate() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "from", "2025-11-15", "to"};
    parser.parseShowDashboard(tokens, "show calendar dashboard from 2025-11-15 to");
  }

  @Test
  public void testParseShowDashboardWithExtraTokens() {
    SimpleCommandParser parser = new SimpleCommandParser();
    String[] tokens = {"show", "calendar", "dashboard", "from", "2025-11-15", "to", "2025-11-20",
        "extra"};
    InterfaceCommand command = parser.parseShowDashboard(tokens, 
        "show calendar dashboard from 2025-11-15 to 2025-11-20 extra");
    
    assertNotNull(command);
    assertTrue(command instanceof ShowCalendarDashboardCommand);
  }
}