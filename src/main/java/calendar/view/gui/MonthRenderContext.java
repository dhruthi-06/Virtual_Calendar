package calendar.view.gui;

import calendar.model.InterfaceEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Parameter object for calendar month rendering.
 * Reduces parameter list in CalendarGridRenderer.
 */
public class MonthRenderContext {
  private final YearMonth month;
  private final LocalDate selectedDate;
  private final String calendarName;
  private final DayClickListener clickListener;
  private final EventProvider eventProvider;

  /**
   * Private constructor - use Builder.
   *
   * @param builder the builder instance
   */
  private MonthRenderContext(Builder builder) {
    this.month = builder.month;
    this.selectedDate = builder.selectedDate;
    this.calendarName = builder.calendarName;
    this.clickListener = builder.clickListener;
    this.eventProvider = builder.eventProvider;
  }

  /**
   * Gets the month to render.
   *
   * @return the YearMonth
   */
  public YearMonth getMonth() {
    return month;
  }

  /**
   * Gets the selected date.
   *
   * @return the selected LocalDate
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Gets the calendar name.
   *
   * @return the calendar name
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Gets the day click listener.
   *
   * @return the DayClickListener
   */
  public DayClickListener getClickListener() {
    return clickListener;
  }

  /**
   * Gets the event provider.
   *
   * @return the EventProvider
   */
  public EventProvider getEventProvider() {
    return eventProvider;
  }

  /**
   * Builder for MonthRenderContext.
   */
  public static class Builder {
    private final YearMonth month;
    private final LocalDate selectedDate;
    private final String calendarName;
    private DayClickListener clickListener;
    private EventProvider eventProvider;

    /**
     * Constructs a Builder with required fields.
     *
     * @param month the month to render
     * @param selectedDate the selected date
     * @param calendarName the calendar name
     */
    public Builder(YearMonth month, LocalDate selectedDate, String calendarName) {
      this.month = month;
      this.selectedDate = selectedDate;
      this.calendarName = calendarName;
    }

    /**
     * Sets the click listener.
     *
     * @param clickListener the listener
     * @return this builder
     */
    public Builder clickListener(DayClickListener clickListener) {
      this.clickListener = clickListener;
      return this;
    }

    /**
     * Sets the event provider.
     *
     * @param eventProvider the provider
     * @return this builder
     */
    public Builder eventProvider(EventProvider eventProvider) {
      this.eventProvider = eventProvider;
      return this;
    }

    /**
     * Builds the context.
     *
     * @return new MonthRenderContext
     */
    public MonthRenderContext build() {
      return new MonthRenderContext(this);
    }
  }

  /**
   * Callback interface for day clicks.
   */
  public interface DayClickListener {
    /**
     * Called when a day is clicked.
     *
     * @param date the date that was clicked
     */
    void onDayClicked(LocalDate date);
  }

  /**
   * Callback interface for getting events.
   */
  public interface EventProvider {
    /**
     * Gets events for a specific date.
     *
     * @param date the date to get events for
     * @return list of events on that date
     */
    List<InterfaceEvent> getEventsForDate(LocalDate date);
  }
}