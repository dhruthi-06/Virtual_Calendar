package calendar.view.gui;

import calendar.model.InterfaceEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Renders event cards for display in the events panel.
 */
public class EventCardRenderer {

  /**
   * Private constructor to prevent instantiation.
   */
  private EventCardRenderer() {
  }

  /**
   * Creates a complete event card panel.
   *
   * @param event the event to display
   * @return a JPanel containing the event card
   */
  public static JPanel createEventCard(InterfaceEvent event) {
    JPanel panel = createBasePanel();

    addSubjectLabel(panel, event);
    addTimeLabel(panel, event);
    addLocationLabel(panel, event);
    addDescriptionLabel(panel, event);
    addBadges(panel, event);

    return panel;
  }

  /**
   * Creates the base panel for an event card.
   */
  private static JPanel createBasePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(220, 220, 220)),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    panel.setMaximumSize(new Dimension(280, 140));
    panel.setBackground(Color.WHITE);
    return panel;
  }

  /**
   * Adds the event subject label to the panel.
   */
  private static void addSubjectLabel(JPanel panel, InterfaceEvent event) {
    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("Arial", Font.BOLD, 13));
    subjectLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    panel.add(subjectLabel);
    panel.add(Box.createVerticalStrut(5));
  }

  /**
   * Adds the event time label to the panel.
   */
  private static void addTimeLabel(JPanel panel, InterfaceEvent event) {
    String timeText = event.isAllDay() ? "All Day" :
        event.getStart().toLocalTime() + " - " + event.getEnd().toLocalTime();
    JLabel timeLabel = new JLabel(timeText);
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    timeLabel.setForeground(new Color(90, 90, 90));
    timeLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
    panel.add(timeLabel);
  }

  /**
   * Adds the event location label if present.
   */
  private static void addLocationLabel(JPanel panel, InterfaceEvent event) {
    if (!event.getLocation().isEmpty()) {
      panel.add(Box.createVerticalStrut(3));
      JLabel locationLabel = new JLabel("Location: " + event.getLocation());
      locationLabel.setFont(new Font("Arial", Font.PLAIN, 11));
      locationLabel.setForeground(new Color(90, 90, 90));
      locationLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
      panel.add(locationLabel);
    }
  }

  /**
   * Adds the event description label if present.
   */
  private static void addDescriptionLabel(JPanel panel, InterfaceEvent event) {
    if (!event.getDescription().isEmpty()) {
      panel.add(Box.createVerticalStrut(3));
      String desc = event.getDescription();
      if (desc.length() > 30) {
        desc = desc.substring(0, 30) + "...";
      }
      JLabel descLabel = new JLabel("Note: " + desc);
      descLabel.setFont(new Font("Arial", Font.ITALIC, 10));
      descLabel.setForeground(new Color(120, 120, 120));
      descLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
      panel.add(descLabel);
    }
  }

  /**
   * Adds status and recurring badges to the panel.
   */
  private static void addBadges(JPanel panel, InterfaceEvent event) {
    panel.add(Box.createVerticalStrut(6));
    JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    badgePanel.setBackground(Color.WHITE);
    badgePanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

    addStatusBadge(badgePanel, event);
    addRecurringBadge(badgePanel, event);

    panel.add(badgePanel);
  }

  /**
   * Adds the public/private status badge.
   */
  private static void addStatusBadge(JPanel badgePanel, InterfaceEvent event) {
    String statusText = event.isPublic() ? "Public" : "Private";
    Color statusColor = event.isPublic() ? new Color(52, 168, 83) : new Color(234, 67, 53);
    JLabel statusLabel = new JLabel(statusText);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 9));
    statusLabel.setForeground(Color.WHITE);
    statusLabel.setBackground(statusColor);
    statusLabel.setOpaque(true);
    statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    badgePanel.add(statusLabel);
  }

  /**
   * Adds the recurring badge if applicable.
   */
  private static void addRecurringBadge(JPanel badgePanel, InterfaceEvent event) {
    if (event.isPartOfSeries()) {
      JLabel recurringLabel = new JLabel("Recurring");
      recurringLabel.setFont(new Font("Arial", Font.BOLD, 9));
      recurringLabel.setForeground(Color.WHITE);
      recurringLabel.setBackground(new Color(156, 39, 176));
      recurringLabel.setOpaque(true);
      recurringLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      badgePanel.add(recurringLabel);
    }
  }
}