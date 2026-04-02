package calendar.util;

import calendar.model.Event;
import calendar.model.InterfaceEvent;
import calendar.util.CalendarAnalytics.AnalyticsResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for CalendarAnalytics utility class.
 */
public class CalendarAnalyticsTest {

  private List<InterfaceEvent> events;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    events = new ArrayList<>();
  }

  @Test
  public void testCalculateAnalyticsEmptyList() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(0, result.getTotalEvents());
    Assert.assertTrue(result.getEventsBySubject().isEmpty());
    Assert.assertEquals(0.0, result.getAverageEventsPerDay(), 0.01);
  }

  @Test
  public void testCalculateAnalyticsTotalEvents() {
    events.add(new Event("Meeting", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Lunch", 
        LocalDateTime.of(2025, 11, 15, 12, 0),
        LocalDateTime.of(2025, 11, 15, 13, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(2, result.getTotalEvents());
  }

  @Test
  public void testCalculateAnalyticsBySubject() {
    events.add(new Event("Meeting", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Meeting", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));
    events.add(new Event("Lunch", 
        LocalDateTime.of(2025, 11, 15, 12, 0),
        LocalDateTime.of(2025, 11, 15, 13, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 16);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var bySubject = result.getEventsBySubject();
    Assert.assertEquals(2, bySubject.get("Meeting").intValue());
    Assert.assertEquals(1, bySubject.get("Lunch").intValue());
  }

  @Test
  public void testCalculateAnalyticsByWeekday() {

    events.add(new Event("MondayEvent", 
        LocalDateTime.of(2025, 11, 17, 10, 0),
        LocalDateTime.of(2025, 11, 17, 11, 0)));

    events.add(new Event("TuesdayEvent", 
        LocalDateTime.of(2025, 11, 18, 10, 0),
        LocalDateTime.of(2025, 11, 18, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 17);
    LocalDate end = LocalDate.of(2025, 11, 18);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var byWeekday = result.getEventsByWeekday();
    Assert.assertEquals(1, byWeekday.get("Monday").intValue());
    Assert.assertEquals(1, byWeekday.get("Tuesday").intValue());
  }

  @Test
  public void testCalculateAnalyticsAveragePerDay() {
    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));
    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 17, 10, 0),
        LocalDateTime.of(2025, 11, 17, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 17);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);


    Assert.assertEquals(1.0, result.getAverageEventsPerDay(), 0.01);
  }

  @Test
  public void testCalculateAnalyticsBusiestDay() {
    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 12, 0),
        LocalDateTime.of(2025, 11, 15, 13, 0)));
    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 16);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(LocalDate.of(2025, 11, 15), result.getBusiestDay());
  }

  @Test
  public void testCalculateAnalyticsOnlinePercentage() {
    Event event1 = new Event("OnlineMeeting", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("Online");
    events.add(event1);

    Event event2 = new Event("InPersonMeeting", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("Office");
    events.add(event2);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(50.0, result.getOnlinePercentage(), 0.01);
    Assert.assertEquals(50.0, result.getOfflinePercentage(), 0.01);
  }

  @Test
  public void testCalculateAnalyticsNullDates() {
    try {
      CalendarAnalytics.calculateAnalytics(events, null, LocalDate.now());
      Assert.fail("Should throw exception for null start date");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("cannot be null"));
    }

    try {
      CalendarAnalytics.calculateAnalytics(events, LocalDate.now(), null);
      Assert.fail("Should throw exception for null end date");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("cannot be null"));
    }
  }

  @Test
  public void testCalculateAnalyticsInvalidRange() {
    LocalDate start = LocalDate.of(2025, 11, 20);
    LocalDate end = LocalDate.of(2025, 11, 15);
    try {
      CalendarAnalytics.calculateAnalytics(events, start, end);
      Assert.fail("Should throw exception for invalid date range");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("after"));
    }
  }

  @Test
  public void testCalculateAnalyticsNullEventsList() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(null, start, end);
    Assert.assertEquals(0, result.getTotalEvents());
  }

  @Test
  public void testCalculateAnalyticsEventsOutsideRange() {
    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 10, 10, 0),
        LocalDateTime.of(2025, 11, 10, 11, 0)));
    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 25, 10, 0),
        LocalDateTime.of(2025, 11, 25, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);


    Assert.assertEquals(1, result.getTotalEvents());
  }
}

