package calendar.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.commands.CopyEventsCommand;
import calendar.controller.commands.CopySingleEventCommand;
import calendar.controller.commands.InterfaceCommand;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for CopyCommandParser.
 */
public class CopyCommandParserTest {

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
  public void testCopySingleEventIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-16T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(1, model.getCalendar("Source").getEventCount());
    Assert.assertEquals(1, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testCopyEventsOnDateIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Morning from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Afternoon from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-16\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(2, model.getCalendar("Source").getEventCount());
    Assert.assertEquals(2, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("2 event(s) copied to Target"));
  }

  @Test
  public void testCopyEventsBetweenDatesIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Day1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Day2 from 2025-11-16T09:00 to 2025-11-16T10:00\n"
        + "create event Day3 from 2025-11-17T09:00 to 2025-11-17T10:00\n"
        + "copy events between 2025-11-15 and 2025-11-17 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(3, model.getCalendar("Source").getEventCount());
    Assert.assertEquals(3, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("3 event(s) copied to Target"));
  }

  @Test
  public void testCopyQuotedNameIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event \"Team Meeting\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event \"Team Meeting\" on 2025-11-15T10:00 --target Target "
        + "to 2025-11-16T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(1, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testCopyNonexistentEventIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "copy event Nonexistent on 2025-11-15T10:00 --target Target to 2025-11-16T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(0, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
  }

  @Test
  public void testCopyToNonexistentCalendarIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Nonexistent to 2025-11-16T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Calendar not found"));
  }

  @Test
  public void testCopyNoEventsIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-16\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(0, model.getCalendar("Target").getEventCount());
    String output = outputStream.toString();
    assertTrue(output.contains("0 event(s) copied to Target"));
  }

  @Test
  public void testCopySingleTokenIntegrated() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "use calendar --name Source\n"
        + "copy\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR") || output.contains("Invalid"));
  }

  @Test
  public void testParseCopySingleEvent() {
    CommandParser parser = new CommandParser();

    String command = "copy event \"Meeting\" on 2025-01-15T10:00 --target Work to 2025-01-16T14:00";
    InterfaceCommand cmd = parser.parse(command);

    assertNotNull("Should parse copy single event command", cmd);
    assertTrue("Should be CopySingleEventCommand",
        cmd instanceof CopySingleEventCommand);
  }

  @Test
  public void testParseCopyEventsOnDate() {
    CommandParser parser = new CommandParser();

    String command = "copy events on 2025-01-15 --target Work to 2025-01-20";
    InterfaceCommand cmd = parser.parse(command);

    assertNotNull("Should parse copy events on date command", cmd);
    assertTrue("Should be CopyEventsCommand",
        cmd instanceof CopyEventsCommand);
  }

  @Test
  public void testParseCopyEventsBetween() {
    CommandParser parser = new CommandParser();

    String command = "copy events between 2025-01-01 and 2025-01-31 "
        + "--target Personal to 2025-02-01";
    InterfaceCommand cmd = parser.parse(command);

    assertNotNull("Should parse copy events between command", cmd);
    assertTrue("Should be CopyEventsCommand",
        cmd instanceof CopyEventsCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventMissingOn() {
    CommandParser parser = new CommandParser();

    String command = "copy event \"Meeting\" at 2025-01-15T10:00 --target Work to 2025-01-16T14:00";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsMissingTarget() {
    CommandParser parser = new CommandParser();

    String command = "copy events on 2025-01-15 to 2025-01-20";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventMissingTarget() {
    CommandParser parser = new CommandParser();

    String command = "copy event \"Meeting\" on 2025-01-15T10:00 to 2025-01-16T14:00";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventMissingTo() {
    CommandParser parser = new CommandParser();

    String command = "copy event \"Meeting\" on 2025-01-15T10:00 --target Work";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsInvalidFormat() {
    CommandParser parser = new CommandParser();

    String command = "copy events invalid 2025-01-15 --target Work to 2025-01-20";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateMissingTarget() {
    CommandParser parser = new CommandParser();

    String command = "copy events on 2025-01-15 to 2025-01-20";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsOnDateMissingTo() {
    CommandParser parser = new CommandParser();

    String command = "copy events on 2025-01-15 --target Work";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenMissingAnd() {
    CommandParser parser = new CommandParser();

    String command = "copy events between 2025-01-01 to 2025-01-31 --target Personal to 2025-02-01";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenMissingTarget() {
    CommandParser parser = new CommandParser();

    String command = "copy events between 2025-01-01 and 2025-01-31 to 2025-02-01";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyEventsBetweenMissingTo() {
    CommandParser parser = new CommandParser();

    String command = "copy events between 2025-01-01 and 2025-01-31 --target Personal";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyInvalidSubCommand() {
    CommandParser parser = new CommandParser();

    String command = "copy something on 2025-01-15";
    parser.parse(command);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseCopyWithTooFewTokens() {
    CommandParser parser = new CommandParser();

    String command = "copy";
    parser.parse(command);
  }
}