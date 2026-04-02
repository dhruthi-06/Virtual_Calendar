package calendar.controller.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;

/**
 * Integrated tests for BaseCommand class.
 */
public class BaseCommandTest {

  private InterfaceCalendarSystem model;
  private InterfaceCalendarView view;
  private ByteArrayOutputStream outputStream;

  private static class TestCommand extends BaseCommand {
    private boolean executed = false;

    @Override
    public void execute(InterfaceCalendarSystem model, InterfaceCalendarView view,
                        String currentCalendar) {
      validateCalendarInUse(currentCalendar);
      executed = true;
    }

    public boolean wasExecuted() {
      return executed;
    }
  }

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
  public void testValidationPassesWhenCalendarInUse() {
    model.createCalendar("Work", "UTC");
    TestCommand command = new TestCommand();

    command.execute(model, view, "Work");

    assertTrue(command.wasExecuted());
  }

  @Test
  public void testValidationThrowsWhenNoCalendarInUse() {
    TestCommand command = new TestCommand();

    try {
      command.execute(model, view, null);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("No calendar in use"));
      assertFalse(command.wasExecuted());
    }
  }

  @Test
  public void testGetNewCalendarContextReturnsNull() {
    TestCommand command = new TestCommand();

    assertNull(command.getNewCalendarContext());
  }

  @Test
  public void testIsExitCommandReturnsFalse() {
    TestCommand command = new TestCommand();

    assertFalse(command.isExitCommand());
  }

  @Test
  public void testMultipleCommandsCanUseValidation() {
    model.createCalendar("Work", "UTC");
    TestCommand command1 = new TestCommand();
    TestCommand command2 = new TestCommand();

    command1.execute(model, view, "Work");
    command2.execute(model, view, "Work");

    assertTrue(command1.wasExecuted());
    assertTrue(command2.wasExecuted());
  }
}