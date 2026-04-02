package calendar.model;

import java.time.LocalDateTime;

/**
 * Parameter object for single event copy requests.
 */
public class EventCopyRequest {
  private final String eventName;
  private final LocalDateTime sourceDateTime;
  private final String targetCalendar;
  private final LocalDateTime targetDateTime;

  /**
   * Private constructor.
   */
  private EventCopyRequest(Builder builder) {
    this.eventName = builder.eventName;
    this.sourceDateTime = builder.sourceDateTime;
    this.targetCalendar = builder.targetCalendar;
    this.targetDateTime = builder.targetDateTime;
  }

  public String getEventName() {
    return eventName;
  }

  public LocalDateTime getSourceDateTime() {
    return sourceDateTime;
  }

  public String getTargetCalendar() {
    return targetCalendar;
  }

  public LocalDateTime getTargetDateTime() {
    return targetDateTime;
  }

  /**
   * Builder for EventCopyRequest.
   */
  public static class Builder {
    private final String eventName;
    private LocalDateTime sourceDateTime;
    private String targetCalendar;
    private LocalDateTime targetDateTime;

    /**
     * Constructor with event name (required).
     *
     * @param eventName the event name to copy
     */
    public Builder(String eventName) {
      this.eventName = eventName;
    }

    /**
     * Sets the source date/time.
     *
     * @param sourceDateTime the source date/time
     * @return this builder
     */
    public Builder sourceDateTime(LocalDateTime sourceDateTime) {
      this.sourceDateTime = sourceDateTime;
      return this;
    }

    /**
     * Sets the target calendar.
     *
     * @param targetCalendar the target calendar name
     * @return this builder
     */
    public Builder targetCalendar(String targetCalendar) {
      this.targetCalendar = targetCalendar;
      return this;
    }

    /**
     * Sets the target date/time.
     *
     * @param targetDateTime the target date/time
     * @return this builder
     */
    public Builder targetDateTime(LocalDateTime targetDateTime) {
      this.targetDateTime = targetDateTime;
      return this;
    }

    /**
     * Builds the EventCopyRequest.
     *
     * @return a new EventCopyRequest instance
     */
    public EventCopyRequest build() {
      return new EventCopyRequest(this);
    }
  }
}