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
 * Integrated tests for ExitCommand.
 */
public class ExitCommandTest {
  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testExitCommand() {
    String commands = "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertFalse(output.isEmpty());
  }

  @Test
  public void testExitWithCalendar() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.length() > 0);
  }

  @Test
  public void testExitMultipleTimes() {
    ExitCommand cmd = new ExitCommand();

    cmd.execute(model, view, null);
    String output1 = outputStream.toString();
    Assert.assertTrue(output1.length() > 0);

    outputStream.reset();
    cmd.execute(model, view, null);
    String output2 = outputStream.toString();
    Assert.assertTrue(output2.length() > 0);
  }

  @Test
  public void testExitDoesNotAffectModel() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));
    Assert.assertEquals(1, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testIsExitCommandReturnsTrue() {
    ExitCommand cmd = new ExitCommand();
    Assert.assertTrue(cmd.isExitCommand());
  }

  @Test
  public void testGetNewCalendarContextReturnsNull() {
    ExitCommand cmd = new ExitCommand();
    Assert.assertNull(cmd.getNewCalendarContext());
  }

  @Test
  public void testInterfaceMethods() {
    ExitCommand cmd = new ExitCommand();
    Assert.assertTrue(cmd.isExitCommand());
    Assert.assertNull(cmd.getNewCalendarContext());
  }
}