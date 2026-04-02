package calendar.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Complete tests for DateTimeParser.
 */
public class DateTimeParserTest {
  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;

  /**
   * Sets up the test environment before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }



  @Test
  public void testParseDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Meeting"));

    LocalDate date = DateTimeParser.parseDate("2025-11-15");
    assertEquals(2025, date.getYear());
    assertEquals(11, date.getMonthValue());
    assertEquals(15, date.getDayOfMonth());
  }

  @Test
  public void testParseDateLeapYearIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event LeapDay on 2024-02-29\n"
        + "print events on 2024-02-29\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("Event created"));

    LocalDate date = DateTimeParser.parseDate("2024-02-29");
    assertEquals(2024, date.getYear());
    assertEquals(2, date.getMonthValue());
    assertEquals(29, date.getDayOfMonth());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDate(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateEmptyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDate("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateInvalidFormatIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDate("11/15/2025");
  }



  @Test
  public void testParseDateTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "show status on 2025-11-15T10:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("BUSY"));

    LocalDateTime dateTime = DateTimeParser.parseDateTime("2025-11-15T10:30");
    assertEquals(10, dateTime.getHour());
    assertEquals(30, dateTime.getMinute());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDateTime(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeEmptyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDateTime("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseDateTimeInvalidFormatIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseDateTime("2025-11-15 10:30");
  }



  @Test
  public void testParseTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime time = DateTimeParser.parseTime("10:30");
    assertEquals(10, time.getHour());
    assertEquals(30, time.getMinute());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseTimeNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseTime(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseTimeEmptyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseTime("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseTimeInvalidFormatIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.parseTime("10:30 AM");
  }



  @Test
  public void testCombineDateTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime combined = DateTimeParser.combineDateTime("2025-11-15", "10:00");
    assertNotNull(combined);
    assertEquals(2025, combined.getYear());
    assertEquals(11, combined.getMonthValue());
    assertEquals(15, combined.getDayOfMonth());
    assertEquals(10, combined.getHour());
    assertEquals(0, combined.getMinute());
  }

  @Test
  public void testFormatDateIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDate date = LocalDate.of(2025, 11, 15);
    String formatted = DateTimeParser.formatDate(date);
    assertEquals("2025-11-15", formatted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateNullIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatDate(null);
  }



  @Test
  public void testFormatDateTimeIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 10, 30);
    String formatted = DateTimeParser.formatDateTime(dateTime);
    assertEquals("2025-11-15T10:30", formatted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateTimeNullIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatDateTime(null);
  }



  @Test
  public void testFormatTimeIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime time = LocalTime.of(10, 30);
    String formatted = DateTimeParser.formatTime(time);
    assertEquals("10:30", formatted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatTimeNullIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatTime(null);
  }



  @Test
  public void testRoundTripDateIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDate original = LocalDate.of(2025, 11, 15);
    String formatted = DateTimeParser.formatDate(original);
    LocalDate parsed = DateTimeParser.parseDate(formatted);
    assertEquals(original, parsed);
  }

  @Test
  public void testRoundTripDateTimeIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime original = LocalDateTime.of(2025, 11, 15, 10, 30);
    String formatted = DateTimeParser.formatDateTime(original);
    LocalDateTime parsed = DateTimeParser.parseDateTime(formatted);
    assertEquals(original, parsed);
  }

  @Test
  public void testRoundTripTimeIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime original = LocalTime.of(10, 30);
    String formatted = DateTimeParser.formatTime(original);
    LocalTime parsed = DateTimeParser.parseTime(formatted);
    assertEquals(original, parsed);
  }



  @Test
  public void testIsValidDateTrueIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertTrue(DateTimeParser.isValidDate("2025-11-15"));
    assertTrue(DateTimeParser.isValidDate("2024-02-29"));
    assertTrue(DateTimeParser.isValidDate("2025-01-01"));
    assertTrue(DateTimeParser.isValidDate("2025-12-31"));
  }




  @Test
  public void testIsValidDateTimeTrueIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertTrue(DateTimeParser.isValidDateTime("2025-11-15T10:30"));
    assertTrue(DateTimeParser.isValidDateTime("2025-01-01T00:00"));
    assertTrue(DateTimeParser.isValidDateTime("2025-12-31T23:59"));
    assertTrue(DateTimeParser.isValidDateTime("2024-02-29T12:00"));
  }

  @Test
  public void testIsValidDateTimeFalseIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertFalse(DateTimeParser.isValidDateTime("2025-11-15 10:30"));
    assertFalse(DateTimeParser.isValidDateTime("2025-11-15T25:00"));
    assertFalse(DateTimeParser.isValidDateTime("2025-11-15T10:60"));
    assertFalse(DateTimeParser.isValidDateTime("invalid"));
    assertFalse(DateTimeParser.isValidDateTime(null));
    assertFalse(DateTimeParser.isValidDateTime(""));
    assertFalse(DateTimeParser.isValidDateTime("   "));
  }


  @Test
  public void testIsValidTimeTrueIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertTrue(DateTimeParser.isValidTime("10:30"));
    assertTrue(DateTimeParser.isValidTime("00:00"));
    assertTrue(DateTimeParser.isValidTime("23:59"));
    assertTrue(DateTimeParser.isValidTime("12:00"));
  }

  @Test
  public void testIsValidTimeFalseIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertFalse(DateTimeParser.isValidTime("10:30 AM"));
    assertFalse(DateTimeParser.isValidTime("25:00"));
    assertFalse(DateTimeParser.isValidTime("10:60"));
    assertFalse(DateTimeParser.isValidTime("invalid"));
    assertFalse(DateTimeParser.isValidTime(null));
    assertFalse(DateTimeParser.isValidTime(""));
    assertFalse(DateTimeParser.isValidTime("   "));
  }



  @Test
  public void testFormatDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDate date = LocalDate.of(2025, 11, 15);
    String formatted = DateTimeParser.formatDate(date);
    assertEquals("2025-11-15", formatted);
  }

  @Test
  public void testFormatDateTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 10, 30);
    String formatted = DateTimeParser.formatDateTime(dateTime);
    assertEquals("2025-11-15T10:30", formatted);
  }

  @Test
  public void testFormatTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime time = LocalTime.of(10, 30);
    String formatted = DateTimeParser.formatTime(time);
    assertEquals("10:30", formatted);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatDate(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatDateTimeNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatDateTime(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatTimeNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    DateTimeParser.formatTime(null);
  }



  @Test
  public void testParseDateWithWhitespaceIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDate date = DateTimeParser.parseDate("  2025-11-15  ");
    assertEquals(2025, date.getYear());
    assertEquals(11, date.getMonthValue());
    assertEquals(15, date.getDayOfMonth());
  }

  @Test
  public void testParseDateTimeWithWhitespaceIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime dateTime = DateTimeParser.parseDateTime("  2025-11-15T10:30  ");
    assertEquals(10, dateTime.getHour());
    assertEquals(30, dateTime.getMinute());
  }

  @Test
  public void testParseTimeWithWhitespaceIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime time = DateTimeParser.parseTime("  10:30  ");
    assertEquals(10, time.getHour());
    assertEquals(30, time.getMinute());
  }

  @Test
  public void testRoundTripDateIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDate original = LocalDate.of(2025, 11, 15);
    String formatted = DateTimeParser.formatDate(original);
    LocalDate parsed = DateTimeParser.parseDate(formatted);
    assertEquals(original, parsed);
  }

  @Test
  public void testRoundTripDateTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime original = LocalDateTime.of(2025, 11, 15, 10, 30);
    String formatted = DateTimeParser.formatDateTime(original);
    LocalDateTime parsed = DateTimeParser.parseDateTime(formatted);
    assertEquals(original, parsed);
  }

  @Test
  public void testRoundTripTimeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:30 to 2025-11-15T11:30\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalTime original = LocalTime.of(10, 30);
    String formatted = DateTimeParser.formatTime(original);
    LocalTime parsed = DateTimeParser.parseTime(formatted);
    assertEquals(original, parsed);
  }

  @Test
  public void testCombineDateTime() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    LocalDateTime combined = DateTimeParser.combineDateTime("2025-11-15", "10:00");
    assertNotNull(combined);
    assertEquals(2025, combined.getYear());
    assertEquals(11, combined.getMonthValue());
    assertEquals(15, combined.getDayOfMonth());
    assertEquals(10, combined.getHour());
    assertEquals(0, combined.getMinute());
  }



  @Test
  public void testIsValidDateVariousFormatsIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test on 2025-11-15\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();


    assertTrue(DateTimeParser.isValidDate("2025-11-15"));
    assertTrue(DateTimeParser.isValidDate("2000-01-01"));
    assertTrue(DateTimeParser.isValidDate("2099-12-31"));


    assertFalse(DateTimeParser.isValidDate("2025/11/15"));
    assertFalse(DateTimeParser.isValidDate("15-11-2025"));
    assertFalse(DateTimeParser.isValidDate("2025-11-32"));
  }

}