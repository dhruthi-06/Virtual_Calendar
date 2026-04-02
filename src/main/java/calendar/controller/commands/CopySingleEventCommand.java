package calendar.controller.commands;

import calendar.model.EventCopyRequest;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDateTime;

/**
 * Command to copy a single event from one calendar to another.
 * Uses EventCopyRequest parameter object with Builder.
 */
public class CopySingleEventCommand extends BaseCommand {

  private final EventCopyRequest request;

  /**
   * Builder class for CopySingleEventCommand.
   */
  public static class Builder {
    private final String eventName;
    private LocalDateTime sourceDateTime;
    private String targetCalendar;
    private LocalDateTime targetDateTime;

    /**
     * Constructs a Builder with the event name.
     *
     * @param eventName the name of the event to copy
     */
    public Builder(String eventName) {
      this.eventName = eventName;
    }

    /**
     * Sets the source date and time.
     *
     * @param sourceDateTime the source date and time
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
     * Sets the target date and time.
     *
     * @param targetDateTime the target date and time
     * @return this builder
     */
    public Builder targetDateTime(LocalDateTime targetDateTime) {
      this.targetDateTime = targetDateTime;
      return this;
    }

    /**
     * Builds the CopySingleEventCommand.
     *
     * @return a new CopySingleEventCommand instance
     */
    public CopySingleEventCommand build() {
      EventCopyRequest request = new EventCopyRequest.Builder(eventName)
          .sourceDateTime(sourceDateTime)
          .targetCalendar(targetCalendar)
          .targetDateTime(targetDateTime)
          .build();
      return new CopySingleEventCommand(request);
    }
  }

  private CopySingleEventCommand(EventCopyRequest request) {
    this.request = request;
  }

  /**
   * Constructs a CopySingleEventCommand with all parameters.
   * Convenience constructor that creates EventCopyRequest internally.
   *
   * @param eventName the name of the event to copy
   * @param sourceDateTime the source date and time
   * @param targetCalendar the target calendar name
   * @param targetDateTime the target date and time
   */
  public CopySingleEventCommand(String eventName, LocalDateTime sourceDateTime,
                                String targetCalendar, LocalDateTime targetDateTime) {
    this.request = new EventCopyRequest.Builder(eventName)
        .sourceDateTime(sourceDateTime)
        .targetCalendar(targetCalendar)
        .targetDateTime(targetDateTime)
        .build();
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      model.copyEvent(currentCalendar, request);
      view.displayMessage("Event copied to " + request.getTargetCalendar());
    } catch (Exception e) {
      view.displayError("Failed to copy event: " + e.getMessage());
    }
  }
}