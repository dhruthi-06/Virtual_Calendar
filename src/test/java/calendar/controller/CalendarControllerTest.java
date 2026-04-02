package calendar.controller;

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
 * Integrated tests for CalendarController.
 */
public class CalendarControllerTest {
  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

  /**
   * This is Setup method.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testConstructorValidationIntegrated() {
    try {
      new CalendarController(null, view, new StringReader(""));
      Assert.fail("Should throw for null model");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Model cannot be null"));
    }

    try {
      new CalendarController(model, null, new StringReader(""));
      Assert.fail("Should throw for null view");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("View cannot be null"));
    }

    try {
      new CalendarController(model, view, null);
      Assert.fail("Should throw for null input");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("Input cannot be null"));
    }

    CalendarController controller =
        new CalendarController(model, view, new StringReader("exit\n"));
    Assert.assertNotNull(controller);
    Assert.assertNull(controller.getCurrentCalendarName());
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testExitCommandIntegrated() {
    String commands = "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting calendar application"));
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testExitCaseInsensitiveIntegrated() {
    String commands = "EXIT\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting"));
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testExitMixedCaseIntegrated() {
    String commands = "ExIt\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testEmptyLinesIgnoredIntegrated() {
    String commands = "\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting"));
    Assert.assertFalse(output.contains("Invalid command"));
  }

  @Test
  public void testMultipleEmptyLinesIntegrated() {
    String commands = "\n\n\n  \nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testWhitespaceOnlyIntegrated() {
    String commands = "   \nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testNoExitCommandIntegrated() {
    String commands = "help\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("File ended without 'exit' command"));
  }

  @Test
  public void testMultipleCommandsNoExitIntegrated() {
    String commands =
        "create calendar --name Test --timezone UTC\n"
            + "use calendar --name Test\n"
            + "help\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("File ended without 'exit' command"));
  }

  @Test
  public void testCreateCalendarIntegrated() {
    String commands =
        "create calendar --name Work --timezone America/New_York\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));
    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals("Work", calendar.getName());
    Assert.assertEquals(ZoneId.of("America/New_York"), calendar.getTimezone());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar created"));
    Assert.assertTrue(output.contains("Work"));
  }

  @Test
  public void testUseCalendarIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals("Work", controller.getCurrentCalendarName());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Now using calendar: Work"));
  }

  @Test
  public void testHelpCommandIntegrated() {
    String commands = "help\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertFalse(output.isEmpty());
    Assert.assertTrue(output.length() > 100);
  }

  @Test
  public void testCreateEventIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(1, calendar.getEventCount());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Event created"));
  }

  @Test
  public void testInvalidCommandIntegrated() {
    String commands = "invalid\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid command"));
  }

  @Test
  public void testInvalidCreateIntegrated() {
    String commands = "create\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid command"));
  }

  @Test
  public void testInvalidEditIntegrated() {
    String commands = "edit\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid command"));
  }

  @Test
  public void testIllegalArgumentExceptionIntegrated() {
    String commands = "create calendar --name Work\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid command"));
  }

  @Test
  public void testIllegalStateExceptionIntegrated() {
    String commands = "print events on 2024-01-15\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("No calendar in use"));
  }

  @Test
  public void testContextPersistsIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "help\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals("Work", controller.getCurrentCalendarName());
  }

  @Test
  public void testContextChangesIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "create calendar --name Personal --timezone UTC\n"
            + "use calendar --name Work\n"
            + "use calendar --name Personal\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals("Personal", controller.getCurrentCalendarName());
  }

  @Test
  public void testGetCurrentCalendarInitiallyNullIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("exit\n"));
    Assert.assertNull(controller.getCurrentCalendarName());
  }

  @Test
  public void testGetCurrentCalendarAfterUseIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals("Work", controller.getCurrentCalendarName());
  }

  @Test
  public void testSetCurrentCalendarValidIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    controller.setCurrentCalendarName("Work");
    Assert.assertEquals("Work", controller.getCurrentCalendarName());
  }

  @Test
  public void testSetCurrentCalendarNullIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("exit\n"));
    controller.setCurrentCalendarName(null);
    Assert.assertNull(controller.getCurrentCalendarName());
  }

  @Test
  public void testSetCurrentCalendarNonExistentIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("exit\n"));
    try {
      controller.setCurrentCalendarName("NonExistent");
      Assert.fail("Should throw exception");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void testIsRunningInitiallyFalseIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("exit\n"));
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testIsRunningAfterStopIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("help\n"));
    controller.stop();
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testStopIntegrated() {
    CalendarController controller =
        new CalendarController(model, view, new StringReader("help\n"));
    controller.stop();
    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testStopBeforeRunIntegrated() {
    String commands = "help\nhelp\nhelp\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));
    controller.stop();

    controller.run();

    Assert.assertFalse(controller.isRunning());
  }

  @Test
  public void testCompleteWorkflowIntegrated() {
    String commands =
        "create calendar --name Work --timezone America/New_York\n"
            + "use calendar --name Work\n"
            + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertTrue(model.calendarExists("Work"));
    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(1, calendar.getEventCount());
    Assert.assertEquals("Work", controller.getCurrentCalendarName());

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Calendar created"));
    Assert.assertTrue(output.contains("Now using calendar"));
    Assert.assertTrue(output.contains("Event created"));
  }

  @Test
  public void testMultipleSwitchesIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "create calendar --name Personal --timezone UTC\n"
            + "create calendar --name School --timezone UTC\n"
            + "use calendar --name Work\n"
            + "use calendar --name Personal\n"
            + "use calendar --name School\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    Assert.assertEquals("School", controller.getCurrentCalendarName());
  }

  @Test
  public void testErrorRecoveryIntegrated() {
    String commands = "invalid command\nhelp\nexit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Invalid command"));
    Assert.assertTrue(output.length() > 200);
    Assert.assertTrue(output.contains("Exiting"));
  }

  @Test
  public void testMultipleEventsWorkflowIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "create event Meeting1 from 2025-11-15T09:00 to 2025-11-15T10:00\n"
            + "create event Meeting2 from 2025-11-15T14:00 to 2025-11-15T15:00\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(2, calendar.getEventCount());
  }

  @Test
  public void testPrintEventsWorkflowIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
            + "print events on 2025-11-15\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    controller.run();

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Meeting"));
  }

  @Test
  public void testIsRunningDuringExecutionIntegrated() {
    String commands =
        "create calendar --name Work --timezone UTC\n"
            + "use calendar --name Work\n"
            + "create event Test from 2025-11-15T10:00 to 2025-11-15T11:00\n"
            + "exit\n";
    CalendarController controller =
        new CalendarController(model, view, new StringReader(commands));

    Assert.assertFalse(controller.isRunning());

    controller.run();

    Assert.assertFalse(controller.isRunning());

    InterfaceCalendar calendar = model.getCalendar("Work");
    Assert.assertEquals(1, calendar.getEventCount());
  }
}
