package calendar.model;

import java.time.LocalDateTime;

/**
 * Parameter object for event creation requests.
 */
public class EventCreationRequest {

  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Private constructor.
   *
   * @param builder the builder instance
   */
  private EventCreationRequest(Builder builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.description = builder.description;
    this.location = builder.location;
    this.isPublic = builder.isPublic;
  }

  /**
   * Gets the event subject.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date and time.
   *
   * @return the start date time
   */
  public LocalDateTime getStart() {
    return start;
  }

  /**
   * Gets the end date and time.
   *
   * @return the end date time
   */
  public LocalDateTime getEnd() {
    return end;
  }

  /**
   * Gets the event description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the event location.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks if the event is public.
   *
   * @return true if public, false if private
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Builder for EventCreationRequest.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private String description = "";
    private String location = "";
    private boolean isPublic = true;

    /**
     * Constructor with required fields.
     *
     * @param subject event subject
     * @param start start date/time
     * @param end end date/time
     */
    public Builder(String subject, LocalDateTime start, LocalDateTime end) {
      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    /**
     * Sets the description.
     *
     * @param description the event description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the event location
     * @return this builder
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the public status.
     *
     * @param isPublic true if public, false if private
     * @return this builder
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Builds the EventCreationRequest.
     *
     * @return a new EventCreationRequest instance
     */
    public EventCreationRequest build() {
      return new EventCreationRequest(this);
    }
  }
}