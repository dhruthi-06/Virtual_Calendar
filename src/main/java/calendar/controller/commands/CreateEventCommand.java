package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.model.RecurringEventCreationRequest;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDateTime;

/**
 * Command to create a new event in a calendar.
 *  Uses RecurringEventCreationRequest for recurring events.
 */
public class CreateEventCommand extends BaseCommand {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final RecurringEventCreationRequest recurringRequest;

  /**
   * Builder class for CreateEventCommand.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private String repeatsPattern = null;
    private Integer repeatCount = null;
    private LocalDateTime repeatUntil = null;

    /**
     * Constructs a Builder with required parameters.
     *
     * @param subject the event subject
     * @param start the start date and time
     * @param end the end date and time
     */
    public Builder(String subject, LocalDateTime start, LocalDateTime end) {
      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    /**
     * Sets the repeat pattern and count.
     *
     * @param pattern the repeat pattern
     * @param count the number of repetitions
     * @return this builder
     */
    public Builder repeats(String pattern, int count) {
      this.repeatsPattern = pattern;
      this.repeatCount = count;
      return this;
    }

    /**
     * Sets the repeat pattern and end date.
     *
     * @param pattern the repeat pattern
     * @param until the end date for repetitions
     * @return this builder
     */
    public Builder repeatsUntil(String pattern, LocalDateTime until) {
      this.repeatsPattern = pattern;
      this.repeatUntil = until;
      return this;
    }

    /**
     * Builds the CreateEventCommand.
     *
     * @return a new CreateEventCommand instance
     */
    public CreateEventCommand build() {
      return new CreateEventCommand(this);
    }
  }

  private CreateEventCommand(Builder builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;

    if (builder.repeatsPattern != null) {
      RecurringEventCreationRequest.Builder reqBuilder =
          new RecurringEventCreationRequest.Builder(
              builder.subject, builder.start, builder.end, builder.repeatsPattern);

      if (builder.repeatCount != null) {
        reqBuilder.repeatCount(builder.repeatCount);
      } else if (builder.repeatUntil != null) {
        reqBuilder.repeatUntil(builder.repeatUntil);
      }

      this.recurringRequest = reqBuilder.build();
    } else {
      this.recurringRequest = null;
    }
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      if (recurringRequest == null) {
        model.createEvent(currentCalendar, subject, start, end);
        view.displayMessage("Event created: " + subject);
      } else {
        model.createRecurringEvent(currentCalendar, recurringRequest);
        view.displayMessage("Recurring event created: " + subject);
      }
    } catch (Exception e) {
      view.displayError("Failed to create event: " + e.getMessage());
    }
  }
}