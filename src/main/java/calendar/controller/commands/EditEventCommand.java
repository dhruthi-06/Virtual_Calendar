package calendar.controller.commands;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.util.DateTimeParser;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDateTime;

/**
 * Command to edit an event in a calendar.
 */
public class EditEventCommand extends BaseCommand {

  private final EditMode mode;
  private final String property;
  private final String subject;
  private final LocalDateTime startDateTime;
  private final String newValue;

  /**
   * Enum for edit modes.
   */
  public enum EditMode {
    SINGLE,
    FROM_DATE,
    ENTIRE_SERIES
  }

  /**
   * Builder class for EditEventCommand.
   */
  public static class Builder {
    private final EditMode mode;
    private final String property;
    private final String subject;
    private LocalDateTime startDateTime;
    private String newValue;

    /**
     * Constructs a Builder with required parameters.
     *
     * @param mode the edit mode
     * @param property the property to edit
     * @param subject the event subject
     */
    public Builder(EditMode mode, String property, String subject) {
      if (mode == null) {
        throw new IllegalArgumentException("Edit mode cannot be null");
      }
      if (property == null || property.trim().isEmpty()) {
        throw new IllegalArgumentException("Property cannot be null or empty");
      }
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }

      this.mode = mode;
      this.property = property;
      this.subject = subject;
    }

    /**
     * Sets the start date and time.
     *
     * @param startDateTime the start date and time
     * @return this builder
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the new value for the property.
     *
     * @param newValue the new value
     * @return this builder
     */
    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    /**
     * Builds the EditEventCommand.
     *
     * @return a new EditEventCommand instance
     */
    public EditEventCommand build() {
      if (startDateTime == null) {
        throw new IllegalStateException("Start date/time must be set");
      }
      if (newValue == null) {
        throw new IllegalStateException("New value must be set");
      }
      return new EditEventCommand(this);
    }
  }

  private EditEventCommand(Builder builder) {
    this.mode = builder.mode;
    this.property = builder.property;
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.newValue = builder.newValue;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      InterfaceCalendar calendar = model.getCalendar(currentCalendar);

      if (!isValidProperty(property)) {
        throw new IllegalArgumentException("Invalid property: " + property
            + ". Valid properties: subject, start, end, description, location, status");
      }

      switch (mode) {
        case SINGLE:
          calendar.editEvent(subject, startDateTime, property, newValue);
          view.displayMessage("Event '" + subject + "' updated successfully.");
          break;

        case FROM_DATE:
          calendar.editEventsFromDate(subject, startDateTime, property, newValue);
          view.displayMessage("Events in series starting from "
              + DateTimeParser.formatDateTime(startDateTime)
              + " updated successfully.");
          break;

        case ENTIRE_SERIES:
          calendar.editEntireSeries(subject, startDateTime, property, newValue);
          view.displayMessage("Entire event series updated successfully.");
          break;

        default:
          throw new IllegalStateException("Unknown edit mode: " + mode);
      }

    } catch (IllegalArgumentException e) {
      view.displayError("Failed to edit event: " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Error editing event: " + e.getMessage());
    }
  }

  private boolean isValidProperty(String property) {
    String prop = property.toLowerCase();
    return prop.equals("subject")
        || prop.equals("start")
        || prop.equals("end")
        || prop.equals("description")
        || prop.equals("location")
        || prop.equals("status");
  }

  /**
   * Gets the edit mode.
   *
   * @return the edit mode
   */
  public EditMode getMode() {
    return mode;
  }

  /**
   * Gets the property being edited.
   *
   * @return the property name
   */
  public String getProperty() {
    return property;
  }

  /**
   * Gets the new value for the property.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValue;
  }
}