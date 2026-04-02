package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ParserUtils utility methods.
 * Tests command parsing with various formats and edge cases.
 */
public class ParserUtilsTest {

  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testQuotedSubjects() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Team Standup Meeting\" from 2025-11-15T09:00 to 2025-11-15T09:30\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertNotNull(model.getCalendar("Work").findEvent("Team Standup Meeting",
        java.time.LocalDateTime.parse("2025-11-15T09:00")));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        java.time.LocalDateTime.parse("2025-11-15T10:00")));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name \"My Work Calendar\" --timezone UTC\n"
        + "use calendar --name \"My Work Calendar\"\n"
        + "create event \"Team Meeting\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("My Work Calendar"));
    assertEquals(1, model.getCalendar("My Work Calendar").getEventCount());
  }

  @Test
  public void testRecurringPatterns() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Standup from 2025-11-17T09:00 to 2025-11-17T09:30 repeats MWF for 3 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(3, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Daily from 2025-11-17T09:00 "
        + "to 2025-11-17T09:30 repeats MTWRFSU for 7 times\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(7, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-17T10:00 to "
        + "2025-11-17T11:00 repeats M until 2025-12-15\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.getCalendar("Work").getEventCount() > 0);

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-15T10:00 "
        + "to 2025-11-15T11:00 repeats invalid\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testCopyOperations() {
    String commands = "create calendar --name Source --timezone UTC\n"
        + "create calendar --name Target --timezone UTC\n"
        + "use calendar --name Source\n"
        + "create event \"Important Meeting\" from 2025-11-15T10:00 "
        + "to 2025-11-15T11:00\n"
        + "copy event \"Important Meeting\" on "
        + "2025-11-15T10:00 --target Target to 2025-11-16T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Target").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 "
        + "to 2025-11-15T11:00\n"
        + "edit event location Meeting from "
        + "2025-11-15T10:00 with \"Conference Room B\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));
  }

  @Test
  public void testQuoteHandling() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Test from 2025-11-15T10:00 with \"\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("updated"));

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"A\" from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event NoQuotes from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertEquals(1, model.getCalendar("Work").getEventCount());

    outputStream.reset();
    model = new CalendarSystem();
    commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event \"Unclosed Meeting from "
        + "2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(outputStream.toString().contains("ERROR"));
  }

  @Test
  public void testSpacingAndFormatting() {
    String commands = "create   calendar   --name   Work   --timezone   UTC\n"
        + "use   calendar   --name   Work\n"
        + "create   event   Meeting   from   2025-11-15T10:00"
        + "   to   2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.calendarExists("Work"));
    assertEquals(1, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testComplexWorkflow() {
    String commands = "create calendar --name Work --timezone America/New_York\n"
        + "use calendar --name Work\n"
        + "create event \"Weekly Team Sync\" from "
        + "2025-11-15T14:00 to 2025-11-15T15:00 "
        + "repeats MW until 2025-12-31\n"
        + "edit events location \"Weekly Team Sync\" "
        + "from 2025-11-20T14:00 with \"Virtual Meeting Room\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();
    assertTrue(model.getCalendar("Work").getEventCount() > 0);
    assertTrue(outputStream.toString().contains("Recurring event created"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractEventNameForCopyWithUnclosedQuote() {
    String commandBody = "\"My Event on 2025-01-15T10:00";
    ParserUtils.extractEventNameForCopy(commandBody);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractEventNameForCopyWithoutOn() {
    String commandBody = "MyEvent at 2025-01-15T10:00";
    ParserUtils.extractEventNameForCopy(commandBody);
  }

  @Test
  public void testExtractEventNameForCopyWithoutQuotes() {
    String commandBody = "MyEvent on 2025-01-15T10:00";

    ParserUtils.SubjectAndRemainder result =
        ParserUtils.extractEventNameForCopy(commandBody);

    assertEquals("MyEvent", result.subject);
    assertEquals("on 2025-01-15T10:00", result.remainder);
  }

  @Test
  public void testExtractEventNameForCopyWithQuotes() {
    String commandBody = "\"Event With Spaces\" on 2025-01-15T10:00";

    ParserUtils.SubjectAndRemainder result =
        ParserUtils.extractEventNameForCopy(commandBody);

    assertEquals("Event With Spaces", result.subject);
    assertEquals("on 2025-01-15T10:00", result.remainder);
  }
}