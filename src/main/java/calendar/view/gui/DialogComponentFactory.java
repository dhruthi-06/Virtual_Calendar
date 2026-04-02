package calendar.view.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Utility class for creating common GUI components used in dialogs.
 * Eliminates duplicate code across dialog classes.
 */
public class DialogComponentFactory {

  /**
   * Private constructor to prevent instantiation of the utility class.
   */
  private DialogComponentFactory() {
  }

  /**
   * Creates a panel suitable for a dialog header.
   *
   * @param title the title text to display.
   * @param backgroundColor the background color of the header panel.
   * @return a JPanel styled as a header.
   */
  public static JPanel createHeaderPanel(String title, Color backgroundColor) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(backgroundColor);
    panel.setBorder(new EmptyBorder(15, 20, 15, 20));

    JLabel headerLabel = new JLabel(title);
    headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
    headerLabel.setForeground(Color.WHITE);
    panel.add(headerLabel, BorderLayout.WEST);
    return panel;
  }

  /**
   * Creates a panel containing confirmation and cancel buttons.
   *
   * @param confirmText the text for the confirmation button.
   * @param confirmColor the background color for the confirmation button.
   * @param onConfirm the action to run when the confirm button is clicked.
   * @param onCancel the action to run when the cancel button is clicked.
   * @return a JPanel containing the action buttons.
   */
  public static JPanel createButtonPanel(String confirmText, Color confirmColor,
                                         Runnable onConfirm, Runnable onCancel) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    panel.setBackground(Color.WHITE);

    JButton confirmButton = createStyledButton(confirmText, confirmColor, true);
    confirmButton.addActionListener(e -> onConfirm.run());

    JButton cancelButton = createStyledButton("Cancel", new Color(240, 240, 240), false);
    cancelButton.addActionListener(e -> onCancel.run());

    panel.add(confirmButton);
    panel.add(cancelButton);
    return panel;
  }

  /**
   * Creates a styled button.
   *
   * @param text button text
   * @param bgColor background color
   * @param isPrimary whether this is a primary button
   * @return styled JButton
   */
  private static JButton createStyledButton(String text, Color bgColor, boolean isPrimary) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", isPrimary ? Font.BOLD : Font.PLAIN, 13));
    button.setPreferredSize(new Dimension(isPrimary ? 130 : 100, 35));
    button.setBackground(bgColor);
    button.setForeground(isPrimary ? Color.WHITE : Color.BLACK);
    button.setOpaque(true);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    return button;
  }

  /**
   * Displays an error message dialog.
   *
   * @param parent the parent component for the dialog.
   * @param message the error message to display.
   */
  public static void showError(java.awt.Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays a success or information message dialog.
   *
   * @param parent the parent component for the dialog.
   * @param message the success message to display.
   */
  public static void showMessage(java.awt.Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Creates a GridBagConstraints with standard settings.
   *
   * @param x grid x position
   * @param y grid y position
   * @param weightx weight in x direction
   * @return configured GridBagConstraints
   */
  public static GridBagConstraints createStandardGbc(int x, int y, double weightx) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 15, 10, 15);
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.weightx = weightx;
    return gbc;
  }

  /**
   * Creates a GridBagConstraints with custom insets.
   *
   * @param x grid x position
   * @param y grid y position
   * @param weightx weight in x direction
   * @param insets custom insets
   * @return configured GridBagConstraints
   */
  public static GridBagConstraints createCustomGbc(int x, int y, double weightx, Insets insets) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = insets;
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.weightx = weightx;
    return gbc;
  }

  /**
   * Creates a bold label with standard font.
   *
   * @param text label text
   * @return JLabel with bold font
   */
  public static JLabel createBoldLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Arial", Font.BOLD, 13));
    return label;
  }

  /**
   * Creates an error label with standard styling.
   *
   * @return JLabel configured for error messages
   */
  public static JLabel createErrorLabel() {
    JLabel label = new JLabel(" ");
    label.setFont(new Font("Arial", Font.PLAIN, 11));
    label.setForeground(new Color(220, 53, 69));
    return label;
  }

  /**
   * Creates a titled border with standard styling.
   *
   * @param title border title
   * @return TitledBorder with standard styling
   */
  public static TitledBorder createStandardTitledBorder(String title) {
    return BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
        title,
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 12));
  }
}