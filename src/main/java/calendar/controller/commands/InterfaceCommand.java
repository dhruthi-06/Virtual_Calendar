package calendar.controller.commands;

import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;

/**
 * Interface for all commands.
 */
public interface InterfaceCommand {
  /**
   * Executes the command.
   *
   * @param model the calendar system model
   * @param view the view to display results
   * @param currentCalendar the currently active calendar (null if none)
   */
  void execute(InterfaceCalendarSystem model, InterfaceCalendarView view, String currentCalendar);

  /**
   * Returns the calendar name if this command changes the active calendar.
   *
   * @return new calendar name, or null if calendar doesn't change
   */
  String getNewCalendarContext();

  /**
   * Checks if this command terminates the application.
   *
   * @return true if this is an exit command
   */
  boolean isExitCommand();
}