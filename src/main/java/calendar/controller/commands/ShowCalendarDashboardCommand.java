package calendar.controller.commands;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.util.CalendarAnalytics;
import calendar.util.CalendarAnalytics.AnalyticsResult;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Command to show calendar analytics dashboard for a date range.
 */
public class ShowCalendarDashboardCommand extends BaseCommand {

  private final LocalDate startDate;
  private final LocalDate endDate;

  /**
   * Constructs a ShowCalendarDashboardCommand.
   *
   * @param startDate the start date (inclusive)
   * @param endDate the end date (inclusive)
   */
  public ShowCalendarDashboardCommand(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @Override
  public void execute(InterfaceCalendarSystem model, InterfaceCalendarView view,
                      String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      InterfaceCalendar calendar = model.getCalendar(currentCalendar);
      
      // Get all events in the date range
      LocalDateTime rangeStart = startDate.atStartOfDay();
      LocalDateTime rangeEnd = endDate.atTime(23, 59, 59);
      var events = calendar.getEventsInRange(rangeStart, rangeEnd);

      // Calculate analytics
      AnalyticsResult analytics = CalendarAnalytics.calculateAnalytics(events, startDate, endDate);

      // Display the dashboard
      view.displayDashboard(analytics, startDate, endDate);
    } catch (Exception e) {
      view.displayError("Error displaying dashboard: " + e.getMessage());
    }
  }

  /**
   * Gets the start date.
   *
   * @return the start date
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  /**
   * Gets the end date.
   *
   * @return the end date
   */
  public LocalDate getEndDate() {
    return endDate;
  }
}

