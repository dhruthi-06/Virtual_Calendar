package calendar.controller.commands;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.model.InterfaceEvent;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for CreateEventCommand.
 */
public class CreateEventCommandTest {
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
  public void testCreateSingleEventIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(1, calendar.getEventCount());

    InterfaceEvent event = calendar.getAllEvents().get(0);
    Assert.assertEquals("Meeting", event.getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 15, 10, 0), event.getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 15, 11, 0), event.getEnd());
    Assert.assertEquals(60, event.getDurationMinutes());
    Assert.assertEquals("", event.getDescription());
    Assert.assertEquals("", event.getLocation());
    Assert.assertTrue(event.isPublic());
    Assert.assertFalse(event.isPartOfSeries());
    Assert.assertNull(event.getSeriesId());
    Assert.assertFalse(event.isAllDay());
    Assert.assertFalse(event.spansMultipleDays());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event created: Meeting"));
  }

  @Test
  public void testCreateAllDayEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Holiday on 2025-12-25\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent event = calendar.getAllEvents().get(0);

    Assert.assertEquals("Holiday", event.getSubject());
    Assert.assertTrue(event.isAllDay());
    Assert.assertEquals(8, event.getStart().getHour());
    Assert.assertEquals(17, event.getEnd().getHour());
    Assert.assertEquals(540, event.getDurationMinutes());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event created: Holiday"));
  }

  @Test
  public void testCreateMultiDayEventIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Conference from 2025-11-15T09:00 to 2025-11-17T17:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent event = calendar.getAllEvents().get(0);

    Assert.assertEquals("Conference", event.getSubject());
    Assert.assertTrue(event.spansMultipleDays());
    Assert.assertEquals(15, event.getStart().getDayOfMonth());
    Assert.assertEquals(17, event.getEnd().getDayOfMonth());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event created: Conference"));
  }

  @Test
  public void testCreateRecurringWithCountIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(6, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    InterfaceEvent firstEvent = events.get(0);
    Assert.assertEquals("Standup", firstEvent.getSubject());
    Assert.assertEquals(30, firstEvent.getDurationMinutes());
    Assert.assertTrue(firstEvent.isPartOfSeries());
    Assert.assertNotNull(firstEvent.getSeriesId());

    String seriesId = firstEvent.getSeriesId();
    for (InterfaceEvent event : events) {
      Assert.assertEquals(seriesId, event.getSeriesId());
      Assert.assertTrue(event.isPartOfSeries());
      Assert.assertEquals("Standup", event.getSubject());
      Assert.assertEquals(30, event.getDurationMinutes());
    }

    Assert.assertEquals("MONDAY", events.get(0).getStart().getDayOfWeek().toString());
    Assert.assertEquals("WEDNESDAY", events.get(1).getStart().getDayOfWeek().toString());
    Assert.assertEquals("FRIDAY", events.get(2).getStart().getDayOfWeek().toString());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Recurring event created: Standup"));
  }

  @Test
  public void testCreateRecurringWithUntilIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-10T10:00 to 2025-11-10T11:00 "
        + "repeats M until 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertTrue(calendar.getEventCount() >= 3);

    List<InterfaceEvent> events = calendar.getAllEvents();
    String seriesId = events.get(0).getSeriesId();
    LocalDateTime until = LocalDateTime.of(2025, 12, 1, 23, 59);

    for (InterfaceEvent event : events) {
      Assert.assertEquals("Weekly", event.getSubject());
      Assert.assertEquals(seriesId, event.getSeriesId());
      Assert.assertTrue(event.isPartOfSeries());
      Assert.assertEquals("MONDAY", event.getStart().getDayOfWeek().toString());
      Assert.assertTrue(event.getStart().isBefore(until) || event.getStart().equals(until));
    }

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Recurring event created: Weekly"));
  }

  @Test
  public void testCreateRecurringSingleDayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Monday from 2025-11-09T14:00 to 2025-11-09T15:00 "
        + "repeats M for 4 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(4, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    for (InterfaceEvent event : events) {
      Assert.assertEquals("MONDAY", event.getStart().getDayOfWeek().toString());
    }
  }

  @Test
  public void testCreateRecurringAllDaysIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-10T08:00 to 2025-11-10T09:00 "
        + "repeats MTWRFSU for 7 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(7, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    String seriesId = events.get(0).getSeriesId();
    for (InterfaceEvent event : events) {
      Assert.assertEquals(seriesId, event.getSeriesId());
    }
  }

  @Test
  public void testCreateEventQueriesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "print events on 2025-11-15\n"
        + "show status on 2025-11-15T10:30\n"
        + "show status on 2025-11-15T09:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent found = calendar.findEvent("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0));
    Assert.assertNotNull(found);

    List<InterfaceEvent> eventsOnDate = calendar.getEventsOnDate(
        LocalDateTime.of(2025, 11, 15, 10, 0).toLocalDate());
    Assert.assertEquals(1, eventsOnDate.size());

    Assert.assertTrue(calendar.isBusyAt(LocalDateTime.of(2025, 11, 15, 10, 30)));
    Assert.assertFalse(calendar.isBusyAt(LocalDateTime.of(2025, 11, 15, 9, 0)));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Meeting"));
    Assert.assertTrue(output.contains("BUSY"));
    Assert.assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testCreateNoCalendarIntegrated() {
    String commands = "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testCreateInvalidTimesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Invalid from 2025-11-15T11:00 to 2025-11-15T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(0, calendar.getEventCount());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to create event"));
  }

  @Test
  public void testCreateMultipleEventsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "create event Meeting3 from 2025-11-16T10:00 to 2025-11-16T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(3, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    Assert.assertEquals("Meeting1", events.get(0).getSubject());
    Assert.assertEquals("Meeting2", events.get(1).getSubject());
    Assert.assertEquals("Meeting3", events.get(2).getSubject());
  }

  @Test
  public void testCreateEdgeCasesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Midnight from 2025-11-15T00:00 to 2025-11-15T01:00\n"
        + "create event LeapDay from 2024-02-29T10:00 to 2024-02-29T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(2, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    Assert.assertEquals(0, events.get(0).getStart().getHour());
    Assert.assertEquals(29, events.get(1).getStart().getDayOfMonth());
    Assert.assertEquals(2, events.get(1).getStart().getMonthValue());
  }

  @Test
  public void testBuilderMethodsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Single from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event WithCount from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MWF for 3 times\n"
        + "create event WithUntil from 2025-11-18T14:00 to 2025-11-18T15:00 "
        + "repeats T until 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertTrue(calendar.getEventCount() >= 4);
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    CreateEventCommand cmd = new CreateEventCommand.Builder("Test",
        LocalDateTime.now(), LocalDateTime.now()).build();
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }

  @Test
  public void testBuilderChaining() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();

    CreateEventCommand.Builder builder = new CreateEventCommand.Builder("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    CreateEventCommand.Builder result = builder.repeats("MWF", 5);
    Assert.assertNotNull(result);
    Assert.assertSame(builder, result);

    CreateEventCommand cmd = result.build();
    Assert.assertNotNull(cmd);
  }

  @Test
  public void testBuilderUntilChaining() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));
    controller.run();

    CreateEventCommand.Builder builder = new CreateEventCommand.Builder("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));

    CreateEventCommand.Builder result = builder.repeatsUntil("M",
        LocalDateTime.of(2025, 12, 1, 0, 0));
    Assert.assertNotNull(result);
    Assert.assertSame(builder, result);

    CreateEventCommand cmd = result.build();
    Assert.assertNotNull(cmd);
  }
}