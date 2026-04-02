package calendar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Handles event property editing operations.
 */
public class EventEditor {


  private final Map<String, BiConsumer<InterfaceEvent, String>> propertyUpdaters;


  private final Map<String, Function<String, String>> propertyValidators;

  /**
   * Constructs an EventEditor.
   */
  public EventEditor() {
    this.propertyUpdaters = createPropertyUpdaters();
    this.propertyValidators = createPropertyValidators();
  }

  /**
   * Updates a property of an event.
   * Uses Map lookup instead of switch statement.
   *
   * @param event the event to update
   * @param property the property to update (case-insensitive)
   * @param newValue the new value
   * @throws IllegalArgumentException if property is invalid or value is invalid
   */
  public void updateProperty(InterfaceEvent event, String property, String newValue) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (property == null || property.trim().isEmpty()) {
      throw new IllegalArgumentException("Property cannot be null or empty");
    }

    String normalizedProperty = property.toLowerCase().trim();

    BiConsumer<InterfaceEvent, String> updater = propertyUpdaters.get(normalizedProperty);

    if (updater == null) {
      throw new IllegalArgumentException("Invalid property: " + property
          + ". Valid properties: subject, start, end, description, location, status");
    }

    updater.accept(event, newValue);
  }

  /**
   * Checks if a property name is valid.
   * Useful for validation before attempting updates.
   *
   * @param property the property name to check
   * @return true if the property is valid
   */
  public boolean isValidProperty(String property) {
    if (property == null || property.trim().isEmpty()) {
      return false;
    }

    String normalized = property.toLowerCase().trim();
    return propertyUpdaters.containsKey(normalized);
  }

  /**
   * Gets a list of all valid property names.
   *
   * @return array of valid property names
   */
  public String[] getValidProperties() {
    return new String[]{"subject", "start", "end", "description", "location", "status"};
  }

  /**
   * Validates a property value before updating.
   * Uses Map lookup instead of switch statement.
   * Returns an error message if invalid, null if valid.
   *
   * @param property the property name
   * @param value the value to validate
   * @return error message if invalid, null if valid
   */
  public String validatePropertyValue(String property, String value) {
    if (property == null || property.trim().isEmpty()) {
      return "Property cannot be null or empty";
    }

    String normalized = property.toLowerCase().trim();

    Function<String, String> validator = propertyValidators.get(normalized);

    if (validator == null) {
      return "Invalid property: " + property;
    }

    return validator.apply(value);
  }



  /**
   * Creates Map of property updaters.
   *  Replaces switch statement in updateProperty()
   */
  private Map<String, BiConsumer<InterfaceEvent, String>> createPropertyUpdaters() {
    Map<String, BiConsumer<InterfaceEvent, String>> updaters = new HashMap<>();

    updaters.put("subject", this::updateSubject);
    updaters.put("start", this::updateStartTime);
    updaters.put("end", this::updateEndTime);
    updaters.put("description", this::updateDescription);
    updaters.put("location", this::updateLocation);
    updaters.put("status", this::updateStatus);

    return updaters;
  }

  /**
   * Creates Map of property validators.
   * Replaces switch statement in validatePropertyValue()
   */
  private Map<String, Function<String, String>> createPropertyValidators() {
    Map<String, Function<String, String>> validators = new HashMap<>();

    validators.put("subject", this::validateSubject);
    validators.put("start", this::validateDateTime);
    validators.put("end", this::validateDateTime);
    validators.put("description", this::validateDescriptionOrLocation);
    validators.put("location", this::validateDescriptionOrLocation);
    validators.put("status", this::validateStatus);

    return validators;
  }



  /**
   * Updates the subject of an event.
   * Delegates to Event's updateSubject method which handles validation.
   *
   * @param event the event to update
   * @param newValue the new subject value
   */
  private void updateSubject(InterfaceEvent event, String newValue) {
    event.updateSubject(newValue);
  }

  /**
   * Updates the start time of an event.
   * Parses the date/time string and delegates to Event's updateStart method.
   *
   * @param event the event to update
   * @param newValue the new start time value
   */
  private void updateStartTime(InterfaceEvent event, String newValue) {
    try {
      LocalDateTime newStart = LocalDateTime.parse(newValue);
      event.updateStart(newStart);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + newValue
          + ". Expected: YYYY-MM-DDTHH:mm (e.g., 2025-11-15T10:00)");
    }
  }

  /**
   * Updates the end time of an event.
   * Parses the date/time string and delegates to Event's updateEnd method.
   *
   * @param event the event to update
   * @param newValue the new end time value
   */
  private void updateEndTime(InterfaceEvent event, String newValue) {
    try {
      LocalDateTime newEnd = LocalDateTime.parse(newValue);
      event.updateEnd(newEnd);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + newValue
          + ". Expected: YYYY-MM-DDTHH:mm (e.g., 2025-11-15T17:00)");
    }
  }

  /**
   * Updates the description of an event.
   *
   * @param event the event to update
   * @param newValue the new description value
   */
  private void updateDescription(InterfaceEvent event, String newValue) {
    event.setDescription(newValue != null ? newValue : "");
  }

  /**
   * Updates the location of an event.
   *
   * @param event the event to update
   * @param newValue the new location value
   */
  private void updateLocation(InterfaceEvent event, String newValue) {
    event.setLocation(newValue != null ? newValue : "");
  }

  /**
   * Updates the status (visibility) of an event.
   * Delegates to Event's updateStatus method which handles validation.
   *
   * @param event the event to update
   * @param newValue the new status value
   */
  private void updateStatus(InterfaceEvent event, String newValue) {
    event.updateStatus(newValue);
  }



  /**
   * Validates a subject value.
   *
   * @param value the value to validate
   * @return error message if invalid, null if valid
   */
  private String validateSubject(String value) {
    if (value == null || value.trim().isEmpty()) {
      return "Subject cannot be empty";
    }
    return null;
  }

  /**
   * Validates a date/time value.
   *
   * @param value the value to validate
   * @return error message if invalid, null if valid
   */
  private String validateDateTime(String value) {
    try {
      LocalDateTime.parse(value);
      return null;
    } catch (Exception e) {
      return "Invalid date/time format. Expected: YYYY-MM-DDTHH:mm";
    }
  }

  /**
   * Validates a status value.
   *
   * @param value the value to validate
   * @return error message if invalid, null if valid
   */
  private String validateStatus(String value) {
    if (value == null
        || (!value.equalsIgnoreCase("public") && !value.equalsIgnoreCase("private"))) {
      return "Status must be 'public' or 'private'";
    }
    return null;
  }

  /**
   * Validates a description or location value.
   * These properties accept any value including empty strings.
   *
   * @param value the value to validate
   * @return always returns null (no validation errors)
   */
  private String validateDescriptionOrLocation(String value) {
    return null;
  }
}