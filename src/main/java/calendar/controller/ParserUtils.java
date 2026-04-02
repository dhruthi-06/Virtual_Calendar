package calendar.controller;

import calendar.util.DateTimeParser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods shared across different parsers.
 */
public class ParserUtils {

  private ParserUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Helper class to hold subject and remainder after parsing.
   */
  public static class SubjectAndRemainder {
    public final String subject;
    public final String remainder;

    /**
     * Constructs a SubjectAndRemainder.
     *
     * @param subject the extracted subject
     * @param remainder the remaining text after subject
     */
    public SubjectAndRemainder(String subject, String remainder) {
      this.subject = subject;
      this.remainder = remainder;
    }
  }

  /**
   * Helper class to hold recurrence details.
   */
  public static class RecurrenceDetails {
    public final String weekdays;
    public final Integer count;
    public final LocalDateTime until;

    /**
     * Constructs a RecurrenceDetails.
     *
     * @param weekdays the weekday pattern
     * @param count the number of repetitions
     * @param until the end date for repetitions
     */
    public RecurrenceDetails(String weekdays, Integer count, LocalDateTime until) {
      this.weekdays = weekdays;
      this.count = count;
      this.until = until;
    }
  }

  /**
   * Extracts subject from command body, handling quoted and unquoted subjects.
   *
   * @param commandBody the command body to parse
   * @return the subject and remainder
   */
  public static SubjectAndRemainder extractSubject(String commandBody) {
    String subject;
    String remainder;

    if (commandBody.startsWith("\"")) {
      int endQuote = commandBody.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in subject");
      }
      subject = commandBody.substring(1, endQuote);
      remainder = commandBody.substring(endQuote + 1).trim();
    } else {
      int fromIndex = commandBody.indexOf(" from ");
      int onIndex = commandBody.indexOf(" on ");

      int splitIndex = -1;
      if (fromIndex != -1 && onIndex != -1) {
        splitIndex = Math.min(fromIndex, onIndex);
      } else if (fromIndex != -1) {
        splitIndex = fromIndex;
      } else if (onIndex != -1) {
        splitIndex = onIndex;
      }

      if (splitIndex == -1) {
        throw new IllegalArgumentException("Invalid command format");
      }

      subject = commandBody.substring(0, splitIndex).trim();
      remainder = commandBody.substring(splitIndex).trim();
    }

    return new SubjectAndRemainder(subject, remainder);
  }

  /**
   * Extracts event name for copy command, handling quoted names.
   *
   * @param commandBody the command body to parse
   * @return the event name and remainder
   */
  public static SubjectAndRemainder extractEventNameForCopy(String commandBody) {
    String eventName;
    String remainder;

    if (commandBody.startsWith("\"")) {
      int endQuote = commandBody.indexOf("\"", 1);
      if (endQuote == -1) {
        throw new IllegalArgumentException("Unclosed quote in event name");
      }
      eventName = commandBody.substring(1, endQuote);
      remainder = commandBody.substring(endQuote + 1).trim();
    } else {
      int onIndex = commandBody.indexOf(" on ");
      if (onIndex == -1) {
        throw new IllegalArgumentException("Missing 'on' in copy command");
      }
      eventName = commandBody.substring(0, onIndex).trim();
      remainder = commandBody.substring(onIndex).trim();
    }

    return new SubjectAndRemainder(eventName, remainder);
  }

  /**
   * Extracts recurrence details from repeat pattern.
   *
   * @param pattern the repeat pattern to parse
   * @return the recurrence details
   */
  public static RecurrenceDetails extractRecurrenceDetails(String pattern) {
    String[] forSplit = pattern.split(" for ");
    String[] untilSplit = pattern.split(" until ");

    if (forSplit.length == 2) {
      String weekdays = forSplit[0].trim();
      String countStr = forSplit[1].replace(" times", "").replace(" time", "").trim();
      int count = Integer.parseInt(countStr);
      return new RecurrenceDetails(weekdays, count, null);
    } else if (untilSplit.length == 2) {
      String weekdays = untilSplit[0].trim();
      LocalDateTime until = DateTimeParser.parseDate(untilSplit[1].trim()).atTime(23, 59);
      return new RecurrenceDetails(weekdays, null, until);
    } else {
      throw new IllegalArgumentException("Invalid recurring pattern");
    }
  }

  /**
   * Tokenizes command while respecting quoted strings.
   *
   * @param command the command to tokenize
   * @return array of tokens
   */
  public static String[] tokenize(String command) {
    List<String> tokens = new ArrayList<>();
    Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\S+");
    Matcher matcher = pattern.matcher(command);

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else {
        tokens.add(matcher.group());
      }
    }

    return tokens.toArray(new String[0]);
  }

  /**
   * Removes surrounding quotes from a string if present.
   *
   * @param str the string to process
   * @return the string without quotes
   */
  public static String removeQuotes(String str) {
    if (str == null) {
      return null;
    }
    if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}