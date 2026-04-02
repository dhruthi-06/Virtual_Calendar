package calendar.controller.commands;

import calendar.controller.CalendarController;
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
 * Integrated tests for HelpCommand.
 */
public class HelpCommandTest {
  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

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
  public void testHelpCommandIntegrated() {
    String commands = "help\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertFalse(output.isEmpty());
    Assert.assertTrue(output.length() > 100);
  }

  @Test
  public void testHelpWithCalendarIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "help\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertFalse(output.isEmpty());
  }

  @Test
  public void testHelpMultipleTimesIntegrated() {
    String commands = "help\n"
        + "help\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    int helpCount = output.split("AVAILABLE COMMANDS", -1).length - 1;
    Assert.assertTrue(helpCount >= 2);
  }

  @Test
  public void testHelpDoesNotAffectModelIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "help\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));
    Assert.assertEquals(1, model.getCalendar("Work").getEventCount());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting calendar application"));
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    HelpCommand cmd = new HelpCommand();
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}