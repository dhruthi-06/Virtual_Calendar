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
 * Integrated tests for EditEventCommand.
 */
public class EditEventCommandTest {
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
  public void testEditSingleEventAllPropertiesIntegrated() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Meeting from 2025-11-15T10:00 with UpdatedMeeting\n"
        + "edit event location UpdatedMeeting from 2025-11-15T10:00 with RoomA\n"
        + "edit event description UpdatedMeeting from 2025-11-15T10:00 with Important\n"
        + "edit event status UpdatedMeeting from 2025-11-15T10:00 with private\n"
        + "edit event start UpdatedMeeting from 2025-11-15T10:00 with 2025-11-15T09:00\n"
        + "edit event end UpdatedMeeting from 2025-11-15T09:00 with 2025-11-15T12:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent event = calendar.findEvent("UpdatedMeeting",
        LocalDateTime.of(2025, 11, 15, 9, 0));

    Assert.assertNotNull(event);
    Assert.assertEquals("UpdatedMeeting", event.getSubject());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 15, 9, 0), event.getStart());
    Assert.assertEquals(LocalDateTime.of(2025, 11, 15, 12, 0), event.getEnd());
    Assert.assertEquals(180, event.getDurationMinutes());
    Assert.assertEquals("Important", event.getDescription());
    Assert.assertEquals("RoomA", event.getLocation());
    Assert.assertFalse(event.isPublic());

    Assert.assertNull(calendar.findEvent("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0)));

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditFromDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "edit events subject Standup from 2025-11-12T09:00 with NewStandup\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(6, calendar.getEventCount());

    List<InterfaceEvent> events = calendar.getAllEvents();
    int oldCount = 0;
    int newCount = 0;
    for (InterfaceEvent event : events) {
      if (event.getSubject().equals("Standup")) {
        oldCount++;
      }
      if (event.getSubject().equals("NewStandup")) {
        newCount++;
      }
    }

    Assert.assertTrue(oldCount > 0);
    Assert.assertTrue(newCount > 0);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditEntireSeriesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-10T09:00 to 2025-11-10T09:30 "
        + "repeats MWF for 6 times\n"
        + "edit series location Standup from 2025-11-10T09:00 with Room201\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    List<InterfaceEvent> events = calendar.getAllEvents();

    String originalSeriesId = events.get(0).getSeriesId();
    for (InterfaceEvent event : events) {
      Assert.assertEquals("Room201", event.getLocation());
      Assert.assertEquals("Standup", event.getSubject());
      Assert.assertEquals(originalSeriesId, event.getSeriesId());
    }

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Entire event series updated"));
  }

  @Test
  public void testEditInvalidPropertyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event invalid Meeting from 2025-11-15T10:00 with value\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent event = calendar.findEvent("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0));
    Assert.assertNotNull(event);
    Assert.assertEquals("Meeting", event.getSubject());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit event"));
    Assert.assertTrue(output.contains("Invalid property"));
  }

  @Test
  public void testEditEventNotFoundIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "edit event subject NonExistent from 2025-11-15T10:00 with New\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Failed to edit event"));
  }

  @Test
  public void testEditNoCalendarIntegrated() {
    String commands = "edit event subject Meeting from 2025-11-15T10:00 with New\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("ERROR"));
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testBuilderValidationIntegrated() {
    try {
      new EditEventCommand.Builder(null, "subject", "Meeting");
      Assert.fail("Should throw exception for null mode");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Edit mode cannot be null"));
    }

    try {
      new EditEventCommand.Builder(EditEventCommand.EditMode.SINGLE, null, "Meeting");
      Assert.fail("Should throw exception for null property");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Property cannot be null"));
    }

    try {
      new EditEventCommand.Builder(EditEventCommand.EditMode.SINGLE, "", "Meeting");
      Assert.fail("Should throw exception for empty property");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Property cannot be null or empty"));
    }

    try {
      new EditEventCommand.Builder(EditEventCommand.EditMode.SINGLE, "subject", null);
      Assert.fail("Should throw exception for null subject");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Subject cannot be null"));
    }

    try {
      new EditEventCommand.Builder(EditEventCommand.EditMode.SINGLE, "subject", "   ");
      Assert.fail("Should throw exception for empty subject");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Subject cannot be null or empty"));
    }

    try {
      EditEventCommand.Builder builder = new EditEventCommand.Builder(
          EditEventCommand.EditMode.SINGLE, "subject", "Meeting");
      builder.build();
      Assert.fail("Should throw exception when startDateTime not set");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("Start date/time must be set"));
    }

    try {
      EditEventCommand.Builder builder = new EditEventCommand.Builder(
          EditEventCommand.EditMode.SINGLE, "subject", "Meeting");
      builder.startDateTime(LocalDateTime.now());
      builder.build();
      Assert.fail("Should throw exception when newValue not set");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("New value must be set"));
    }
  }

  @Test
  public void testBuilderMethodsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test1 from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Test1 from 2025-11-15T10:00 with Test2\n"
        + "edit event location Test2 from 2025-11-15T10:00 with RoomA\n"
        + "edit event description Test2 from 2025-11-15T10:00 with Desc\n"
        + "exit\n";
    CalendarController controller = new CalendarController(
        model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    InterfaceEvent event = calendar.findEvent("Test2",
        LocalDateTime.of(2025, 11, 15, 10, 0));
    Assert.assertNotNull(event);
    Assert.assertEquals("RoomA", event.getLocation());
    Assert.assertEquals("Desc", event.getDescription());
  }

  @Test
  public void testGettersIntegrated() {
    EditEventCommand cmd = new EditEventCommand.Builder(
        EditEventCommand.EditMode.SINGLE, "subject", "Meeting")
        .startDateTime(LocalDateTime.now())
        .newValue("New")
        .build();

    Assert.assertEquals(EditEventCommand.EditMode.SINGLE, cmd.getMode());
    Assert.assertEquals("subject", cmd.getProperty());
    Assert.assertEquals("New", cmd.getNewValue());
  }

  @Test
  public void testInterfaceMethodsIntegrated() {
    EditEventCommand cmd = new EditEventCommand.Builder(
        EditEventCommand.EditMode.SINGLE, "subject", "Meeting")
        .startDateTime(LocalDateTime.now())
        .newValue("New")
        .build();
    Assert.assertNull(cmd.getNewCalendarContext());
    Assert.assertFalse(cmd.isExitCommand());
  }
}