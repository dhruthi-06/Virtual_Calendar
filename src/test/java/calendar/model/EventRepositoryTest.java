package calendar.model;

import calendar.controller.CalendarController;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for EventRepository.
 */
public class EventRepositoryTest {

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
  public void testAddAndRetrieveSingleEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(1, model.getCalendar("Work").getEventCount());
    Assert.assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        java.time.LocalDateTime.parse("2025-11-15T10:00")));
  }

  @Test
  public void testAddMultipleEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Meeting2 from 2025-11-15T11:00 to 2025-11-15T12:00\n"
        + "create event Meeting3 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(3, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testCheckBusyStatusIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "show status on 2025-11-15T10:30\n"
        + "show status on 2025-11-15T09:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("BUSY"));
    Assert.assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testFindEventBySubjectAndTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with RoomA\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        java.time.LocalDateTime.parse("2025-11-15T10:00")));
    Assert.assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        java.time.LocalDateTime.parse("2025-11-15T14:00")));
  }

  @Test
  public void testRepositoryWithRecurringEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRF for 5 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals(5, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testEmptyRepositoryQueriesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "print events on 2025-11-15\n"
        + "show status on 2025-11-15T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("No events"));
    Assert.assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testRetrieveEventsOnDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Day1Event from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Day2Event from 2025-11-16T10:00 to 2025-11-16T11:00\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Day1Event"));
    int day2Count = output.split("Day2Event", -1).length - 1;
    Assert.assertTrue(day2Count <= 1);
  }

  @Test
  public void testRetrieveEventsInRangeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Event1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Event2 from 2025-11-16T10:00 to 2025-11-16T11:00\n"
        + "create event Event3 from 2025-11-17T10:00 to 2025-11-17T11:00\n"
        + "create event Event4 from 2025-11-20T10:00 to 2025-11-20T11:00\n"
        + "print events from 2025-11-15T00:00 to 2025-11-17T23:59\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event1"));
    Assert.assertTrue(output.contains("Event2"));
    Assert.assertTrue(output.contains("Event3"));
    int event4Count = output.split("Event4", -1).length - 1;
    Assert.assertTrue(event4Count <= 1);
  }

  @Test
  public void testRepositoryWithMultiDayEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Conference from 2025-11-15T09:00 to 2025-11-17T17:00\n"
        + "print events on 2025-11-15\n"
        + "print events on 2025-11-16\n"
        + "print events on 2025-11-17\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    int conferenceCount = output.split("Conference", -1).length - 1;
    Assert.assertTrue(conferenceCount >= 3);
  }

  @Test
  public void testRepositoryAfterEditingIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Original from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Original from 2025-11-15T10:00 with Updated\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Updated"));
    int originalCount = output.split("Original", -1).length - 1;
    Assert.assertTrue(originalCount <= 2);
    Assert.assertNotNull(model.getCalendar("Work").findEvent("Updated",
        java.time.LocalDateTime.parse("2025-11-15T10:00")));
  }
}