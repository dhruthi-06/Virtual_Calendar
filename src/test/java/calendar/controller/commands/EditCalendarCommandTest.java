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
 * Integrated tests for EditCalendarCommand.
 */
public class EditCalendarCommandTest {
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
  public void testEditCalendarNameIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "edit calendar --name Work --property name NewWork\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertFalse(model.calendarExists("Work"));
    Assert.assertTrue(model.calendarExists("NewWork"));

    InterfaceCalendar calendar = model.getCalendar("NewWork");
    Assert.assertEquals("NewWork", calendar.getName());
    Assert.assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Work"));
    Assert.assertTrue(output.contains("NewWork"));
    Assert.assertTrue(output.contains("Calendar name changed"));
  }

  @Test
  public void testEditCalendarTimezoneIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "edit calendar --name Work --property timezone America/Chicago\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(ZoneId.of("America/Chicago"), calendar.getTimezone());
    Assert.assertEquals("Work", calendar.getName());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("America/Chicago"));
    Assert.assertTrue(output.contains("Calendar timezone changed"));
  }

  @Test
  public void testEditCaseInsensitiveIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property NAME Work1\n"
        + "edit calendar --name Work1 --property NaMe Work2\n"
        + "edit calendar --name Work2 --property name Work3\n"
        + "edit calendar --name Work3 --property TIMEZONE America/New_York\n"
        + "edit calendar --name Work3 --property TimeZone America/Chicago\n"
        + "edit calendar --name Work3 --property timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work3"));
    Assert.assertEquals(ZoneId.of("UTC"), model.getCalendar("Work3").getTimezone());
  }

  @Test
  public void testEditMultipleTimezonesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone America/New_York\n"
        + "edit calendar --name Work --property timezone America/Chicago\n"
        + "edit calendar --name Work --property timezone America/Los_Angeles\n"
        + "edit calendar --name Work --property timezone Europe/London\n"
        + "edit calendar --name Work --property timezone Asia/Tokyo\n"
        + "edit calendar --name Work --property timezone Australia/Sydney\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(ZoneId.of("Australia/Sydney"), calendar.getTimezone());
  }

  @Test
  public void testEditNameWithEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-16T14:00 to 2025-11-16T15:00\n"
        + "edit calendar --name Work --property name NewWork\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("NewWork");
    Assert.assertEquals(2, calendar.getEventCount());
    Assert.assertEquals("Meeting1", calendar.getAllEvents().get(0).getSubject());
    Assert.assertEquals("Meeting2", calendar.getAllEvents().get(1).getSubject());
  }

  @Test
  public void testEditTimezoneWithEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit calendar --name Work --property timezone America/Los_Angeles\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(1, calendar.getEventCount());
    Assert.assertEquals("Meeting", calendar.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testEditInvalidPropertyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property invalid value\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals("Work", calendar.getName());
    Assert.assertEquals(ZoneId.of("UTC"), calendar.getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit calendar"));
    Assert.assertTrue(output.contains("Invalid property"));
  }

  @Test
  public void testEditCalendarNotFoundIntegrated() {
    String commands = "edit calendar --name NonExistent --property name NewName\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit calendar"));
  }

  @Test
  public void testEditDuplicateNameIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "create calendar --name Personal --timezone UTC\n"
        + "edit calendar --name Work --property name Personal\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit calendar"));
  }

  @Test
  public void testEditInvalidTimezoneIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property timezone InvalidTimezone\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(ZoneId.of("UTC"), calendar.getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit calendar"));
  }

  @Test
  public void testEditSpecialCharactersIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property name \"Work@Home #1\"\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work@Home #1"));
    InterfaceCalendar calendar = model.getCalendar("Work@Home #1");
    Assert.assertEquals("Work@Home #1", calendar.getName());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Work@Home #1"));
  }

  @Test
  public void testEditMultipleTimesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property name Work1\n"
        + "edit calendar --name Work1 --property timezone America/New_York\n"
        + "edit calendar --name Work1 --property name Work2\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work2"));
    InterfaceCalendar calendar = model.getCalendar("Work2");
    Assert.assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "edit calendar --name Work --property name NewWork\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    EditCalendarCommand cmd = new EditCalendarCommand("Work", "name", "NewWork");
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}