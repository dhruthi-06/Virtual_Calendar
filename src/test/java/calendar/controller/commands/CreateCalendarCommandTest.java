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
import java.time.ZoneId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for CreateCalendarCommand.
 * */
public class CreateCalendarCommandTest {
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
  public void testCreateCalendarIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertNotNull(calendar);
    Assert.assertEquals("Work", calendar.getName());
    Assert.assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());
    Assert.assertEquals(0, calendar.getEventCount());
    Assert.assertTrue(calendar.getAllEvents().isEmpty());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Work"));
    Assert.assertTrue(output.contains("America/New_York"));
    Assert.assertTrue(output.contains("Calendar created"));
  }

  @Test
  public void testCreateMultipleCalendarsIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "create calendar --name Personal --timezone America/Los_Angeles\n"
        + "create calendar --name School --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));
    Assert.assertTrue(model.calendarExists("Personal"));
    Assert.assertTrue(model.calendarExists("School"));

    Assert.assertEquals(ZoneId.of("America/New_York"),
        model.getCalendar("Work").getTimezone());
    Assert.assertEquals(ZoneId.of("America/Los_Angeles"),
        model.getCalendar("Personal").getTimezone());
    Assert.assertEquals(ZoneId.of("UTC"), model.getCalendar("School").getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar created: Work"));
    Assert.assertTrue(output.contains("Calendar created: Personal"));
    Assert.assertTrue(output.contains("Calendar created: School"));
  }

  @Test
  public void testCreateDifferentTimezonesIntegrated() {
    String commands = "create calendar --name Cal0 --timezone UTC\n"
        + "create calendar --name Cal1 --timezone America/New_York\n"
        + "create calendar --name Cal2 --timezone America/Chicago\n"
        + "create calendar --name Cal3 --timezone America/Los_Angeles\n"
        + "create calendar --name Cal4 --timezone Europe/London\n"
        + "create calendar --name Cal5 --timezone Asia/Tokyo\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(ZoneId.of("UTC"), model.getCalendar("Cal0").getTimezone());
    Assert.assertEquals(ZoneId.of("America/New_York"),
        model.getCalendar("Cal1").getTimezone());
    Assert.assertEquals(ZoneId.of("America/Chicago"),
        model.getCalendar("Cal2").getTimezone());
    Assert.assertEquals(ZoneId.of("America/Los_Angeles"),
        model.getCalendar("Cal3").getTimezone());
    Assert.assertEquals(ZoneId.of("Europe/London"),
        model.getCalendar("Cal4").getTimezone());
    Assert.assertEquals(ZoneId.of("Asia/Tokyo"), model.getCalendar("Cal5").getTimezone());
  }

  @Test
  public void testCreateSpecialCharactersIntegrated() {
    String commands = "create calendar --name \"Work@Home #1\" --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work@Home #1"));
    InterfaceCalendar cal = model.getCalendar("Work@Home #1");
    Assert.assertEquals("Work@Home #1", cal.getName());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Work@Home #1"));
  }

  @Test
  public void testCreateThenAddEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar cal = model.getCalendar("Work");
    Assert.assertEquals(1, cal.getEventCount());
  }

  @Test
  public void testCreateDuplicateNameIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Work --timezone America/New_York\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar cal = model.getCalendar("Work");
    Assert.assertEquals(ZoneId.of("UTC"), cal.getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to create calendar"));
  }

  @Test
  public void testContextNotChangedIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    CreateCalendarCommand cmd = new CreateCalendarCommand("Work", "UTC");
    Assert.assertNull(cmd.getNewCalendarContext());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    CreateCalendarCommand cmd = new CreateCalendarCommand("Work", "UTC");
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}