package calendar.view.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages color assignments and color calculations for calendars.
 */
public class CalendarColorManager {
  private final Map<String, Color> calendarColors;
  private int colorIndex = 0;

  private static final Color[] AVAILABLE_COLORS = {
      new Color(66, 133, 244),
      new Color(234, 67, 53),
      new Color(251, 188, 5),
      new Color(52, 168, 83),
      new Color(156, 39, 176),
      new Color(255, 109, 0),
  };

  /**
   * Constructs a new CalendarColorManager.
   */
  public CalendarColorManager() {
    this.calendarColors = new HashMap<>();
  }

  /**
   * Gets or assigns a color for the given calendar name.
   *
   * @param calendarName the calendar name
   * @return the color for this calendar
   */
  public Color getCalendarColor(String calendarName) {
    if (calendarName == null) {
      return AVAILABLE_COLORS[0];
    }

    if (!calendarColors.containsKey(calendarName)) {
      calendarColors.put(calendarName, AVAILABLE_COLORS[colorIndex % AVAILABLE_COLORS.length]);
      colorIndex++;
    }

    return calendarColors.get(calendarName);
  }

  /**
   * Calculates the background color for a day button.
   *
   * @param calendarColor the base calendar color
   * @param isToday whether this is today's date
   * @param isSunday whether this is a Sunday
   * @return the calculated color
   */
  public Color calculateDayButtonColor(Color calendarColor, boolean isToday, boolean isSunday) {
    if (isToday) {
      return brighten(calendarColor, 160);
    } else if (isSunday) {
      return new Color(245, 245, 245);
    } else {
      return Color.WHITE;
    }
  }

  /**
   * Calculates the hover color for a day button.
   *
   * @param calendarColor the base calendar color
   * @return the hover color
   */
  public Color calculateHoverColor(Color calendarColor) {
    return brighten(calendarColor, 200);
  }

  /**
   * Calculates the border color for a selected day.
   *
   * @param calendarColor the base calendar color
   * @return the darker border color
   */
  public Color calculateBorderColor(Color calendarColor) {
    return darken(calendarColor, 40);
  }

  /**
   * Brightens a color by adding a value to each RGB component.
   *
   * @param color the original color
   * @param amount the amount to brighten (0-255)
   * @return the brightened color
   */
  private Color brighten(Color color, int amount) {
    return new Color(
        Math.min(255, color.getRed() + amount),
        Math.min(255, color.getGreen() + amount),
        Math.min(255, color.getBlue() + amount)
    );
  }

  /**
   * Darkens a color by subtracting a value from each RGB component.
   *
   * @param color the original color
   * @param amount the amount to darken (0-255)
   * @return the darkened color
   */
  private Color darken(Color color, int amount) {
    return new Color(
        Math.max(0, color.getRed() - amount),
        Math.max(0, color.getGreen() - amount),
        Math.max(0, color.getBlue() - amount)
    );
  }
}