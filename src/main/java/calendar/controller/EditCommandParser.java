package calendar.controller;

import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.InterfaceCommand;
import calendar.util.DateTimeParser;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses edit commands for calendars and events.
 */
public class EditCommandParser {

  private final Map<String, EditEventCommand.EditMode> editModeMap;

  /**
   * Constructs an EditCommandParser.
   */
  public EditCommandParser() {
    this.editModeMap = new HashMap<>();
    editModeMap.put("event", EditEventCommand.EditMode.SINGLE);
    editModeMap.put("events", EditEventCommand.EditMode.FROM_DATE);
    editModeMap.put("series", EditEventCommand.EditMode.ENTIRE_SERIES);
  }

  /**
   * Parses an edit command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parse(String[] tokens, String fullCommand) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid edit command");
    }

    String subCommand = tokens[1].toLowerCase();

    if (subCommand.equals("calendar")) {
      return parseEditCalendar(tokens);
    } else if (subCommand.equals("event") || subCommand.equals("events")
        || subCommand.equals("series")) {
      return parseEditEvent(fullCommand, subCommand);
    } else {
      throw new IllegalArgumentException("Invalid edit sub-command: " + subCommand);
    }
  }

  /**
   * Parses an edit calendar command.
   * Format: edit calendar --name n --property property value.
   *
   * @param tokens the command tokens
   * @return the edit calendar command
   */
  private InterfaceCommand parseEditCalendar(String[] tokens) {
    String name = null;
    String property = null;
    String newValue = null;

    for (int i = 2; i < tokens.length; i++) {
      if (tokens[i].equals("--name") && i + 1 < tokens.length) {
        name = tokens[i + 1];
        i++;
      } else if (tokens[i].equals("--property") && i + 2 < tokens.length) {
        property = tokens[i + 1];
        newValue = tokens[i + 2];
        i += 2;
      }
    }

    if (name == null || property == null || newValue == null) {
      throw new IllegalArgumentException(
          "Usage: edit calendar --name <n> --property <property> <value>");
    }

    return new EditCalendarCommand(name, property, newValue);
  }

  /**
   * Parses an edit event command.
   * Delegates to specific parser based on edit mode.
   *
   * @param fullCommand the full command string
   * @param mode the edit mode string
   * @return the edit event command
   */
  private InterfaceCommand parseEditEvent(String fullCommand, String mode) {
    final EditEventCommand.EditMode editMode = parseEditMode(mode);

    String prefix = "edit " + mode + " ";
    String commandBody = fullCommand.substring(prefix.length()).trim();

    int firstSpace = commandBody.indexOf(" ");
    if (firstSpace == -1) {
      throw new IllegalArgumentException("Invalid edit command format. "
          + "Expected: edit <mode> <property> <subject> from <datetime> with <value>");
    }
    final String property = commandBody.substring(0, firstSpace).trim();
    String remainder = commandBody.substring(firstSpace + 1).trim();

    EditEventCommandParts parts = extractEditEventParts(remainder, mode);

    return new EditEventCommand.Builder(editMode, property, parts.subject)
        .startDateTime(parts.startDateTime)
        .newValue(parts.newValue)
        .build();
  }

  /**
   * Extracts the parts of an edit event command.
   * Handles different formats for event versus events or series.
   *
   * @param remainder the remainder of the command string
   * @param mode the edit mode
   * @return the parsed command parts
   */
  private EditEventCommandParts extractEditEventParts(String remainder, String mode) {
    if (!remainder.contains(" from ")) {
      throw new IllegalArgumentException("Missing 'from' in edit command");
    }

    int fromIndex = remainder.indexOf(" from ");
    String subject = remainder.substring(0, fromIndex).trim();
    subject = ParserUtils.removeQuotes(subject);

    String afterFrom = remainder.substring(fromIndex + 6).trim();

    LocalDateTime startDateTime;
    String newValue;

    if (mode.equals("event")) {
      EditEventTimeParts timeParts = parseEventTimeWithOptionalTo(afterFrom);
      startDateTime = timeParts.startDateTime;
      newValue = timeParts.newValue;
    } else {
      EditEventTimeParts timeParts = parseEventTimeSimple(afterFrom);
      startDateTime = timeParts.startDateTime;
      newValue = timeParts.newValue;
    }

    newValue = ParserUtils.removeQuotes(newValue);

    return new EditEventCommandParts(subject, startDateTime, newValue);
  }

  /**
   * Parses time parts for single event edit with optional to clause.
   *
   * @param afterFrom the command string after the from keyword
   * @return the parsed time parts
   */
  private EditEventTimeParts parseEventTimeWithOptionalTo(String afterFrom) {
    if (afterFrom.contains(" to ") && afterFrom.contains(" with ")) {
      String[] toSplit = afterFrom.split(" to ", 2);
      String startStr = toSplit[0].trim();
      LocalDateTime startDateTime = DateTimeParser.parseDateTime(startStr);

      String[] withSplit = toSplit[1].split(" with ", 2);
      String newValue = withSplit[1].trim();

      return new EditEventTimeParts(startDateTime, newValue);
    } else if (afterFrom.contains(" with ")) {
      String[] withSplit = afterFrom.split(" with ", 2);
      LocalDateTime startDateTime = DateTimeParser.parseDateTime(withSplit[0].trim());
      String newValue = withSplit[1].trim();

      return new EditEventTimeParts(startDateTime, newValue);
    } else {
      throw new IllegalArgumentException("Missing 'with' in edit command");
    }
  }

  /**
   * Parses time parts for events or series edit with simpler format.
   *
   * @param afterFrom the command string after the from keyword
   * @return the parsed time parts
   */
  private EditEventTimeParts parseEventTimeSimple(String afterFrom) {
    if (!afterFrom.contains(" with ")) {
      throw new IllegalArgumentException("Missing 'with' in edit command");
    }

    String[] withSplit = afterFrom.split(" with ", 2);
    LocalDateTime startDateTime = DateTimeParser.parseDateTime(withSplit[0].trim());
    String newValue = withSplit[1].trim();

    return new EditEventTimeParts(startDateTime, newValue);
  }

  /**
   * Parses the edit mode from the mode string.
   *
   * @param mode the mode string
   * @return the edit mode enum value
   */
  private EditEventCommand.EditMode parseEditMode(String mode) {
    EditEventCommand.EditMode editMode = editModeMap.get(mode);
    if (editMode == null) {
      throw new IllegalArgumentException("Invalid edit mode: " + mode);
    }
    return editMode;
  }

  /**
   * Helper class to hold parsed edit event command parts.
   */
  private static class EditEventCommandParts {
    final String subject;
    final LocalDateTime startDateTime;
    final String newValue;

    /**
     * Constructs EditEventCommandParts.
     *
     * @param subject the event subject
     * @param startDateTime the event start date and time
     * @param newValue the new value for the property
     */
    EditEventCommandParts(String subject, LocalDateTime startDateTime, String newValue) {
      this.subject = subject;
      this.startDateTime = startDateTime;
      this.newValue = newValue;
    }
  }

  /**
   * Helper class to hold parsed time parts.
   */
  private static class EditEventTimeParts {
    final LocalDateTime startDateTime;
    final String newValue;

    /**
     * Constructs EditEventTimeParts.
     *
     * @param startDateTime the start date and time
     * @param newValue the new value for the property
     */
    EditEventTimeParts(LocalDateTime startDateTime, String newValue) {
      this.startDateTime = startDateTime;
      this.newValue = newValue;
    }
  }
}