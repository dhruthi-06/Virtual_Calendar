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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for CopyEventsCommand.
 */
public class CopyEventsCommandTest {
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
  public void testSingleDate() {
    String commands = "create calendar --name Source --timezone America/New_York\n"
        + "create calendar --name Target --timezone America/New_York\n"
        + "use calendar --name Source\n"
        + "create event Meeting1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:30\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    InterfaceCalendar targetCal = model.getCalendar("Target");

    Assert.assertEquals(2, sourceCal.getEventCount());
    Assert.assertEquals(2, targetCal.getEventCount());

    List<InterfaceEvent> events = targetCal.getAllEvents();
    Assert.assertEquals("Meeting1", events.get(0).getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 20, 10, 0), events.get(0).getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 20, 11, 0), events.get(0).getEnd());
    Assert.assertEquals(60, events.get(0).getDurationMinutes());

    Assert.assertEquals("Meeting2", events.get(1).getSubject());
    Assert.assertEquals(90, events.get(1).getDurationMinutes());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("2 event(s) copied to Target"));
  }

  @Test
  public void testDateRange() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Event1 from 2025-11-10T09:00 to 2025-11-10T10:00\n"
        + "create event Event2 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "create event Event3 from 2025-11-25T11:00 to 2025-11-25T12:00\n"
        + "copy events between 2025-11-01 and 2025-11-20 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(2, targetCal.getEventCount());

    List<InterfaceEvent> events = targetCal.getAllEvents();
    Assert.assertEquals(12, events.get(0).getStart().getMonthValue());
    Assert.assertEquals(10, events.get(0).getStart().getDayOfMonth());
    Assert.assertEquals(12, events.get(1).getStart().getMonthValue());
    Assert.assertEquals(15, events.get(1).getStart().getDayOfMonth());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("2 event(s) copied to Target"));
  }

  @Test
  public void testRecurringSeries() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 repeats MWF for 6 times\n"
        + "copy events on 2025-11-10 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    InterfaceCalendar targetCal = model.getCalendar("Target");

    Assert.assertEquals(6, sourceCal.getEventCount());

    List<InterfaceEvent> sourceEventsOnDate = sourceCal.getEventsOnDate(LocalDate.of(2025, 11, 10));
    List<InterfaceEvent> targetEventsOnDate = targetCal.getEventsOnDate(LocalDate.of(2025, 12, 1));

    Assert.assertEquals(sourceEventsOnDate.size(), targetEventsOnDate.size());

    String originalSeriesId = sourceEventsOnDate.get(0).getSeriesId();
    String newSeriesId = targetEventsOnDate.get(0).getSeriesId();

    Assert.assertNotNull(newSeriesId);
    Assert.assertNotEquals(originalSeriesId, newSeriesId);

    for (InterfaceEvent event : targetEventsOnDate) {
      Assert.assertTrue(event.isPartOfSeries());
      Assert.assertEquals(newSeriesId, event.getSeriesId());
    }

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("copied to Target"));
  }

  @Test
  public void testAllProperties() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T15:00 to 2025-11-15T16:30\n"
        + "edit event description Meeting from 2025-11-15T15:00 with \"Q4 strategy\"\n"
        + "edit event location Meeting from 2025-11-15T15:00 with \"Room A\"\n"
        + "edit event status Meeting from 2025-11-15T15:00 with private\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);

    Assert.assertEquals("Meeting", copiedEvent.getSubject());
    Assert.assertEquals("Q4 strategy", copiedEvent.getDescription());
    Assert.assertEquals("Room A", copiedEvent.getLocation());
    Assert.assertFalse(copiedEvent.isPublic());
    Assert.assertEquals(90, copiedEvent.getDurationMinutes());
    Assert.assertEquals(20, copiedEvent.getStart().getDayOfMonth());
  }

  @Test
  public void testAllDay() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Holiday on 2025-12-25\n"
        + "copy events on 2025-12-25 --target Target to 2025-12-31\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);

    Assert.assertEquals("Holiday", copiedEvent.getSubject());
    Assert.assertTrue(copiedEvent.isAllDay());
    Assert.assertEquals(31, copiedEvent.getStart().getDayOfMonth());
    Assert.assertEquals(8, copiedEvent.getStart().getHour());
    Assert.assertEquals(17, copiedEvent.getEnd().getHour());
  }

  @Test
  public void testMultipleDays() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Day1 from 2025-11-05T10:00 to 2025-11-05T11:00\n"
        + "create event Day2 from 2025-11-10T14:00 to 2025-11-10T15:00\n"
        + "create event Day3 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "copy events between 2025-11-01 and 2025-11-30 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(3, targetCal.getEventCount());

    List<InterfaceEvent> events = targetCal.getAllEvents();
    Assert.assertEquals("Day1", events.get(0).getSubject());
    Assert.assertEquals(5, events.get(0).getStart().getDayOfMonth());
    Assert.assertEquals("Day2", events.get(1).getSubject());
    Assert.assertEquals(10, events.get(1).getStart().getDayOfMonth());
    Assert.assertEquals("Day3", events.get(2).getSubject());
    Assert.assertEquals(15, events.get(2).getStart().getDayOfMonth());
  }

  @Test
  public void testQueryable() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "use calendar --name Target\n"
        + "print events on 2025-11-20\n"
        + "show status on 2025-11-20T10:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent found = targetCal.findEvent("Meeting", LocalDateTime.of(2025, 11, 20, 10, 0));
    Assert.assertNotNull(found);

    List<InterfaceEvent> eventsOnDate = targetCal.getEventsOnDate(LocalDate.of(2025, 11, 20));
    Assert.assertEquals(1, eventsOnDate.size());

    Assert.assertTrue(targetCal.isBusyAt(LocalDateTime.of(2025, 11, 20, 10, 30)));
    Assert.assertFalse(targetCal.isBusyAt(LocalDateTime.of(2025, 11, 20, 9, 30)));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Meeting"));
    Assert.assertTrue(output.contains("BUSY"));
  }

  @Test
  public void testNoCalendar() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testZeroEvents() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(0, targetCal.getEventCount());
    Assert.assertTrue(targetCal.getAllEvents().isEmpty());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("0 event(s) copied to Target"));
  }

  @Test
  public void testErrorHandling() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy events on 2025-11-15 --target NonExistent to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to copy events"));
  }

  @Test
  public void testSameCalendar() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy events on 2025-11-15 --target Source to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    Assert.assertEquals(2, sourceCal.getEventCount());

    InterfaceEvent original = sourceCal.findEvent("Meeting", LocalDateTime.of(2025, 11, 15, 10, 0));
    InterfaceEvent copied = sourceCal.findEvent("Meeting", LocalDateTime.of(2025, 11, 20, 10, 0));

    Assert.assertNotNull(original);
    Assert.assertNotNull(copied);
    Assert.assertNotSame(original, copied);
  }

  @Test
  public void testConflict() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "use calendar --name Target\n"
        + "create event Meeting from 2025-11-20T10:00 to 2025-11-20T11:00\n"
        + "use calendar --name Source\n"
        + "copy events on 2025-11-15 --target Target to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(1, targetCal.getEventCount());

    InterfaceEvent existingEvent = targetCal.getAllEvents().get(0);
    Assert.assertEquals("Meeting", existingEvent.getSubject());
  }

  @Test
  public void testInclusiveRange() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event LastDay from 2025-11-20T10:00 to 2025-11-20T11:00\n"
        + "copy events between 2025-11-01 and 2025-11-20 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(1, targetCal.getEventCount());

    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);
    Assert.assertEquals("LastDay", copiedEvent.getSubject());
  }

  @Test
  public void testPartialSeries() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 repeats MWF for 6 times\n"
        + "copy events between 2025-11-12 and 2025-11-17 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(3, targetCal.getEventCount());

    List<InterfaceEvent> targetEvents = targetCal.getAllEvents();
    String newSeriesId = targetEvents.get(0).getSeriesId();

    for (InterfaceEvent event : targetEvents) {
      Assert.assertTrue(event.isPartOfSeries());
      Assert.assertEquals(newSeriesId, event.getSeriesId());
    }
  }

  @Test
  public void testTimezones() {
    String commands = "create calendar --name EST --timezone America/New_York\n"
        + "create calendar --name PST --timezone America/Los_Angeles\n"
        + "use calendar --name EST\n"
        + "create event Meeting from 2025-11-15T14:00 to 2025-11-15T15:00\n"
        + "copy events on 2025-11-15 --target PST to 2025-11-20\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar pstCal = model.getCalendar("PST");
    InterfaceEvent copiedEvent = pstCal.getAllEvents().get(0);

    Assert.assertEquals("Meeting", copiedEvent.getSubject());
    Assert.assertEquals(11, copiedEvent.getStart().getHour());
    Assert.assertEquals(12, copiedEvent.getEnd().getHour());
    Assert.assertEquals(60, copiedEvent.getDurationMinutes());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("1 event(s) copied to PST"));
  }

  @Test
  public void testBuilderChaining() {
    CopyEventsCommand.Builder builder = new CopyEventsCommand.Builder(
        LocalDate.of(2025, 11, 15), "Target", LocalDate.of(2025, 11, 20));

    CopyEventsCommand.Builder returned = builder.sourceEndDate(LocalDate.of(2025, 11, 18));
    Assert.assertNotNull(returned);
    Assert.assertSame(builder, returned);

    CopyEventsCommand cmd = builder.build();
    Assert.assertNotNull(cmd);
  }

  @Test
  public void testBuilderExecution() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Event1 from 2025-11-10T10:00 to 2025-11-10T11:00\n"
        + "create event Event2 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy events between 2025-11-10 and 2025-11-15 --target Target to 2025-12-01\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(2, targetCal.getEventCount());
  }

  @Test
  public void testInterfaceMethods() {
    CopyEventsCommand cmd = new CopyEventsCommand(
        LocalDate.of(2025, 11, 15), null, "Target", LocalDate.of(2025, 11, 20));

    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}