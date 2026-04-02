package calendar.controller.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for PrintEventsCommand.
 */
public class PrintEventsCommandTest {
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
  public void testPrintOnDateIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:30\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals(2, calendar.getEventCount());

    String output = outputStream.toString();
    assertTrue(output.contains("Events on"));
    assertTrue(output.contains("Meeting1"));
    assertTrue(output.contains("Meeting2"));
    assertTrue(output.contains("from"));
    assertTrue(output.contains("to"));
    assertTrue(output.contains("10:00"));
    assertTrue(output.contains("11:00"));
    assertTrue(output.contains("14:00"));
    assertTrue(output.contains("15:30"));

    assertEquals(2, calendar.getEventCount());
  }

  @Test
  public void testPrintOnDateNoEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals(0, calendar.getEventCount());

    String output = outputStream.toString();
    assertTrue(output.contains("No events scheduled on"));
  }

  @Test
  public void testPrintAllDayEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
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
  public void testPrintWithLocationIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with \"Room 101\"\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Room 101"));
    assertTrue(output.contains("at"));
  }

  @Test
  public void testPrintRecurringEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats M for 4 times\n"
        + "print events on 2025-11-10\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Standup"));
    assertTrue(output.contains("Recurring"));
  }

  @Test
  public void testPrintInRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Event1 from 2025-11-10T09:00 to 2025-11-10T10:00\n"
        + "create event Event2 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "create event Event3 from 2025-11-25T11:00 to 2025-11-25T12:00\n"
        + "print events from 2025-11-01T00:00 to 2025-11-20T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    assertEquals(3, calendar.getEventCount());

    String output = outputStream.toString();
    assertTrue(output.contains("Events between"));
    assertTrue(output.contains("Event1"));
    assertTrue(output.contains("Event2"));

    int event3Count = 0;
    String[] lines = output.split("\n");
    for (String line : lines) {
      if (line.contains("Event3")) {
        event3Count++;
      }
    }
    assertTrue(event3Count <= 1);
  }

  @Test
  public void testPrintInRangeNoEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("No events found between"));
  }

  @Test
  public void testPrintInRangeWithDescriptionIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with \"Important meeting\"\n"
        + "edit event location Meeting from 2025-11-15T10:00 with \"Room 101\"\n"
        + "print events from 2025-11-01T00:00 to 2025-11-30T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("Important meeting"));
    assertTrue(output.contains("Room 101"));
  }

  @Test
  public void testPrintNoCalendarIntegrated() {
    String commands = "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testGetModeIntegrated() {
    PrintEventsCommand cmd1 = new PrintEventsCommand(LocalDate.now());
    assertEquals(PrintEventsCommand.PrintMode.SINGLE_DATE, cmd1.getMode());

    PrintEventsCommand cmd2 = new PrintEventsCommand(LocalDateTime.now(), LocalDateTime.now());
    assertEquals(PrintEventsCommand.PrintMode.DATE_RANGE, cmd2.getMode());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    PrintEventsCommand cmd = new PrintEventsCommand(LocalDate.now());
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }

  @Test
  public void testBuilderPatternOnDate() {
    PrintEventsCommand.Builder builder = new PrintEventsCommand.Builder();

    PrintEventsCommand cmd = builder
        .onDate(LocalDate.of(2025, 11, 15))
        .build();

    assertNotNull(cmd);
    assertEquals(PrintEventsCommand.PrintMode.SINGLE_DATE, cmd.getMode());

    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    cmd.execute(model, view, "Work");

    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));
  }

  @Test
  public void testBuilderPatternInRange() {
    PrintEventsCommand.Builder builder = new PrintEventsCommand.Builder();

    PrintEventsCommand cmd = builder
        .inRange(
            LocalDateTime.of(2025, 11, 15, 0, 0),
            LocalDateTime.of(2025, 11, 17, 23, 59))
        .build();

    assertNotNull(cmd);
    assertEquals(PrintEventsCommand.PrintMode.DATE_RANGE, cmd.getMode());

    model.createCalendar("Work", "UTC");
    model.createEvent("Work", "Event1",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    cmd.execute(model, view, "Work");

    String output = outputStream.toString();
    assertTrue(output.contains("Event1"));
  }

  @Test
  public void testBuilderValidation() {
    PrintEventsCommand.Builder builder1 = new PrintEventsCommand.Builder();

    try {
      builder1.onDate(null);
      fail("Should throw exception for null date");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }

    PrintEventsCommand.Builder builder2 = new PrintEventsCommand.Builder();

    try {
      builder2.inRange(null, LocalDateTime.now());
      fail("Should throw exception for null start");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }

    PrintEventsCommand.Builder builder3 = new PrintEventsCommand.Builder();

    try {
      builder3.inRange(LocalDateTime.now(), null);
      fail("Should throw exception for null end");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }

    PrintEventsCommand.Builder builder4 = new PrintEventsCommand.Builder();

    try {
      builder4.inRange(
          LocalDateTime.of(2025, 11, 20, 10, 0),
          LocalDateTime.of(2025, 11, 15, 10, 0));
      fail("Should throw exception for end before start");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("before"));
    }

    PrintEventsCommand.Builder builder5 = new PrintEventsCommand.Builder();

    try {
      builder5.build();
      fail("Should throw exception for building without mode");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Must call"));
    }
  }

  @Test
  public void testPrintErrorHandling() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    model.deleteCalendar("Work");

    PrintEventsCommand cmd = new PrintEventsCommand(LocalDate.of(2025, 11, 15));
    cmd.execute(model, view, "Work");

    String output = outputStream.toString();
    assertTrue(output.contains("Error printing events"));
  }
}