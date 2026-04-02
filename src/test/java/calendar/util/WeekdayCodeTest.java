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
import java.time.DayOfWeek;
import org.junit.Before;
import org.junit.Test;

/**
 * Complete tests for WeekdayCode.
 */
public class WeekdayCodeTest {
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
  public void testMondayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 2 times\n"
        + "print events on 2025-11-17\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());


    WeekdayCode code = WeekdayCode.fromCode("M");
    assertEquals(DayOfWeek.MONDAY, code.getDayOfWeek());
    assertEquals("M", code.getCode());
  }

  @Test
  public void testTuesdayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-18T09:00 to 2025-11-18T09:30 repeats T for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("T");
    assertEquals(DayOfWeek.TUESDAY, code.getDayOfWeek());
  }

  @Test
  public void testWednesdayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-19T09:00 to 2025-11-19T09:30 repeats W for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("W");
    assertEquals(DayOfWeek.WEDNESDAY, code.getDayOfWeek());
  }

  @Test
  public void testThursdayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-20T09:00 to 2025-11-20T09:30 repeats R for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("R");
    assertEquals(DayOfWeek.THURSDAY, code.getDayOfWeek());
  }

  @Test
  public void testFridayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-21T09:00 to 2025-11-21T09:30 repeats F for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("F");
    assertEquals(DayOfWeek.FRIDAY, code.getDayOfWeek());
  }

  @Test
  public void testSaturdayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-22T09:00 to 2025-11-22T09:30 repeats S for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("S");
    assertEquals(DayOfWeek.SATURDAY, code.getDayOfWeek());
  }

  @Test
  public void testSundayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekly from 2025-11-23T09:00 to 2025-11-23T09:30 repeats U for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(2, model.getCalendar("Work").getEventCount());

    WeekdayCode code = WeekdayCode.fromCode("U");
    assertEquals(DayOfWeek.SUNDAY, code.getDayOfWeek());
  }

  @Test
  public void testMultipleWeekdayCodesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event MWF from 2025-11-17T09:00 to 2025-11-17T09:30 repeats MWF for 6 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(6, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testAllWeekdayCodesIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDays from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRFSU for 7 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(7, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testInvalidWeekdayCodeIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Invalid from 2025-11-17T09:00 to 2025-11-17T09:30 repeats X for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
  }

  @Test
  public void testGetFullNameMondayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode monday = WeekdayCode.fromCode("M");
    assertEquals("Monday", monday.getFullName());
  }

  @Test
  public void testGetFullNameTuesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-18T09:00 to 2025-11-18T09:30 repeats T for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode tuesday = WeekdayCode.fromCode("T");
    assertEquals("Tuesday", tuesday.getFullName());
  }

  @Test
  public void testGetFullNameWednesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-19T09:00 to 2025-11-19T09:30 repeats W for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode wednesday = WeekdayCode.fromCode("W");
    assertEquals("Wednesday", wednesday.getFullName());
  }

  @Test
  public void testGetFullNameThursdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-20T09:00 to 2025-11-20T09:30 repeats R for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode thursday = WeekdayCode.fromCode("R");
    assertEquals("Thursday", thursday.getFullName());
  }

  @Test
  public void testGetFullNameFridayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-21T09:00 to 2025-11-21T09:30 repeats F for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode friday = WeekdayCode.fromCode("F");
    assertEquals("Friday", friday.getFullName());
  }

  @Test
  public void testGetFullNameSaturdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-22T09:00 to 2025-11-22T09:30 repeats S for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode saturday = WeekdayCode.fromCode("S");
    assertEquals("Saturday", saturday.getFullName());
  }

  @Test
  public void testGetFullNameSundayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-23T09:00 to 2025-11-23T09:30 repeats U for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode sunday = WeekdayCode.fromCode("U");
    assertEquals("Sunday", sunday.getFullName());
  }



  @Test
  public void testGetAbbreviationMondayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode monday = WeekdayCode.fromCode("M");
    assertEquals("Mon", monday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationTuesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-18T09:00 to 2025-11-18T09:30 repeats T for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode tuesday = WeekdayCode.fromCode("T");
    assertEquals("Tue", tuesday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationWednesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-19T09:00 to 2025-11-19T09:30 repeats W for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode wednesday = WeekdayCode.fromCode("W");
    assertEquals("Wed", wednesday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationThursdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-20T09:00 to 2025-11-20T09:30 repeats R for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode thursday = WeekdayCode.fromCode("R");
    assertEquals("Thu", thursday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationFridayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-21T09:00 to 2025-11-21T09:30 repeats F for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode friday = WeekdayCode.fromCode("F");
    assertEquals("Fri", friday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationSaturdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-22T09:00 to 2025-11-22T09:30 repeats S for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode saturday = WeekdayCode.fromCode("S");
    assertEquals("Sat", saturday.getAbbreviation());
  }

  @Test
  public void testGetAbbreviationSundayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-23T09:00 to 2025-11-23T09:30 repeats U for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode sunday = WeekdayCode.fromCode("U");
    assertEquals("Sun", sunday.getAbbreviation());
  }



  @Test
  public void testIsWeekendSaturdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekend from 2025-11-22T09:00 to 2025-11-22T09:30 repeats S for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode saturday = WeekdayCode.fromCode("S");
    assertTrue(saturday.isWeekend());
    assertFalse(saturday.isWeekday());
  }

  @Test
  public void testIsWeekendSundayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekend from 2025-11-23T09:00 to 2025-11-23T09:30 repeats U for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode sunday = WeekdayCode.fromCode("U");
    assertTrue(sunday.isWeekend());
    assertFalse(sunday.isWeekday());
  }

  @Test
  public void testIsWeekdayMondayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekday from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode monday = WeekdayCode.fromCode("M");
    assertTrue(monday.isWeekday());
    assertFalse(monday.isWeekend());
  }

  @Test
  public void testIsWeekdayTuesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekday from 2025-11-18T09:00 to 2025-11-18T09:30 repeats T for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode tuesday = WeekdayCode.fromCode("T");
    assertTrue(tuesday.isWeekday());
    assertFalse(tuesday.isWeekend());
  }

  @Test
  public void testIsWeekdayWednesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekday from 2025-11-19T09:00 to 2025-11-19T09:30 repeats W for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode wednesday = WeekdayCode.fromCode("W");
    assertTrue(wednesday.isWeekday());
    assertFalse(wednesday.isWeekend());
  }

  @Test
  public void testIsWeekdayThursdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekday from 2025-11-20T09:00 to 2025-11-20T09:30 repeats R for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode thursday = WeekdayCode.fromCode("R");
    assertTrue(thursday.isWeekday());
    assertFalse(thursday.isWeekend());
  }

  @Test
  public void testIsWeekdayFridayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Weekday from 2025-11-21T09:00 to 2025-11-21T09:30 repeats F for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode friday = WeekdayCode.fromCode("F");
    assertTrue(friday.isWeekday());
    assertFalse(friday.isWeekend());
  }



  @Test
  public void testToStringIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats MWF for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode monday = WeekdayCode.fromCode("M");
    WeekdayCode wednesday = WeekdayCode.fromCode("W");
    WeekdayCode friday = WeekdayCode.fromCode("F");

    assertEquals("M", monday.toString());
    assertEquals("W", wednesday.toString());
    assertEquals("F", friday.toString());
  }



  @Test
  public void testFromDayOfWeekMondayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.MONDAY);
    assertEquals(WeekdayCode.MONDAY, code);
  }

  @Test
  public void testFromDayOfWeekTuesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-18T09:00 to 2025-11-18T09:30 repeats T for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.TUESDAY);
    assertEquals(WeekdayCode.TUESDAY, code);
  }

  @Test
  public void testFromDayOfWeekWednesdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-19T09:00 to 2025-11-19T09:30 repeats W for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.WEDNESDAY);
    assertEquals(WeekdayCode.WEDNESDAY, code);
  }

  @Test
  public void testFromDayOfWeekThursdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-20T09:00 to 2025-11-20T09:30 repeats R for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.THURSDAY);
    assertEquals(WeekdayCode.THURSDAY, code);
  }

  @Test
  public void testFromDayOfWeekFridayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-21T09:00 to 2025-11-21T09:30 repeats F for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.FRIDAY);
    assertEquals(WeekdayCode.FRIDAY, code);
  }

  @Test
  public void testFromDayOfWeekSaturdayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-22T09:00 to 2025-11-22T09:30 repeats S for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.SATURDAY);
    assertEquals(WeekdayCode.SATURDAY, code);
  }

  @Test
  public void testFromDayOfWeekSundayIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-23T09:00 to 2025-11-23T09:30 repeats U for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromDayOfWeek(DayOfWeek.SUNDAY);
    assertEquals(WeekdayCode.SUNDAY, code);
  }


  @Test
  public void testFromCodeNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    try {
      WeekdayCode.fromCode(null);
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null or empty"));
    }
  }

  @Test
  public void testFromCodeEmptyIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    try {
      WeekdayCode.fromCode("");
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null or empty"));
    }
  }

  @Test
  public void testFromCodeWhitespaceIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    try {
      WeekdayCode.fromCode("   ");
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null or empty"));
    }
  }

  @Test
  public void testFromDayOfWeekNullIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    try {
      WeekdayCode.fromDayOfWeek(null);
      assertTrue("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testFromCodeCaseInsensitiveIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats m for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode lowerM = WeekdayCode.fromCode("m");
    WeekdayCode upperM = WeekdayCode.fromCode("M");
    assertEquals(lowerM, upperM);
  }

  @Test
  public void testFromCodeWithWhitespaceIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Test from 2025-11-17T09:00 to 2025-11-17T09:30 repeats M for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    WeekdayCode code = WeekdayCode.fromCode(" M ");
    assertEquals(WeekdayCode.MONDAY, code);
  }

  @Test
  public void testMultipleWeekdayCodesIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event MWF from 2025-11-17T09:00 to 2025-11-17T09:30 repeats MWF for 6 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(6, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testAllWeekdayCodesIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDays from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRFSU for 7 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    assertEquals(7, model.getCalendar("Work").getEventCount());
  }

  @Test
  public void testInvalidWeekdayCodeIntegrated1() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Invalid from 2025-11-17T09:00 to 2025-11-17T09:30 repeats X for 2 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();
    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
  }

  @Test
  public void testFromCodeInvalidCharacterIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Invalid from 2025-11-17T09:00 to 2025-11-17T09:30 repeats Z for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Invalid weekday code") || output.contains("ERROR"));
  }



  @Test
  public void testAllWeekdaysFullNameIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDays from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRFSU for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();


    assertEquals("Monday", WeekdayCode.MONDAY.getFullName());
    assertEquals("Tuesday", WeekdayCode.TUESDAY.getFullName());
    assertEquals("Wednesday", WeekdayCode.WEDNESDAY.getFullName());
    assertEquals("Thursday", WeekdayCode.THURSDAY.getFullName());
    assertEquals("Friday", WeekdayCode.FRIDAY.getFullName());
    assertEquals("Saturday", WeekdayCode.SATURDAY.getFullName());
    assertEquals("Sunday", WeekdayCode.SUNDAY.getFullName());
  }

  @Test
  public void testAllWeekdaysAbbreviationIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDays from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRFSU for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();

    assertEquals("Mon", WeekdayCode.MONDAY.getAbbreviation());
    assertEquals("Tue", WeekdayCode.TUESDAY.getAbbreviation());
    assertEquals("Wed", WeekdayCode.WEDNESDAY.getAbbreviation());
    assertEquals("Thu", WeekdayCode.THURSDAY.getAbbreviation());
    assertEquals("Fri", WeekdayCode.FRIDAY.getAbbreviation());
    assertEquals("Sat", WeekdayCode.SATURDAY.getAbbreviation());
    assertEquals("Sun", WeekdayCode.SUNDAY.getAbbreviation());
  }

  @Test
  public void testWeekendVsWeekdayAllDaysIntegrated() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event AllDays from 2025-11-17T09:00 to 2025-11-17T09:30 "
        + "repeats MTWRFSU for 1 times\n"
        + "exit\n";
    CalendarController controller = new CalendarController(model, view, new StringReader(commands));
    controller.run();


    assertTrue(WeekdayCode.MONDAY.isWeekday());
    assertTrue(WeekdayCode.TUESDAY.isWeekday());
    assertTrue(WeekdayCode.WEDNESDAY.isWeekday());
    assertTrue(WeekdayCode.THURSDAY.isWeekday());
    assertTrue(WeekdayCode.FRIDAY.isWeekday());

    assertTrue(WeekdayCode.SATURDAY.isWeekend());
    assertTrue(WeekdayCode.SUNDAY.isWeekend());

    assertFalse(WeekdayCode.MONDAY.isWeekend());
    assertFalse(WeekdayCode.SATURDAY.isWeekday());
  }
}