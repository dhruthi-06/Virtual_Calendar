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
 * Comprehensive test cases for CalendarAnalytics to cover all branches and lines.
 * Focuses on covering SURVIVED mutations from pitest.
 */
public class CalendarAnalyticsComprehensiveTest {

  private List<InterfaceEvent> events;

  /**
   * Sets up test fixtures.
   */
  @Before
  public void setUp() {
    events = new ArrayList<>();
  }


  @Test
  public void testCalculateAnalyticsZeroDaysInRange() {
    LocalDate date = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, date, date);
    
    Assert.assertEquals(0, result.getTotalEvents());
    Assert.assertEquals(0.0, result.getAverageEventsPerDay(), 0.001);
  }


  @Test
  public void testCalculateEventsByWeekWithEventsOutsideRange() {
    events.add(new Event("Before", 
        LocalDateTime.of(2025, 11, 10, 10, 0),
        LocalDateTime.of(2025, 11, 10, 11, 0)));
    events.add(new Event("InRange", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("After", 
        LocalDateTime.of(2025, 11, 20, 10, 0),
        LocalDateTime.of(2025, 11, 20, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 16);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByWeek = result.getEventsByWeek();

    Assert.assertTrue(eventsByWeek.size() > 0);
  }


  @Test
  public void testCalculateEventsByWeekMultipleEventsSameWeek() {
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

    var eventsByWeek = result.getEventsByWeek();

    int totalInWeek = eventsByWeek.values().stream().mapToInt(Integer::intValue).sum();
    Assert.assertEquals(3, totalInWeek);
  }


  @Test
  public void testCalculateEventsByWeekEmptyWhenNoMatches() {
    events.add(new Event("Before", 
        LocalDateTime.of(2025, 11, 10, 10, 0),
        LocalDateTime.of(2025, 11, 10, 11, 0)));
    events.add(new Event("After", 
        LocalDateTime.of(2025, 11, 25, 10, 0),
        LocalDateTime.of(2025, 11, 25, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByWeek = result.getEventsByWeek();
    Assert.assertTrue(eventsByWeek.isEmpty());
  }


  @Test
  public void testCalculateEventsByMonthWithEventsOutsideRange() {
    events.add(new Event("Before", 
        LocalDateTime.of(2025, 10, 30, 10, 0),
        LocalDateTime.of(2025, 10, 30, 11, 0)));
    events.add(new Event("InRange", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("After", 
        LocalDateTime.of(2025, 12, 5, 10, 0),
        LocalDateTime.of(2025, 12, 5, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 1);
    LocalDate end = LocalDate.of(2025, 11, 30);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByMonth = result.getEventsByMonth();
    Assert.assertEquals(1, eventsByMonth.getOrDefault(11, 0).intValue());
  }


  @Test
  public void testCalculateEventsByMonthMultipleEventsSameMonth() {
    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 5, 10, 0),
        LocalDateTime.of(2025, 11, 5, 11, 0)));
    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 25, 10, 0),
        LocalDateTime.of(2025, 11, 25, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 1);
    LocalDate end = LocalDate.of(2025, 11, 30);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByMonth = result.getEventsByMonth();
    Assert.assertEquals(3, eventsByMonth.getOrDefault(11, 0).intValue());
  }

  @Test
  public void testCalculateEventsByMonthEmptyWhenNoMatches() {
    events.add(new Event("Before", 
        LocalDateTime.of(2025, 10, 30, 10, 0),
        LocalDateTime.of(2025, 10, 30, 11, 0)));
    events.add(new Event("After", 
        LocalDateTime.of(2025, 12, 5, 10, 0),
        LocalDateTime.of(2025, 12, 5, 11, 0)));

    LocalDate start = LocalDate.of(2025, 11, 1);
    LocalDate end = LocalDate.of(2025, 11, 30);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByMonth = result.getEventsByMonth();
    Assert.assertTrue(eventsByMonth.isEmpty());
  }

  @Test
  public void testCalculateEventsPerDaySingleDay() {
    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));

    LocalDate date = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, date, date);

    Assert.assertEquals(1, result.getTotalEvents());
    Assert.assertNotNull(result.getBusiestDay());
    Assert.assertEquals(date, result.getBusiestDay());
  }


  @Test
  public void testCalculateEventsPerDayEventStartsOnStartDate() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    events.add(new Event("Event1", 
        start.atStartOfDay(),
        start.atTime(10, 0)));

    LocalDate end = LocalDate.of(2025, 11, 20);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());
  }

  @Test
  public void testCalculateEventsPerDayEventStartsBeforeStartDate() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("Spanning", 
        LocalDateTime.of(2025, 11, 14, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());

    Assert.assertNotNull(result.getBusiestDay());
  }

  @Test
  public void testCalculateEventsPerDayEventEndsOnLastDate() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    
    events.add(new Event("Event1", 
        end.atStartOfDay(),
        end.atTime(10, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());
  }

  @Test
  public void testCalculateEventsPerDayEventEndsAfterEndDate() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);

    events.add(new Event("Spanning", 
        LocalDateTime.of(2025, 11, 19, 10, 0),
        LocalDateTime.of(2025, 11, 21, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());

    Assert.assertNotNull(result.getBusiestDay());
  }

  @Test
  public void testFindBusiestDayWithTie() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 17);
    

    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0)));
    

    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));
    events.add(new Event("Event4", 
        LocalDateTime.of(2025, 11, 16, 14, 0),
        LocalDateTime.of(2025, 11, 16, 15, 0)));
    

    events.add(new Event("Event5", 
        LocalDateTime.of(2025, 11, 17, 10, 0),
        LocalDateTime.of(2025, 11, 17, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);


    Assert.assertNotNull(result.getBusiestDay());
    int busiestDayValue = result.getBusiestDay().getDayOfMonth();
    Assert.assertTrue(busiestDayValue == 15 || busiestDayValue == 16);
  }


  @Test
  public void testFindLeastBusyDayWithTie() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 17);
    

    events.add(new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    

    events.add(new Event("Event2", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));

    events.add(new Event("Event3", 
        LocalDateTime.of(2025, 11, 17, 10, 0),
        LocalDateTime.of(2025, 11, 17, 11, 0)));
    events.add(new Event("Event4", 
        LocalDateTime.of(2025, 11, 17, 14, 0),
        LocalDateTime.of(2025, 11, 17, 15, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertNotNull(result.getLeastBusyDay());
    int leastBusyDayValue = result.getLeastBusyDay().getDayOfMonth();
    Assert.assertTrue(leastBusyDayValue == 15 || leastBusyDayValue == 16);
  }

  @Test
  public void testFindLeastBusyDayWithEmptyMap() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    events.add(new Event("Before", 
        LocalDateTime.of(2025, 11, 10, 10, 0),
        LocalDateTime.of(2025, 11, 10, 11, 0)));
    events.add(new Event("After", 
        LocalDateTime.of(2025, 11, 25, 10, 0),
        LocalDateTime.of(2025, 11, 25, 11, 0)));

    result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertNotNull(result.getLeastBusyDay());
  }

  @Test
  public void testCalculateOnlineOfflinePercentagesWithNullLocation() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation(null);
    events.add(event1);

    Event event2 = new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("");

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(0.0, result.getOnlinePercentage(), 0.01);
    Assert.assertEquals(100.0, result.getOfflinePercentage(), 0.01);
  }

  @Test
  public void testCalculateOnlineOfflinePercentagesLocationDoesNotContainOnline() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("Office Room 101");
    events.add(event1);

    Event event2 = new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("Conference Hall");

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(0.0, result.getOnlinePercentage(), 0.01);
    Assert.assertEquals(100.0, result.getOfflinePercentage(), 0.01);
  }


  @Test
  public void testCalculateOnlineOfflinePercentagesLocationContainsOnlineCaseInsensitive() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("Online Meeting");
    events.add(event1);

    Event event2 = new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("ONLINE");
    events.add(event2);

    Event event3 = new Event("Event3", 
        LocalDateTime.of(2025, 11, 15, 16, 0),
        LocalDateTime.of(2025, 11, 15, 17, 0));
    event3.setLocation("In Person");
    events.add(event3);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(66.67, result.getOnlinePercentage(), 0.1);
    Assert.assertEquals(33.33, result.getOfflinePercentage(), 0.1);
  }


  @Test
  public void testCalculateOnlineOfflinePercentagesLocationContainsOnlineAsSubstring() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("Join online via Zoom");
    events.add(event1);

    Event event2 = new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("Virtual - online platform");
    events.add(event2);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(100.0, result.getOnlinePercentage(), 0.01);
    Assert.assertEquals(0.0, result.getOfflinePercentage(), 0.01);
  }


  @Test
  public void testCalculateAnalyticsMultiDayEventSpanningRange() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("LongEvent", 
        LocalDateTime.of(2025, 11, 14, 10, 0),
        LocalDateTime.of(2025, 11, 21, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());

    Assert.assertNotNull(result.getBusiestDay());
  }

  @Test
  public void testCalculateAnalyticsEventsOnBoundaries() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);

    events.add(new Event("StartBoundary", 
        start.atStartOfDay(),
        start.atTime(10, 0)));
    

    events.add(new Event("EndBoundary", 
        end.atTime(14, 0),
        end.atTime(23, 59, 59)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(2, result.getTotalEvents());
  }


  @Test
  public void testCalculateAnalyticsEventEndsAtStartBoundary() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("BeforeStart", 
        LocalDateTime.of(2025, 11, 14, 10, 0),
        start.atStartOfDay()));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(0, result.getTotalEvents());
  }

  @Test
  public void testCalculateAnalyticsEventStartsAtEndBoundary() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("AtEnd", 
        end.atTime(23, 59, 59),
        LocalDateTime.of(2025, 11, 21, 10, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);


    Assert.assertEquals(0, result.getTotalEvents());
  }


  @Test
  public void testCalculateEventsByWeekdayBoundaryDates() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 16);
    
    events.add(new Event("Saturday", 
        start.atStartOfDay(),
        start.atTime(10, 0)));
    events.add(new Event("Sunday", 
        end.atStartOfDay(),
        end.atTime(10, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    var eventsByWeekday = result.getEventsByWeekday();
    Assert.assertTrue(eventsByWeekday.getOrDefault("Saturday", 0) >= 1);
    Assert.assertTrue(eventsByWeekday.getOrDefault("Sunday", 0) >= 1);
  }

  @Test
  public void testCalculateEventsByWeekdayEventStartsBeforeRange() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("Before", 
        LocalDateTime.of(2025, 11, 14, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());
  }


  @Test
  public void testCalculateEventsByWeekdayEventStartsAfterRange() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    

    events.add(new Event("After", 
        LocalDateTime.of(2025, 11, 19, 10, 0),
        LocalDateTime.of(2025, 11, 21, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(1, result.getTotalEvents());
    var eventsByWeekday = result.getEventsByWeekday();

    Assert.assertTrue(result.getTotalEvents() > 0);
  }

  @Test
  public void testCalculateAnalyticsAllDaysEqualCount() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 17);
    

    events.add(new Event("Day1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0)));
    events.add(new Event("Day2", 
        LocalDateTime.of(2025, 11, 16, 10, 0),
        LocalDateTime.of(2025, 11, 16, 11, 0)));
    events.add(new Event("Day3", 
        LocalDateTime.of(2025, 11, 17, 10, 0),
        LocalDateTime.of(2025, 11, 17, 11, 0)));

    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertNotNull(result.getBusiestDay());
    Assert.assertNotNull(result.getLeastBusyDay());

  }

  @Test
  public void testCalculateAnalyticsEmptyEventsNonEmptyRange() {
    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 20);
    
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(0, result.getTotalEvents());
    Assert.assertEquals(0.0, result.getAverageEventsPerDay(), 0.001);
    Assert.assertNotNull(result.getBusiestDay());
    Assert.assertNotNull(result.getLeastBusyDay());
  }


  @Test
  public void testCalculateOnlineOfflinePercentagesOnlineAtStart() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("online meeting");
    events.add(event1);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(100.0, result.getOnlinePercentage(), 0.01);
  }


  @Test
  public void testCalculateOnlineOfflinePercentagesOnlineAtEnd() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("meeting online");
    events.add(event1);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(100.0, result.getOnlinePercentage(), 0.01);
  }

  @Test
  public void testCalculateOnlineOfflinePercentagesMixedCase() {
    Event event1 = new Event("Event1", 
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    event1.setLocation("Online");
    events.add(event1);

    Event event2 = new Event("Event2", 
        LocalDateTime.of(2025, 11, 15, 14, 0),
        LocalDateTime.of(2025, 11, 15, 15, 0));
    event2.setLocation("ONLINE");
    events.add(event2);

    Event event3 = new Event("Event3", 
        LocalDateTime.of(2025, 11, 15, 16, 0),
        LocalDateTime.of(2025, 11, 15, 17, 0));
    event3.setLocation("online");
    events.add(event3);

    LocalDate start = LocalDate.of(2025, 11, 15);
    LocalDate end = LocalDate.of(2025, 11, 15);
    AnalyticsResult result = CalendarAnalytics.calculateAnalytics(events, start, end);

    Assert.assertEquals(100.0, result.getOnlinePercentage(), 0.01);
  }
}


