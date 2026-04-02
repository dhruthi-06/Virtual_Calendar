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
 * Integrated tests for CopySingleEventCommand.
 */
public class CopySingleEventCommandTest {
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
  public void testBasicCopy() {
    String commands = "create calendar --name Source --timezone America/New_York\n"
        + "create calendar --name Target --timezone America/New_York\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-20T14:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 with Important\n"
        + "edit event location Meeting from 2025-11-15T10:00 with RoomA\n"
        + "edit event status Meeting from 2025-11-15T10:00 with private\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-21T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    InterfaceCalendar targetCal = model.getCalendar("Target");

    Assert.assertEquals(1, sourceCal.getEventCount());
    Assert.assertEquals(2, targetCal.getEventCount());

    List<InterfaceEvent> targetEvents = targetCal.getAllEvents();
    InterfaceEvent firstCopy = targetEvents.get(0);
    InterfaceEvent secondCopy = targetEvents.get(1);

    Assert.assertEquals("Meeting", firstCopy.getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 20, 14, 0), firstCopy.getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 20, 15, 0), firstCopy.getEnd());
    Assert.assertEquals(60, firstCopy.getDurationMinutes());
    Assert.assertEquals("", firstCopy.getDescription());
    Assert.assertEquals("", firstCopy.getLocation());
    Assert.assertTrue(firstCopy.isPublic());

    Assert.assertEquals("Meeting", secondCopy.getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 21, 10, 0), secondCopy.getStart());
    Assert.assertEquals("Important", secondCopy.getDescription());
    Assert.assertEquals("RoomA", secondCopy.getLocation());
    Assert.assertFalse(secondCopy.isPublic());

    InterfaceEvent sourceEvent = sourceCal.getAllEvents().get(0);
    Assert.assertEquals(LocalDateTime.of(2025, 11, 15, 10, 0), sourceEvent.getStart());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testRecurringSeries() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "copy event Standup on 2025-11-10T09:00 --target Target to 2025-12-01T10:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    InterfaceCalendar targetCal = model.getCalendar("Target");

    Assert.assertEquals(6, sourceCal.getEventCount());
    Assert.assertEquals(1, targetCal.getEventCount());

    InterfaceEvent sourceEvent = sourceCal.findEvent("Standup",
        LocalDateTime.of(2025, 11, 10, 9, 0));
    String originalSeriesId = sourceEvent.getSeriesId();
    Assert.assertTrue(sourceEvent.isPartOfSeries());

    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);
    Assert.assertEquals("Standup", copiedEvent.getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 12, 1, 10, 0), copiedEvent.getStart());
    Assert.assertEquals(30, copiedEvent.getDurationMinutes());
    Assert.assertNotNull(copiedEvent.getSeriesId());
    Assert.assertNotEquals(originalSeriesId, copiedEvent.getSeriesId());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testQuotedName() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event \"Team Meeting\" from 2025-11-15T09:00 to 2025-11-15T10:00\n"
        + "copy event \"Team Meeting\" on 2025-11-15T09:00 --target Target "
        + "to 2025-11-16T09:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);
    Assert.assertEquals("Team Meeting", copiedEvent.getSubject());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testAllDay() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Holiday on 2025-12-25\n"
        + "copy event Holiday on 2025-12-25T08:00 --target Target to 2025-12-31T08:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent copiedEvent = targetCal.getAllEvents().get(0);
    Assert.assertTrue(copiedEvent.isAllDay());
    Assert.assertEquals(31, copiedEvent.getStart().getDayOfMonth());
    Assert.assertEquals(8, copiedEvent.getStart().getHour());
    Assert.assertEquals(17, copiedEvent.getEnd().getHour());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event copied to Target"));
  }

  @Test
  public void testQueryable() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-20T14:00\n"
        + "use calendar --name Target\n"
        + "print events on 2025-11-20\n"
        + "show status on 2025-11-20T14:30\n"
        + "show status on 2025-11-20T13:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    InterfaceEvent found = targetCal.findEvent("Meeting",
        LocalDateTime.of(2025, 11, 20, 14, 0));
    Assert.assertNotNull(found);

    Assert.assertTrue(targetCal.isBusyAt(LocalDateTime.of(2025, 11, 20, 14, 30)));
    Assert.assertFalse(targetCal.isBusyAt(LocalDateTime.of(2025, 11, 20, 13, 30)));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Meeting"));
    Assert.assertTrue(output.contains("BUSY"));
    Assert.assertTrue(output.contains("AVAILABLE"));
  }

  @Test
  public void testNoCalendar() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-20T14:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testNotFound() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "copy event NonExistent on 2025-11-15T10:00 --target Target "
        + "to 2025-11-20T14:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to copy event"));
  }

  @Test
  public void testSameCalendar() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Source to 2025-11-20T14:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar sourceCal = model.getCalendar("Source");
    Assert.assertEquals(2, sourceCal.getEventCount());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event copied to Source"));
  }

  @Test
  public void testBuilderChaining() {
    CopySingleEventCommand.Builder builder = new CopySingleEventCommand.Builder("Meeting");

    CopySingleEventCommand.Builder returned1 = builder.sourceDateTime(
        LocalDateTime.of(2025, 11, 15, 10, 0));
    Assert.assertNotNull(returned1);
    Assert.assertSame(builder, returned1);

    CopySingleEventCommand.Builder returned2 = builder.targetCalendar("Target");
    Assert.assertNotNull(returned2);
    Assert.assertSame(builder, returned2);

    CopySingleEventCommand.Builder returned3 = builder.targetDateTime(
        LocalDateTime.of(2025, 11, 20, 14, 0));
    Assert.assertNotNull(returned3);
    Assert.assertSame(builder, returned3);

    CopySingleEventCommand cmd = builder.build();
    Assert.assertNotNull(cmd);
  }

  @Test
  public void testBuilderExecution() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Test1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Test1 on 2025-11-15T10:00 --target Target to 2025-11-16T10:00\n"
        + "create event Test2 from 2025-11-17T10:00 to 2025-11-17T11:00\n"
        + "copy event Test2 on 2025-11-17T10:00 --target Target to 2025-11-18T14:00\n"
        + "create event Test3 from 2025-11-19T10:00 to 2025-11-19T11:00\n"
        + "copy event Test3 on 2025-11-19T10:00 --target Target to 2025-11-20T16:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar targetCal = model.getCalendar("Target");
    Assert.assertEquals(3, targetCal.getEventCount());

    List<InterfaceEvent> events = targetCal.getAllEvents();
    Assert.assertEquals(LocalDateTime.of(2025, 11, 16, 10, 0), events.get(0).getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 18, 14, 0), events.get(1).getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 20, 16, 0), events.get(2).getStart());
  }

  @Test
  public void testInterfaceMethods() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "copy event Meeting on 2025-11-15T10:00 --target Target to 2025-11-20T14:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    CopySingleEventCommand cmd = new CopySingleEventCommand(
        "Meeting", LocalDateTime.now(), "Target", LocalDateTime.now());
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}