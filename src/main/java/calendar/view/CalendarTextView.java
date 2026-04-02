package calendar.view;

import calendar.model.InterfaceEvent;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Text-based implementation of the calendar view.
 * Outputs information to a PrintStream (typically System.out).
 * Formats output for readability in both interactive and headless modes.
 */
public class CalendarTextView implements InterfaceCalendarView {

  private final PrintStream out;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm");

  /**
   * Creates a view that outputs to System.out.
   */
  public CalendarTextView() {
    this(System.out);
  }

  /**
   * Creates a view that outputs to the specified PrintStream.
   *
   * @param out the output stream
   * @throws IllegalArgumentException if out is null
   */
  public CalendarTextView(PrintStream out) {
    if (out == null) {
      throw new IllegalArgumentException("Output stream cannot be null");
    }
    this.out = out;
  }

  @Override
  public void displayMessage(String message) {
    if (message != null && !message.isEmpty()) {
      out.println(message);
    }
  }

  @Override
  public void displayError(String error) {
    if (error != null && !error.isEmpty()) {
      out.println("ERROR: " + error);
    }
  }

  @Override
  public void displayEvents(List<InterfaceEvent> events) {
    if (events == null || events.isEmpty()) {
      out.println("No events found.");
      return;
    }

    out.println("Events:");
    for (InterfaceEvent event : events) {
      out.println("- " + formatEventDetailed(event));
    }
  }

  @Override
  public void displayEventsForDate(List<InterfaceEvent> events, String date) {
    if (events == null || events.isEmpty()) {
      out.println("No events scheduled on " + date);
      return;
    }

    out.println("Events on " + date + ":");

    for (InterfaceEvent event : events) {
      StringBuilder sb = new StringBuilder();
      sb.append("- ").append(event.getSubject());

      if (!event.isAllDay()) {
        sb.append(" from ")
            .append(event.getStart().toLocalTime())
            .append(" to ")
            .append(event.getEnd().toLocalTime());
      } else {
        sb.append(" (All Day)");
      }

      if (!event.getLocation().isEmpty()) {
        sb.append(" at ").append(event.getLocation());
      }

      if (event.isPartOfSeries()) {
        sb.append(" [Recurring]");
      }

      out.println(sb.toString());
    }
  }

  @Override
  public void displayEventsInRange(List<InterfaceEvent> events, String startDate,
                                   String endDate) {
    if (events == null || events.isEmpty()) {
      out.println("No events found between " + startDate + " and " + endDate);
      return;
    }

    out.println("Events between " + startDate + " and " + endDate + ":");

    for (InterfaceEvent event : events) {
      StringBuilder sb = new StringBuilder();
      sb.append("- ").append(event.getSubject())
          .append(" starting on ")
          .append(event.getStart().format(DATETIME_FORMATTER))
          .append(", ending on ")
          .append(event.getEnd().format(DATETIME_FORMATTER));

      if (!event.getLocation().isEmpty()) {
        sb.append(" at ").append(event.getLocation());
      }

      if (!event.getDescription().isEmpty()) {
        sb.append(" - ").append(event.getDescription());
      }

      if (event.isPartOfSeries()) {
        sb.append(" [Recurring]");
      }

      if (!event.isPublic()) {
        sb.append(" [Private]");
      }

      out.println(sb.toString());
    }
  }

  @Override
  public void displayStatus(boolean isBusy, String dateTime) {
    if (isBusy) {
      out.println("Status at " + dateTime + ": BUSY");
    } else {
      out.println("Status at " + dateTime + ": AVAILABLE");
    }
  }

  @Override
  public void displayWelcome() {
    out.println("Welcome to Calendar Application!");
    out.println("Type 'help' for available commands or 'exit' to quit.");
    out.println();
  }

  @Override
  public void displayHelp() {
    out.println();
    out.println("AVAILABLE COMMANDS");
    out.println();
    out.println("CALENDAR MANAGEMENT:");
    out.println("  create calendar --name <name> --timezone <timezone>");
    out.println("  edit calendar --name <name> --property <property> <value>");
    out.println("  use calendar --name <name>");
    out.println();
    out.println("EVENT CREATION:");
    out.println("  create event <subject> from <YYYY-MM-DDTHH:mm> to <YYYY-MM-DDTHH:mm>");
    out.println("  create event <subject> on <YYYY-MM-DD>");
    out.println("  create event <subject> from <dateTime> to <dateTime> "
        + "repeats <weekdays> for <N> times");
    out.println("  create event <subject> from <dateTime> to <dateTime> "
        + "repeats <weekdays> until <date>");
    out.println();
    out.println("EVENT EDITING:");
    out.println("  edit event <property> <subject> from <dateTime> with <newValue>");
    out.println("  edit events <property> <subject> from <dateTime> with <newValue>");
    out.println("  edit series <property> <subject> from <dateTime> with <newValue>");
    out.println();
    out.println("EVENT COPYING:");
    out.println("  copy event <name> on <dateTime> --target <calendar> to <dateTime>");
    out.println("  copy events on <date> --target <calendar> to <date>");
    out.println("  copy events between <date> and <date> --target <calendar> to <date>");
    out.println();
    out.println("QUERIES:");
    out.println("  print events on <YYYY-MM-DD>");
    out.println("  print events from <dateTime> to <dateTime>");
    out.println("  show status on <YYYY-MM-DDTHH:mm>");
    out.println("  show calendar dashboard from <YYYY-MM-DD> to <YYYY-MM-DD>");
    out.println();
    out.println("EXPORT:");
    out.println("  export cal <filename.csv>");
    out.println("  export cal <filename.ical>");
    out.println();
    out.println("OTHER:");
    out.println("  help");
    out.println("  exit");
    out.println();
    out.println("NOTES:");
    out.println("  Weekday Codes: M=Monday, T=Tuesday, W=Wednesday, "
        + "R=Thursday, F=Friday, S=Saturday, U=Sunday");
    out.println("  Multi-word subjects must be enclosed in double quotes");
    out.println("  Timezones use IANA format "
        + "(e.g., America/New_York, Europe/Paris)");
    out.println("  Properties: subject, start, end, description, location, status");
    out.println();
  }

  @Override
  public void displayCalendarList(List<String> calendarNames) {
    if (calendarNames == null || calendarNames.isEmpty()) {
      out.println("No calendars found.");
      return;
    }

    out.println("Available Calendars:");
    for (String name : calendarNames) {
      out.println("- " + name);
    }
  }

  @Override
  public void displayCalendarInfo(String name, String timezone, int eventCount) {
    out.println("Calendar: " + name);
    out.println("Timezone: " + timezone);
    out.println("Events: " + eventCount);
  }

  @Override
  public void displayDashboard(calendar.util.CalendarAnalytics.AnalyticsResult analytics,
                               java.time.LocalDate startDate, java.time.LocalDate endDate) {
    out.println();
    out.println("=== Calendar Dashboard ===");
    out.println("Date Range: " + startDate.format(DATE_FORMATTER) + " to "
        + endDate.format(DATE_FORMATTER));
    out.println();


    out.println("Total number of events: " + analytics.getTotalEvents());
    out.println();


    out.println("Total number of events by subject:");
    var eventsBySubject = analytics.getEventsBySubject();
    if (eventsBySubject.isEmpty()) {
      out.println("  (none)");
    } else {
      eventsBySubject.entrySet().stream()
          .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
          .forEach(entry -> out.println("  " + entry.getKey() + ": " + entry.getValue()));
    }
    out.println();


    out.println("Total number of events by weekday:");
    var eventsByWeekday = analytics.getEventsByWeekday();
    String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
        "Sunday"};
    for (String day : weekdays) {
      int count = eventsByWeekday.getOrDefault(day, 0);
      out.println("  " + day + ": " + count);
    }
    out.println();


    out.println("Total number of events by week:");
    var eventsByWeek = analytics.getEventsByWeek();
    if (eventsByWeek.isEmpty()) {
      out.println("  (none)");
    } else {
      eventsByWeek.entrySet().stream()
          .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
          .forEach(entry -> out.println("  Week " + entry.getKey() + ": " + entry.getValue()));
    }
    out.println();


    out.println("Total number of events by month:");
    var eventsByMonth = analytics.getEventsByMonth();
    if (eventsByMonth.isEmpty()) {
      out.println("  (none)");
    } else {
      String[] monthNames = {"January", "February", "March", "April", "May", "June",
          "July", "August", "September", "October", "November", "December"};
      eventsByMonth.entrySet().stream()
          .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
          .forEach(entry -> {
            String monthName = monthNames[entry.getKey() - 1];
            out.println("  " + monthName + ": " + entry.getValue());
          });
    }
    out.println();


    out.println("Average number of events per day: "
        + String.format("%.2f", analytics.getAverageEventsPerDay()));
    out.println();


    var busiestDay = analytics.getBusiestDay();
    var leastBusyDay = analytics.getLeastBusyDay();
    if (busiestDay != null) {
      out.println("Busiest day: " + busiestDay.format(DATE_FORMATTER));
    } else {
      out.println("Busiest day: (none)");
    }
    if (leastBusyDay != null) {
      out.println("Least busy day: " + leastBusyDay.format(DATE_FORMATTER));
    } else {
      out.println("Least busy day: (none)");
    }
    out.println();


    out.println("Percentage of events:");
    out.println("  Online: " + String.format("%.2f", analytics.getOnlinePercentage()) + "%");
    out.println("  Not online: " + String.format("%.2f", analytics.getOfflinePercentage()) + "%");
    out.println();
  }

  private String formatEventDetailed(InterfaceEvent event) {
    StringBuilder sb = new StringBuilder();

    sb.append(event.getSubject())
        .append(" starting on ")
        .append(event.getStart().format(DATETIME_FORMATTER))
        .append(", ending on ")
        .append(event.getEnd().format(DATETIME_FORMATTER));

    if (!event.getLocation().isEmpty()) {
      sb.append(" at ").append(event.getLocation());
    }

    if (!event.getDescription().isEmpty()) {
      sb.append(" - ").append(event.getDescription());
    }

    if (event.isPartOfSeries()) {
      sb.append(" [Recurring]");
    }

    if (!event.isPublic()) {
      sb.append(" [Private]");
    }

    return sb.toString();
  }

  /**
   * Gets the output stream used by this view.
   *
   * @return the output stream
   */
  public PrintStream getOutputStream() {
    return out;
  }
}