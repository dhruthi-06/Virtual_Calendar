package calendar.controller;

import calendar.controller.commands.InterfaceCommand;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.InterfaceCalendarView;
import java.util.Scanner;

/**
 * Controller implementation for the calendar application.
 * Handles both interactive and headless modes.
 */
public class CalendarController implements InterfaceCalendarController {
  private final InterfaceCalendarSystem model;
  private final InterfaceCalendarView view;
  private final Readable input;
  private final CommandParser parser;

  private String currentCalendarName;
  private boolean isRunning;

  /**
   * Creates a new CalendarController object to manage interaction between
   * the model, view, and user input.
   *
   * @param model the calendar system model used to store and manage events
   * @param view the view used to display output and messages to the user
   * @param input the input source that provides user commands
   */
  public CalendarController(InterfaceCalendarSystem model,
                            InterfaceCalendarView view, Readable input) {
    if (model == null) {
      throw new IllegalArgumentException("Model cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }
    if (input == null) {
      throw new IllegalArgumentException("Input cannot be null");
    }

    this.model = model;
    this.view = view;
    this.input = input;
    this.parser = new CommandParser();
    this.currentCalendarName = null;
    this.isRunning = false;
  }

  @Override
  public void run() {
    isRunning = true;
    Scanner scanner = new Scanner(input);

    while (isRunning && scanner.hasNextLine()) {
      String command = scanner.nextLine().trim();

      if (command.isEmpty()) {
        continue;
      }

      if (command.equalsIgnoreCase("exit")) {
        view.displayMessage("Exiting calendar application.");
        isRunning = false;
        break;
      }

      try {
        executeCommand(command);
      } catch (IllegalArgumentException e) {
        view.displayError("Invalid command: " + e.getMessage());
      } catch (IllegalStateException e) {
        view.displayError(e.getMessage());
      } catch (Exception e) {
        view.displayError("Error: " + e.getMessage());
      }
    }

    if (isRunning) {
      view.displayError("Error: File ended without 'exit' command");
    }
  }

  /**
   * Executes a parsed command.
   * Updates the current calendar context if needed.
   *
   * @param command the command string to execute
   */
  private void executeCommand(String command) {
    InterfaceCommand cmd = parser.parse(command);

    cmd.execute(model, view, currentCalendarName);

    String newCalendar = cmd.getNewCalendarContext();
    if (newCalendar != null) {
      this.currentCalendarName = newCalendar;
    }

    if (cmd.isExitCommand()) {
      isRunning = false;
    }
  }

  /**
   * Gets the current calendar name in use.
   *
   * @return the current calendar name, or null if none
   */
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  /**
   * Sets the current calendar name.
   * Useful for testing.
   *
   * @param calendarName the calendar name to set as current
   */
  public void setCurrentCalendarName(String calendarName) {
    if (calendarName != null && !model.calendarExists(calendarName)) {
      throw new IllegalArgumentException("Calendar does not exist: " + calendarName);
    }
    this.currentCalendarName = calendarName;
  }

  /**
   * Checks if the controller is currently running.
   *
   * @return true if running, false otherwise
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Stops the controller from running.
   * Useful for testing.
   */
  public void stop() {
    isRunning = false;
  }
}