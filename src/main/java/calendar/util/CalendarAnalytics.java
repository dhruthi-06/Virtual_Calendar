package calendar.util;

import calendar.model.InterfaceEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for calculating calendar analytics and metrics.
 * Provides methods to analyze events within a date range.
 */
public class CalendarAnalytics {

  private CalendarAnalytics() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Data class to hold all analytics metrics for a date range.
   */
  public static class AnalyticsResult {
    private final int totalEvents;
    private final Map<String, Integer> eventsBySubject;
    private final Map<String, Integer> eventsByWeekday;
    private final Map<Integer, Integer> eventsByWeek;
    private final Map<Integer, Integer> eventsByMonth;
    private final double averageEventsPerDay;
    private final LocalDate busiestDay;
    private final LocalDate leastBusyDay;
    private final double onlinePercentage;
    private final double offlinePercentage;

    /**
     * Constructs an AnalyticsResult with all metrics.
     */
    public AnalyticsResult(int totalEvents, Map<String, Integer> eventsBySubject,
                          Map<String, Integer> eventsByWeekday,
                          Map<Integer, Integer> eventsByWeek,
                          Map<Integer, Integer> eventsByMonth,
                          double averageEventsPerDay,
                          LocalDate busiestDay, LocalDate leastBusyDay,
                          double onlinePercentage, double offlinePercentage) {
      this.totalEvents = totalEvents;
      this.eventsBySubject = new HashMap<>(eventsBySubject);
      this.eventsByWeekday = new HashMap<>(eventsByWeekday);
      this.eventsByWeek = new HashMap<>(eventsByWeek);
      this.eventsByMonth = new HashMap<>(eventsByMonth);
      this.averageEventsPerDay = averageEventsPerDay;
      this.busiestDay = busiestDay;
      this.leastBusyDay = leastBusyDay;
      this.onlinePercentage = onlinePercentage;
      this.offlinePercentage = offlinePercentage;
    }

    public int getTotalEvents() {
      return totalEvents;
    }

    public Map<String, Integer> getEventsBySubject() {
      return new HashMap<>(eventsBySubject);
    }

    public Map<String, Integer> getEventsByWeekday() {
      return new HashMap<>(eventsByWeekday);
    }

    public Map<Integer, Integer> getEventsByWeek() {
      return new HashMap<>(eventsByWeek);
    }

    public Map<Integer, Integer> getEventsByMonth() {
      return new HashMap<>(eventsByMonth);
    }

    public double getAverageEventsPerDay() {
      return averageEventsPerDay;
    }

    public LocalDate getBusiestDay() {
      return busiestDay;
    }

    public LocalDate getLeastBusyDay() {
      return leastBusyDay;
    }

    public double getOnlinePercentage() {
      return onlinePercentage;
    }

    public double getOfflinePercentage() {
      return offlinePercentage;
    }
  }

  /**
   * Calculates analytics for events within a date range.
   *
   * @param events list of events to analyze
   * @param startDate start of the date range (inclusive)
   * @param endDate end of the date range (inclusive)
   * @return AnalyticsResult containing all calculated metrics
   */
  public static AnalyticsResult calculateAnalytics(List<InterfaceEvent> events,
                                                   LocalDate startDate,
                                                   LocalDate endDate) {
    if (events == null) {
      events = new ArrayList<>();
    }
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }


    List<InterfaceEvent> filteredEvents = filterEventsInRange(events, startDate, endDate);


    int totalEvents = filteredEvents.size();
    Map<String, Integer> eventsBySubject = calculateEventsBySubject(filteredEvents);
    Map<String, Integer> eventsByWeekday = calculateEventsByWeekday(filteredEvents, startDate,
        endDate);
    Map<Integer, Integer> eventsByWeek = calculateEventsByWeek(filteredEvents, startDate,
        endDate);
    Map<Integer, Integer> eventsByMonth = calculateEventsByMonth(filteredEvents, startDate,
        endDate);

    long daysInRange = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    double averageEventsPerDay = daysInRange > 0 ? (double) totalEvents / daysInRange : 0.0;

    Map<LocalDate, Integer> eventsPerDay = calculateEventsPerDay(filteredEvents, startDate,
        endDate);
    LocalDate busiestDay = findBusiestDay(eventsPerDay);
    LocalDate leastBusyDay = findLeastBusyDay(eventsPerDay);

    double[] onlineOfflinePercentages = calculateOnlineOfflinePercentages(filteredEvents);
    double onlinePercentage = onlineOfflinePercentages[0];
    double offlinePercentage = onlineOfflinePercentages[1];

    return new AnalyticsResult(totalEvents, eventsBySubject, eventsByWeekday,
        eventsByWeek, eventsByMonth, averageEventsPerDay,
        busiestDay, leastBusyDay, onlinePercentage, offlinePercentage);
  }

  /**
   * Filters events that overlap with the date range.
   */
  private static List<InterfaceEvent> filterEventsInRange(List<InterfaceEvent> events,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
    List<InterfaceEvent> filtered = new ArrayList<>();
    LocalDateTime rangeStart = startDate.atStartOfDay();
    LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);

    for (InterfaceEvent event : events) {

      if (event.getStart().isBefore(rangeEnd) && event.getEnd().isAfter(rangeStart)) {
        filtered.add(event);
      }
    }
    return filtered;
  }

  /**
   * Calculates the count of events by subject.
   */
  private static Map<String, Integer> calculateEventsBySubject(List<InterfaceEvent> events) {
    Map<String, Integer> bySubject = new HashMap<>();
    for (InterfaceEvent event : events) {
      String subject = event.getSubject();
      bySubject.put(subject, bySubject.getOrDefault(subject, 0) + 1);
    }
    return bySubject;
  }

  /**
   * Calculates the count of events by weekday.
   * For events spanning multiple days, counts each day the event occurs.
   */
  private static Map<String, Integer> calculateEventsByWeekday(List<InterfaceEvent> events,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
    Map<String, Integer> byWeekday = new HashMap<>();
    String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday"};


    for (String day : weekdays) {
      byWeekday.put(day, 0);
    }


    for (InterfaceEvent event : events) {
      LocalDate eventStartDate = event.getStart().toLocalDate();
      if (!eventStartDate.isBefore(startDate) && !eventStartDate.isAfter(endDate)) {
        String weekday = eventStartDate.getDayOfWeek().toString();
        weekday = weekday.substring(0, 1) + weekday.substring(1).toLowerCase();
        byWeekday.put(weekday, byWeekday.getOrDefault(weekday, 0) + 1);
      }
    }

    return byWeekday;
  }

  /**
   * Calculates the count of events by week number.
   * Uses ISO week numbering.
   */
  private static Map<Integer, Integer> calculateEventsByWeek(List<InterfaceEvent> events,
                                                            LocalDate startDate,
                                                            LocalDate endDate) {
    Map<Integer, Integer> byWeek = new HashMap<>();
    WeekFields weekFields = WeekFields.of(Locale.getDefault());

    for (InterfaceEvent event : events) {
      LocalDate eventStartDate = event.getStart().toLocalDate();
      if (!eventStartDate.isBefore(startDate) && !eventStartDate.isAfter(endDate)) {
        int weekNumber = eventStartDate.get(weekFields.weekOfWeekBasedYear());
        byWeek.put(weekNumber, byWeek.getOrDefault(weekNumber, 0) + 1);
      }
    }

    return byWeek;
  }

  /**
   * Calculates the count of events by month.
   */
  private static Map<Integer, Integer> calculateEventsByMonth(List<InterfaceEvent> events,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
    Map<Integer, Integer> byMonth = new HashMap<>();

    for (InterfaceEvent event : events) {
      LocalDate eventStartDate = event.getStart().toLocalDate();
      if (!eventStartDate.isBefore(startDate) && !eventStartDate.isAfter(endDate)) {
        int month = eventStartDate.getMonthValue();
        byMonth.put(month, byMonth.getOrDefault(month, 0) + 1);
      }
    }

    return byMonth;
  }

  /**
   * Calculates the number of events per day in the range.
   */
  private static Map<LocalDate, Integer> calculateEventsPerDay(List<InterfaceEvent> events,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
    Map<LocalDate, Integer> eventsPerDay = new HashMap<>();


    LocalDate current = startDate;
    while (!current.isAfter(endDate)) {
      eventsPerDay.put(current, 0);
      current = current.plusDays(1);
    }


    for (InterfaceEvent event : events) {
      LocalDate eventStart = event.getStart().toLocalDate();
      LocalDate eventEnd = event.getEnd().toLocalDate();

      LocalDate currentDate = eventStart.isBefore(startDate) ? startDate : eventStart;
      LocalDate lastDate = eventEnd.isAfter(endDate) ? endDate : eventEnd;

      while (!currentDate.isAfter(lastDate)) {
        eventsPerDay.put(currentDate, eventsPerDay.getOrDefault(currentDate, 0) + 1);
        currentDate = currentDate.plusDays(1);
      }
    }

    return eventsPerDay;
  }

  /**
   * Finds the busiest day (day with most events).
   */
  private static LocalDate findBusiestDay(Map<LocalDate, Integer> eventsPerDay) {
    LocalDate busiest = null;
    int maxEvents = -1;

    for (Map.Entry<LocalDate, Integer> entry : eventsPerDay.entrySet()) {
      if (entry.getValue() > maxEvents) {
        maxEvents = entry.getValue();
        busiest = entry.getKey();
      }
    }

    return busiest;
  }

  /**
   * Finds the least busy day (day with fewest events, but at least 0).
   */
  private static LocalDate findLeastBusyDay(Map<LocalDate, Integer> eventsPerDay) {
    LocalDate leastBusy = null;
    int minEvents = Integer.MAX_VALUE;

    for (Map.Entry<LocalDate, Integer> entry : eventsPerDay.entrySet()) {
      if (entry.getValue() < minEvents) {
        minEvents = entry.getValue();
        leastBusy = entry.getKey();
      }
    }

    return leastBusy;
  }

  /**
   * Calculates the percentage of online vs offline events.
   * An event is online if its location (case-insensitive) contains "online".
   *
   * @return array with [onlinePercentage, offlinePercentage]
   */
  private static double[] calculateOnlineOfflinePercentages(List<InterfaceEvent> events) {
    if (events.isEmpty()) {
      return new double[]{0.0, 0.0};
    }

    int onlineCount = 0;
    for (InterfaceEvent event : events) {
      String location = event.getLocation();
      if (location != null && location.toLowerCase().contains("online")) {
        onlineCount++;
      }
    }

    double onlinePercentage = (double) onlineCount / events.size() * 100.0;
    double offlinePercentage = 100.0 - onlinePercentage;

    return new double[]{onlinePercentage, offlinePercentage};
  }
}

