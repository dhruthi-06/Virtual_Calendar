package calendar.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.commands.InterfaceCommand;
import calendar.controller.commands.PrintEventsCommand;
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
 * Integrated tests for PrintCommandParser.
 */
public class PrintCommandParserTest {

  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up the test fixtures.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testPrintOnDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Morning from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Afternoon from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Events on 2025-11-15"));
    assertTrue(output.contains("Morning"));
    assertTrue(output.contains("Afternoon"));
  }

  @Test
  public void testPrintInRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Day1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Day2 from 2025-11-16T09:00 to 2025-11-16T10:00\n"
        + "create event Day3 from 2025-11-17T09:00 to 2025-11-17T10:00\n"
        + "print events from 2025-11-15T00:00 to 2025-11-17T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Events between"));
    assertTrue(output.contains("Day1"));
    assertTrue(output.contains("Day2"));
    assertTrue(output.contains("Day3"));
  }

  @Test
  public void testPrintNoEventsOnDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("No events scheduled on 2025-11-15"));
  }

  @Test
  public void testPrintNoEventsInRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events from 2025-11-15T00:00 to 2025-11-17T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("No events found between"));
  }

  @Test
  public void testPrintAllDayEventsIntegrated() {
    String commands = "create calendar --name Personal --timezone UTC\n"
        + "use calendar --name Personal\n"
        + "create event Holiday on 2025-12-25\n"
        + "print events on 2025-12-25\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Holiday"));
    assertTrue(output.contains("All Day"));
  }

  @Test
  public void testPrintRecurringEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-15T09:00 to 2025-11-15T09:30 "
        + "repeats MWF for 3 times\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Standup"));
    assertTrue(output.contains("Recurring"));
  }

  @Test
  public void testPrintEventsWithLocationIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with RoomA\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("RoomA"));
  }

  @Test
  public void testPrintWithoutCalendarIntegrated() {
    String commands = "print events on 2025-11-15\nexit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testPrintMultiDayEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Conference from 2025-11-15T09:00 to 2025-11-17T17:00\n"
        + "print events from 2025-11-15T00:00 to 2025-11-20T00:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Conference"));
  }

  @Test
  public void testPrintTwoTokensIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR") || output.contains("Invalid"));
  }

  @Test
  public void testParsePrintEventsOnDate() {
    CommandParser parser = new CommandParser();

    String command = "print events on 2025-01-15";
    InterfaceCommand cmd = parser.parse(command);

    assertNotNull("Should parse print events on date command", cmd);
    assertTrue("Should be PrintEventsCommand",
        cmd instanceof PrintEventsCommand);
  }

  @Test
  public void testParsePrintEventsInRange() {
    CommandParser parser = new CommandParser();

    String command = "print events from 2025-01-15T10:00 to 2025-01-15T17:00";
    InterfaceCommand cmd = parser.parse(command);

    assertNotNull("Should parse print events in range command", cmd);
    assertTrue("Should be PrintEventsCommand",
        cmd instanceof PrintEventsCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintInvalidFormat() {
    CommandParser parser = new CommandParser();

    String command = "print events at 2025-01-15";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintMissingTo() {
    CommandParser parser = new CommandParser();

    String command = "print events from 2025-01-15T10:00";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintTooFewTokens() {
    CommandParser parser = new CommandParser();

    String command = "print";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParsePrintWrongSecondToken() {
    CommandParser parser = new CommandParser();

    String command = "print something on 2025-01-15";
    parser.parse(command);
  }

}