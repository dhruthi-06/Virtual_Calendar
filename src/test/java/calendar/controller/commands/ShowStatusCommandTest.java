package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for ShowStatusCommand.
 */
public class ShowStatusCommandTest {
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
  public void testShowStatusBusyIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime checkTime = LocalDateTime.of(2025, 11, 15, 10, 30);
    Assert.assertTrue(calendar.isBusyAt(checkTime));

    String output = outputStream.toString();
    Assert.assertTrue(output.toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusAvailableIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T09:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime checkTime = LocalDateTime.of(2025, 11, 15, 9, 0);
    Assert.assertFalse(calendar.isBusyAt(checkTime));

    String output = outputStream.toString();
    Assert.assertTrue(output.toLowerCase().contains("available"));
  }

  @Test
  public void testShowStatusBoundariesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:00\n"
        + "show status on 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime start = LocalDateTime.of(2025, 11, 15, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 11, 15, 11, 0);

    Assert.assertTrue(calendar.isBusyAt(start));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("BUSY"));
    Assert.assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testShowStatusAllDayEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "show status on 2025-12-25T12:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime checkTime = LocalDateTime.of(2025, 12, 25, 12, 0);
    Assert.assertTrue(calendar.isBusyAt(checkTime));

    String output = outputStream.toString();
    Assert.assertTrue(output.toLowerCase().contains("busy"));
  }

  @Test
  public void testShowStatusMidnightIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "show status on 2025-11-15T00:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    LocalDateTime midnight = LocalDateTime.of(2025, 11, 15, 0, 0);
    Assert.assertFalse(calendar.isBusyAt(midnight));

    String output = outputStream.toString();
    Assert.assertFalse(output.isEmpty());
  }

  @Test
  public void testShowStatusMultipleEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Meeting2 from 2025-11-15T11:00 to 2025-11-15T12:00\n"
        + "create event Meeting3 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "show status on 2025-11-15T10:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(3, calendar.getEventCount());

    LocalDateTime checkTime = LocalDateTime.of(2025, 11, 15, 10, 30);
    Assert.assertFalse(calendar.isBusyAt(checkTime));

    String output = outputStream.toString();
    Assert.assertTrue(output.toLowerCase().contains("available"));
  }

  @Test
  public void testShowStatusNoCalendarIntegrated() {
    String commands = "show status on 2025-11-15T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testShowStatusNullDateTime() {
    try {
      new ShowStatusCommand(null);
      Assert.fail("Should throw exception for null");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testGetDateTimeIntegrated() {
    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 10, 0);
    ShowStatusCommand cmd = new ShowStatusCommand(dateTime);
    Assert.assertEquals(dateTime, cmd.getDateTime());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    ShowStatusCommand cmd = new ShowStatusCommand(LocalDateTime.now());
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}