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
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ShowCalendarDashboardCommand.
 */
public class ShowCalendarDashboardCommandTest {
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
  public void testDashboardBasicIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Lunch from 2025-11-15T12:00 to 2025-11-15T13:00\n"
        + "show calendar dashboard from 2025-11-15 to 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar Dashboard"));
    Assert.assertTrue(output.contains("Total number of events: 2"));
  }

  @Test
  public void testDashboardBySubjectIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting from 2025-11-16T10:00 to 2025-11-16T11:00\n"
        + "create event Lunch from 2025-11-15T12:00 to 2025-11-15T13:00\n"
        + "show calendar dashboard from 2025-11-15 to 2025-11-16\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by subject"));
    Assert.assertTrue(output.contains("Meeting: 2"));
    Assert.assertTrue(output.contains("Lunch: 1"));
  }

  @Test
  public void testDashboardByWeekdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event MondayEvent from 2025-11-17T10:00 to 2025-11-17T11:00\n"
        + "create event TuesdayEvent from 2025-11-18T10:00 to 2025-11-18T11:00\n"
        + "show calendar dashboard from 2025-11-17 to 2025-11-18\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by weekday"));
    Assert.assertTrue(output.contains("Monday: 1"));
    Assert.assertTrue(output.contains("Tuesday: 1"));
  }

  @Test
  public void testDashboardOnlinePercentageIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event OnlineMeeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event InPersonMeeting from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();


    InterfaceCalendar calendar = model.getCalendar("Work");
    calendar.editEvent("OnlineMeeting", 
        java.time.LocalDateTime.of(2025, 11, 15, 10, 0), 
        "location", "Online");

    ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
    InterfaceCalendarView view2 = new CalendarTextView(new PrintStream(outputStream2));

    String commands2 = "use calendar --name Work\n"
        + "show calendar dashboard from 2025-11-15 to 2025-11-15\n"
        + "exit\n";
    CalendarController controller2 = new CalendarController(
        model, view2, new StringReader(commands2));
    controller2.run();

    String output = outputStream2.toString();
    Assert.assertTrue(output.contains("Online:"));
    Assert.assertTrue(output.contains("Not online:"));
  }

  @Test
  public void testDashboardNoCalendarIntegrated() {
    String commands = "show calendar dashboard from 2025-11-15 to 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testDashboardEmptyRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "show calendar dashboard from 2025-11-15 to 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events: 0"));
  }

  @Test
  public void testDashboardDateRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Event1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Event2 from 2025-11-20T10:00 to 2025-11-20T11:00\n"
        + "show calendar dashboard from 2025-11-15 to 2025-11-25\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("2025-11-15 to 2025-11-25"));
    Assert.assertTrue(output.contains("Total number of events: 2"));
  }

  @Test
  public void testDashboardNullDates() {
    try {
      new ShowCalendarDashboardCommand(null, LocalDate.now());
      Assert.fail("Should throw exception for null start date");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("cannot be null"));
    }

    try {
      new ShowCalendarDashboardCommand(LocalDate.now(), null);
      Assert.fail("Should throw exception for null end date");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testDashboardInvalidDateRange() {
    LocalDate start = LocalDate.of(2025, 11, 20);
    LocalDate end = LocalDate.of(2025, 11, 15);
    try {
      new ShowCalendarDashboardCommand(start, end);
      Assert.fail("Should throw exception for invalid date range");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("after"));
    }
  }

  @Test
  public void testGetStartAndEndDates() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    ShowCalendarDashboardCommand cmd = new ShowCalendarDashboardCommand(start, end);
    Assert.assertEquals(start, cmd.getStartDate());
    Assert.assertEquals(end, cmd.getEndDate());
  }

  @Test
  public void testInterfaceMethods() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    ShowCalendarDashboardCommand cmd = new ShowCalendarDashboardCommand(start, end);
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}

