import calendar.controller.CalendarController;
import calendar.controller.CalendarGuiController;
import calendar.controller.InterfaceCalendarController;
import calendar.model.CalendarSystem;
import calendar.model.InterfaceCalendarSystem;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import calendar.view.gui.CalendarGuiView;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.SwingUtilities;

/**
 * The CalendarRunner class is the main entry point for the Calendar Application.
 * It supports three modes of operation:
 * 1. GUI mode (default) - graphical user interface
 * 2. Interactive mode - reads commands from the keyboard
 * 3. Headless mode - executes commands from a file
 */
public class CalendarRunner {
  /**
   * Main entry point for the Calendar application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      runGui();
      return;
    }

    if (args.length >= 2 && args[0].equalsIgnoreCase("--mode")) {
      handleModeCommand(args);
    } else {
      displayError("Error: --mode argument is required");
      printUsage();
    }
  }

  /**
   * Handles the --mode command with appropriate sub-mode.
   */
  private static void handleModeCommand(String[] args) {
    String mode = args[1].toLowerCase();

    switch (mode) {
      case "interactive":
        runInteractive();
        break;
      case "headless":
        handleHeadlessMode(args);
        break;
      default:
        displayError("Error: mode must be either 'interactive' or 'headless'");
        displayError("Provided mode: " + mode);
        printUsage();
    }
  }

  /**
   * Handles headless mode, checking for required file path.
   */
  private static void handleHeadlessMode(String[] args) {
    if (args.length < 3) {
      displayError("Error: headless mode requires a file path");
      printUsage();
      return;
    }
    runHeadless(args[2]);
  }

  /**
   * Launches the GUI mode of the application.
   */
  private static void runGui() {
    SwingUtilities.invokeLater(() -> {
      try {
        InterfaceCalendarSystem model = new CalendarSystem();
        CalendarGuiView view = new CalendarGuiView();
        CalendarGuiController controller = new CalendarGuiController(model, view);

        view.setVisible(true);
        controller.start();
      } catch (Exception e) {
        displayError("Error launching GUI: " + e.getMessage());
        e.printStackTrace();
      }
    });
  }

  /**
   * Launches the interactive text mode of the application.
   */
  private static void runInteractive() {
    InterfaceCalendarSystem model = new CalendarSystem();
    InterfaceCalendarView view = new CalendarTextView();
    InterfaceCalendarController controller = new CalendarController(
        model, view, new InputStreamReader(System.in));
    view.displayWelcome();
    controller.run();
  }

  /**
   * Launches the headless mode of the application.
   *
   * @param filePath path to the script file
   */
  private static void runHeadless(String filePath) {
    InterfaceCalendarSystem model = new CalendarSystem();
    InterfaceCalendarView view = new CalendarTextView();
    try (FileReader fileReader = new FileReader(filePath)) {
      InterfaceCalendarController controller = new CalendarController(model, view, fileReader);
      controller.run();
    } catch (IOException e) {
      displayError("Error reading file: " + e.getMessage());
      displayError("File path: " + filePath);
    } catch (Exception e) {
      displayError("Error running calendar application: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Displays an error message to stderr.
   */
  private static void displayError(String message) {
    System.err.println(message);
  }

  /**
   * Prints usage information.
   * Extracted to separate methods for better readability.
   */
  private static void printUsage() {
    System.err.println();
    printUsageHeader();
    printModes();
    printExamples();
    printNotes();
    System.err.println();
  }

  /**
   * Prints the usage header.
   */
  private static void printUsageHeader() {
    System.err.println("Usage: java CalendarRunner --mode <interactive|headless> [file]");
    System.err.println();
  }

  /**
   * Prints the available modes.
   */
  private static void printModes() {
    System.err.println("Modes:");
    System.err.println("  interactive  - Run in interactive mode, reading commands from keyboard");
    System.err.println("  headless     - Run in headless mode, reading commands from file");
    System.err.println();
  }

  /**
   * Prints usage examples.
   */
  private static void printExamples() {
    System.err.println("Examples:");
    System.err.println("  java CalendarRunner --mode interactive");
    System.err.println("  java CalendarRunner --mode headless commands.txt");
    System.err.println("  java CalendarRunner --mode headless res/commands.txt");
    System.err.println();
  }

  /**
   * Prints additional notes.
   */
  private static void printNotes() {
    System.err.println("Note:");
    System.err.println("  - Arguments are case-insensitive");
    System.err.println("  - In headless mode, the command file must end with 'exit' command");
    System.err.println("  - File paths should be relative to the current directory");
  }
}