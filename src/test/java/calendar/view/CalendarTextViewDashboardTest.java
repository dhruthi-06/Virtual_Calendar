package calendar.view;

import calendar.util.CalendarAnalytics.AnalyticsResult;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for CalendarTextView.displayDashboard method.
 * Focuses on covering all branches and lines for mutation testing.
 */
public class CalendarTextViewDashboardTest {

  private CalendarTextView view;
  private ByteArrayOutputStream outputStream;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
  }

  @Test
  public void testDisplayDashboardEmptyEvents() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(0, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.0, null, null,
            0.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("=== Calendar Dashboard ==="));
    Assert.assertTrue(output.contains("2025-11-15 to 2025-11-20"));
    Assert.assertTrue(output.contains("Total number of events: 0"));
    Assert.assertTrue(output.contains("Total number of events by subject"));
    Assert.assertTrue(output.contains("(none)"));
    Assert.assertTrue(output.contains("Total number of events by weekday"));
    Assert.assertTrue(output.contains("Total number of events by week"));
    Assert.assertTrue(output.contains("(none)"));
    Assert.assertTrue(output.contains("Total number of events by month"));
    Assert.assertTrue(output.contains("Busiest day: (none)"));
    Assert.assertTrue(output.contains("Least busy day: (none)"));
  }

  @Test
  public void testDisplayDashboardWithEventsBySubject() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 3);
    eventsBySubject.put("Lunch", 2);
    eventsBySubject.put("Review", 1);

    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(6, eventsBySubject, emptyWeekday, emptyWeek, emptyMonth, 1.0, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events: 6"));
    Assert.assertTrue(output.contains("Meeting: 3"));
    Assert.assertTrue(output.contains("Lunch: 2"));
    Assert.assertTrue(output.contains("Review: 1"));

    int meetingPos = output.indexOf("Meeting: 3");
    int lunchPos = output.indexOf("Lunch: 2");
    int reviewPos = output.indexOf("Review: 1");
    Assert.assertTrue(meetingPos < lunchPos);
    Assert.assertTrue(lunchPos < reviewPos);
  }

  @Test
  public void testDisplayDashboardWithEventsByWeekday() {
    LocalDate start = LocalDate.of(2025, 11, 17);
    final LocalDate end = LocalDate.of(2025, 11, 23);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> eventsByWeekday = new HashMap<>();
    eventsByWeekday.put("Monday", 2);
    eventsByWeekday.put("Tuesday", 1);
    eventsByWeekday.put("Wednesday", 0);
    eventsByWeekday.put("Thursday", 3);
    eventsByWeekday.put("Friday", 0);
    eventsByWeekday.put("Saturday", 1);
    eventsByWeekday.put("Sunday", 0);

    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(7, emptySubject, eventsByWeekday, emptyWeek, emptyMonth, 1.0, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by weekday"));
    Assert.assertTrue(output.contains("Monday: 2"));
    Assert.assertTrue(output.contains("Tuesday: 1"));
    Assert.assertTrue(output.contains("Wednesday: 0"));
    Assert.assertTrue(output.contains("Thursday: 3"));
    Assert.assertTrue(output.contains("Friday: 0"));
    Assert.assertTrue(output.contains("Saturday: 1"));
    Assert.assertTrue(output.contains("Sunday: 0"));
  }

  @Test
  public void testDisplayDashboardWithEventsByWeek() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 30);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> eventsByWeek = new HashMap<>();
    eventsByWeek.put(46, 5);
    eventsByWeek.put(47, 3);
    eventsByWeek.put(48, 2);

    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(10, emptySubject, emptyWeekday, eventsByWeek, emptyMonth, 0.625, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by week"));
    Assert.assertTrue(output.contains("Week 46: 5"));
    Assert.assertTrue(output.contains("Week 47: 3"));
    Assert.assertTrue(output.contains("Week 48: 2"));

    int week46Pos = output.indexOf("Week 46");
    int week47Pos = output.indexOf("Week 47");
    int week48Pos = output.indexOf("Week 48");
    Assert.assertTrue(week46Pos < week47Pos);
    Assert.assertTrue(week47Pos < week48Pos);
  }

  @Test
  public void testDisplayDashboardWithEmptyEventsByWeek() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(0, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.0, null, null,
            0.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by week"));
    Assert.assertTrue(output.contains("(none)"));
  }

  @Test
  public void testDisplayDashboardWithEventsByMonth() {
    final LocalDate start = LocalDate.of(2025, 11, 1);
    final LocalDate end = LocalDate.of(2025, 12, 31);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(11, 10);
    eventsByMonth.put(12, 5);

    AnalyticsResult analytics =
        new AnalyticsResult(15, emptySubject, emptyWeekday, emptyWeek, eventsByMonth, 0.25, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by month"));
    Assert.assertTrue(output.contains("November: 10"));
    Assert.assertTrue(output.contains("December: 5"));

    int novemberPos = output.indexOf("November: 10");
    int decemberPos = output.indexOf("December: 5");
    Assert.assertTrue(novemberPos < decemberPos);
  }

  @Test
  public void testDisplayDashboardWithEmptyEventsByMonth() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(0, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.0, null, null,
            0.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Total number of events by month"));
    Assert.assertTrue(output.contains("(none)"));
  }

  @Test
  public void testDisplayDashboardWithAllMonths() {
    final LocalDate start = LocalDate.of(2025, 1, 1);
    final LocalDate end = LocalDate.of(2025, 12, 31);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(1, 1);
    eventsByMonth.put(6, 1);
    eventsByMonth.put(12, 1);

    AnalyticsResult analytics =
        new AnalyticsResult(3, emptySubject, emptyWeekday, emptyWeek, eventsByMonth, 0.008, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("January: 1"));
    Assert.assertTrue(output.contains("June: 1"));
    Assert.assertTrue(output.contains("December: 1"));
  }

  @Test
  public void testDisplayDashboardAverageEventsPerDay() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 17);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(6, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 2.0, start, start,
            0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Average number of events per day: 2.00"));
  }

  @Test
  public void testDisplayDashboardWithNullBusiestDay() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(0, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.0, null, null,
            0.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Busiest day: (none)"));
  }

  @Test
  public void testDisplayDashboardWithNullLeastBusyDay() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(0, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.0, null, null,
            0.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Least busy day: (none)"));
  }

  @Test
  public void testDisplayDashboardOnlinePercentage() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(10, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 1.67, start,
            start, 30.0, 70.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Percentage of events:"));
    Assert.assertTrue(output.contains("Online: 30.00%"));
    Assert.assertTrue(output.contains("Not online: 70.00%"));
  }

  @Test
  public void testDisplayDashboardAllOnline() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(5, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 0.83, start,
            start, 100.0, 0.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Online: 100.00%"));
    Assert.assertTrue(output.contains("Not online: 0.00%"));
  }

  @Test
  public void testDisplayDashboardAllOffline() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 20);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(8, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 1.33, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Online: 0.00%"));
    Assert.assertTrue(output.contains("Not online: 100.00%"));
  }

  @Test
  public void testDisplayDashboardCompleteScenario() {
    final LocalDate start = LocalDate.of(2025, 11, 1);
    final LocalDate end = LocalDate.of(2025, 11, 30);
    final LocalDate busiest = LocalDate.of(2025, 11, 15);
    final LocalDate leastBusy = LocalDate.of(2025, 11, 30);

    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Meeting", 10);
    eventsBySubject.put("Lunch", 5);

    Map<String, Integer> eventsByWeekday = new HashMap<>();
    eventsByWeekday.put("Monday", 3);
    eventsByWeekday.put("Tuesday", 2);
    eventsByWeekday.put("Wednesday", 4);
    eventsByWeekday.put("Thursday", 3);
    eventsByWeekday.put("Friday", 3);
    eventsByWeekday.put("Saturday", 0);
    eventsByWeekday.put("Sunday", 0);

    Map<Integer, Integer> eventsByWeek = new HashMap<>();
    eventsByWeek.put(44, 5);
    eventsByWeek.put(45, 8);
    eventsByWeek.put(46, 7);

    Map<Integer, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(11, 20);

    AnalyticsResult analytics =
        new AnalyticsResult(20, eventsBySubject, eventsByWeekday, eventsByWeek, eventsByMonth, 0.67,
            busiest, leastBusy, 25.0, 75.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("=== Calendar Dashboard ==="));
    Assert.assertTrue(output.contains("2025-11-01 to 2025-11-30"));
    Assert.assertTrue(output.contains("Total number of events: 20"));
    Assert.assertTrue(output.contains("Meeting: 10"));
    Assert.assertTrue(output.contains("Lunch: 5"));
    Assert.assertTrue(output.contains("Monday: 3"));
    Assert.assertTrue(output.contains("Week 44: 5"));
    Assert.assertTrue(output.contains("November: 20"));
    Assert.assertTrue(output.contains("Average number of events per day: 0.67"));
    Assert.assertTrue(output.contains("Busiest day: 2025-11-15"));
    Assert.assertTrue(output.contains("Least busy day: 2025-11-30"));
    Assert.assertTrue(output.contains("Online: 25.00%"));
    Assert.assertTrue(output.contains("Not online: 75.00%"));
  }

  @Test
  public void testDisplayDashboardSingleDayRange() {
    final LocalDate date = LocalDate.of(2025, 11, 15);

    Map<String, Integer> eventsBySubject = new HashMap<>();
    eventsBySubject.put("Event1", 1);

    Map<String, Integer> eventsByWeekday = new HashMap<>();
    eventsByWeekday.put("Friday", 1);

    Map<Integer, Integer> eventsByWeek = new HashMap<>();
    eventsByWeek.put(46, 1);

    Map<Integer, Integer> eventsByMonth = new HashMap<>();
    eventsByMonth.put(11, 1);

    AnalyticsResult analytics =
        new AnalyticsResult(1, eventsBySubject, eventsByWeekday, eventsByWeek, eventsByMonth, 1.0,
            date, date, 0.0, 100.0);

    view.displayDashboard(analytics, date, date);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("2025-11-15 to 2025-11-15"));
    Assert.assertTrue(output.contains("Total number of events: 1"));
    Assert.assertTrue(output.contains("Average number of events per day: 1.00"));
  }

  @Test
  public void testDisplayDashboardWithDecimalAverage() {
    final LocalDate start = LocalDate.of(2025, 11, 15);
    final LocalDate end = LocalDate.of(2025, 11, 17);

    Map<String, Integer> emptySubject = new HashMap<>();
    Map<String, Integer> emptyWeekday = new HashMap<>();
    Map<Integer, Integer> emptyWeek = new HashMap<>();
    Map<Integer, Integer> emptyMonth = new HashMap<>();

    AnalyticsResult analytics =
        new AnalyticsResult(7, emptySubject, emptyWeekday, emptyWeek, emptyMonth, 2.333333, start,
            start, 0.0, 100.0);

    view.displayDashboard(analytics, start, end);

    String output = outputStream.toString();
    Assert.assertTrue(output.contains("Average number of events per day: 2.33"));
  }
}


