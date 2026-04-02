package calendar.model;

import calendar.util.WeekdayCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of a recurring event.
 */
public class RecurringEvent implements InterfaceRecurringEvent {

  private final InterfaceEvent baseEvent;
  private final Set<WeekdayCode> weekdays;
  private final Integer repeatCount;
  private final LocalDateTime repeatUntil;
  private final String seriesId;

  /**
   * Builder class for RecurringEvent.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String weekdaysStr;
    private String description = "";
    private String location = "";
    private boolean isPublic = true;
    private Integer repeatCount = null;
    private LocalDateTime repeatUntil = null;

    /**
     * Constructs a Builder.
     *
     * @param subject the event subject
     * @param start the start date/time
     * @param end the end date/time
     * @param weekdaysStr the weekdays string
     */
    public Builder(String subject, LocalDateTime start, LocalDateTime end, String weekdaysStr) {
      this.subject = subject;
      this.start = start;
      this.end = end;
      this.weekdaysStr = weekdaysStr;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location the location
     * @return this builder
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the public status.
     *
     * @param isPublic true if public
     * @return this builder
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets the repeat count.
     *
     * @param repeatCount the number of repetitions
     * @return this builder
     */
    public Builder repeatCount(Integer repeatCount) {
      this.repeatCount = repeatCount;
      return this;
    }

    /**
     * Sets the repeat until date.
     *
     * @param repeatUntil the end date for repetitions
     * @return this builder
     */
    public Builder repeatUntil(LocalDateTime repeatUntil) {
      this.repeatUntil = repeatUntil;
      return this;
    }

    /**
     * Builds the RecurringEvent.
     *
     * @return a new RecurringEvent instance
     */
    public RecurringEvent build() {
      return new RecurringEvent(this);
    }
  }

  /**
   * Private constructor using Builder pattern.
   * This is the only way to construct a RecurringEvent.
   *
   * @param builder the builder with all necessary parameters
   */
  private RecurringEvent(Builder builder) {
    if (!builder.start.toLocalDate().equals(builder.end.toLocalDate())) {
      throw new IllegalArgumentException("Recurring events cannot span multiple days");
    }

    if (builder.repeatCount == null && builder.repeatUntil == null) {
      throw new IllegalArgumentException("Must specify either repeat count or repeat until date");
    }

    if (builder.repeatCount != null && builder.repeatUntil != null) {
      throw new IllegalArgumentException("Cannot specify both repeat count and repeat until date");
    }

    if (builder.repeatCount != null && builder.repeatCount <= 0) {
      throw new IllegalArgumentException("Repeat count must be positive");
    }

    if (builder.repeatUntil != null && builder.repeatUntil.isBefore(builder.start)) {
      throw new IllegalArgumentException("Repeat until date cannot be before start date");
    }

    this.baseEvent = new Event(builder.subject, builder.start, builder.end);
    this.baseEvent.setDescription(builder.description);
    this.baseEvent.setLocation(builder.location);
    this.baseEvent.setPublic(builder.isPublic);

    this.weekdays = parseWeekdays(builder.weekdaysStr);
    this.repeatCount = builder.repeatCount;
    this.repeatUntil = builder.repeatUntil;
    this.seriesId = UUID.randomUUID().toString();
    this.baseEvent.setSeriesId(seriesId);
  }

  private Set<WeekdayCode> parseWeekdays(String weekdaysStr) {
    Set<WeekdayCode> days = new HashSet<>();

    if (weekdaysStr == null || weekdaysStr.isEmpty()) {
      throw new IllegalArgumentException("Weekdays cannot be empty");
    }

    for (char c : weekdaysStr.toUpperCase().toCharArray()) {
      String code = String.valueOf(c);
      WeekdayCode dayCode = WeekdayCode.fromCode(code);
      days.add(dayCode);
    }

    if (days.isEmpty()) {
      throw new IllegalArgumentException("Must specify at least one weekday");
    }

    return days;
  }

  @Override
  public InterfaceEvent getBaseEvent() {
    return baseEvent;
  }

  @Override
  public String getSubject() {
    return baseEvent.getSubject();
  }

  @Override
  public LocalDateTime getStartTime() {
    return baseEvent.getStart();
  }

  @Override
  public LocalDateTime getEndTime() {
    return baseEvent.getEnd();
  }

  @Override
  public Set<String> getWeekdays() {
    Set<String> codes = new HashSet<>();
    for (WeekdayCode wc : weekdays) {
      codes.add(wc.getCode());
    }
    return codes;
  }

  @Override
  public Integer getRepeatCount() {
    return repeatCount;
  }

  @Override
  public LocalDateTime getRepeatUntil() {
    return repeatUntil;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public List<InterfaceEvent> generateOccurrences() {
    List<InterfaceEvent> occurrences = new ArrayList<>();
    LocalDate currentDate = baseEvent.getStart().toLocalDate();
    LocalDate endDate = determineEndDate();

    int count = 0;

    while (!currentDate.isAfter(endDate)) {
      if (shouldCreateOccurrenceOn(currentDate)) {
        InterfaceEvent occurrence = createOccurrenceForDate(currentDate);
        occurrences.add(occurrence);

        count++;
        if (hasReachedRepeatLimit(count)) {
          break;
        }
      }

      currentDate = currentDate.plusDays(1);
    }

    return occurrences;
  }

  /**
   * Determines the end date for generating occurrences.
   * Extracted method to reduce generateOccurrences() complexity.
   */
  private LocalDate determineEndDate() {
    if (repeatUntil != null) {
      return repeatUntil.toLocalDate();
    }
    return baseEvent.getStart().toLocalDate().plusYears(10);
  }

  /**
   * Checks if an occurrence should be created on the given date.
   * Extracted method to reduce generateOccurrences() complexity.
   */
  private boolean shouldCreateOccurrenceOn(LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    WeekdayCode dayCode = WeekdayCode.fromDayOfWeek(dayOfWeek);
    return weekdays.contains(dayCode);
  }

  /**
   * Checks if the repeat limit has been reached.
   * Extracted method to reduce generateOccurrences() complexity.
   */
  private boolean hasReachedRepeatLimit(int count) {
    return repeatCount != null && count >= repeatCount;
  }

  /**
   * Creates a single event occurrence for the given date.
   * Extracted method to reduce generateOccurrences() complexity.
   */
  private InterfaceEvent createOccurrenceForDate(LocalDate date) {
    LocalDateTime occurrenceStart = LocalDateTime.of(
        date,
        baseEvent.getStart().toLocalTime()
    );
    LocalDateTime occurrenceEnd = LocalDateTime.of(
        date,
        baseEvent.getEnd().toLocalTime()
    );

    InterfaceEvent occurrence = new Event(occurrenceStart.toString(),
        occurrenceStart, occurrenceEnd);
    occurrence.setSubject(baseEvent.getSubject());
    occurrence.setDescription(baseEvent.getDescription());
    occurrence.setLocation(baseEvent.getLocation());
    occurrence.setPublic(baseEvent.isPublic());
    occurrence.setSeriesId(seriesId);

    return occurrence;
  }

  @Override
  public List<InterfaceEvent> generateOccurrencesInRange(LocalDate start, LocalDate end) {
    List<InterfaceEvent> allOccurrences = generateOccurrences();
    List<InterfaceEvent> filteredOccurrences = new ArrayList<>();

    for (InterfaceEvent event : allOccurrences) {
      LocalDate eventDate = event.getStart().toLocalDate();
      if (!eventDate.isBefore(start) && !eventDate.isAfter(end)) {
        filteredOccurrences.add(event);
      }
    }

    return filteredOccurrences;
  }

  @Override
  public boolean hasOccurrenceOn(LocalDate date) {
    DayOfWeek dayOfWeek = date.getDayOfWeek();
    WeekdayCode dayCode = WeekdayCode.fromDayOfWeek(dayOfWeek);

    if (!weekdays.contains(dayCode)) {
      return false;
    }

    LocalDate startDate = baseEvent.getStart().toLocalDate();

    if (date.isBefore(startDate)) {
      return false;
    }

    if (repeatUntil != null && date.isAfter(repeatUntil.toLocalDate())) {
      return false;
    }

    if (repeatCount != null) {
      List<InterfaceEvent> occurrences = generateOccurrences();
      for (InterfaceEvent event : occurrences) {
        if (event.getStart().toLocalDate().equals(date)) {
          return true;
        }
      }
      return false;
    }

    return true;
  }

  @Override
  public LocalDate getFirstOccurrenceDate() {
    return baseEvent.getStart().toLocalDate();
  }

  @Override
  public LocalDate getLastOccurrenceDate() {
    List<InterfaceEvent> occurrences = generateOccurrences();
    if (occurrences.isEmpty()) {
      return baseEvent.getStart().toLocalDate();
    }
    return occurrences.get(occurrences.size() - 1).getStart().toLocalDate();
  }

  @Override
  public int getOccurrenceCount() {
    if (repeatCount != null) {
      return repeatCount;
    }
    return generateOccurrences().size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Recurring Event: ").append(baseEvent.getSubject());
    sb.append(" on ").append(getWeekdays());
    if (repeatCount != null) {
      sb.append(" for ").append(repeatCount).append(" times");
    } else {
      sb.append(" until ").append(repeatUntil.toLocalDate());
    }
    return sb.toString();
  }
}