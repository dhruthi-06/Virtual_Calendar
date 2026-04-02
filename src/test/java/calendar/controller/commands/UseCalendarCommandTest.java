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
 * Integrated tests for UseCalendarCommand.
 */
public class UseCalendarCommandTest {
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
  public void testUseCalendarIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Now using calendar"));
    Assert.assertTrue(output.contains("Work"));
  }

  @Test
  public void testUseSwitchingIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Personal --timezone UTC\n"
        + "use calendar --name Work\n"
        + "use calendar --name Personal\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Now using calendar: Work"));
    Assert.assertTrue(output.contains("Now using calendar: Personal"));
  }

  @Test
  public void testUseMultipleSwitchesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Personal --timezone UTC\n"
        + "use calendar --name Work\n"
        + "use calendar --name Personal\n"
        + "use calendar --name Work\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    UseCalendarCommand cmd1 = new UseCalendarCommand("Work");
    Assert.assertEquals("Work", cmd1.getNewCalendarContext());

    UseCalendarCommand cmd2 = new UseCalendarCommand("Personal");
    Assert.assertEquals("Personal", cmd2.getNewCalendarContext());
  }

  @Test
  public void testUseCalendarNotFoundIntegrated() {
    String commands = "use calendar --name NonExistent\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to use calendar"));
    Assert.assertTrue(output.contains("does not exist"));
  }

  @Test
  public void testUseCalendarWithEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(1, model.getCalendar("Work").getEventCount());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Now using calendar: Work"));
  }

  @Test
  public void testUseSpecialCharactersIntegrated() {
    String commands = "create calendar --name \"Work@Home\" --timezone UTC\n"
        + "use calendar --name \"Work@Home\"\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Work@Home"));
  }

  @Test
  public void testGetCalendarNameIntegrated() {
    UseCalendarCommand cmd = new UseCalendarCommand("Work");
    Assert.assertEquals("Work", cmd.getCalendarName());
  }

  @Test
  public void testGetNewCalendarContextIntegrated() {
    UseCalendarCommand cmd = new UseCalendarCommand("Work");
    Assert.assertEquals("Work", cmd.getNewCalendarContext());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    UseCalendarCommand cmd = new UseCalendarCommand("Work");
    Assert.assertEquals("Work", cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}