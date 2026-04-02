package calendar.controller.commands;

/**
 * Abstract base class for commands that require a calendar to be in use.
 * Provides common validation logic to eliminate code duplication.
 */
public abstract class BaseCommand implements InterfaceCommand {

  /**
   * Validates that a calendar is currently in use.
   * Throws IllegalStateException if no calendar is active.
   *
   * @param currentCalendar the name of the current calendar in use
   */
  protected void validateCalendarInUse(String currentCalendar) {
    if (currentCalendar == null) {
      throw new IllegalStateException(
          "No calendar in use. Use 'use calendar --name <name>' first.");
    }
  }

  @Override
  public String getNewCalendarContext() {
    return null;
  }

  @Override
  public boolean isExitCommand() {
    return false;
  }
}