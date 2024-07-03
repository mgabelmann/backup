package mgabelmann.photo.copyright;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Contains information about a file.
 */
public final class FileInfo implements Serializable, Comparable<FileInfo> {
    private final String fileName;
    private final String title;
    private final LocalDateTime dateTime;

    /**
     * Constructor.
     * @param fileName path to file
     * @param title file title if exists
     * @param dateTime date and time
     */
    public FileInfo(final String fileName, final String title, final LocalDateTime dateTime) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be null or empty");

        } else if (dateTime == null) {
            throw new IllegalArgumentException("dateTime cannot be null");
        }

        this.fileName = fileName;
        this.title = title == null ? "" : title;
        this.dateTime = dateTime;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public String getName() {
        return title.isEmpty() ? fileName : title;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo fileInfo)) return false;

        return fileName.equals(fileInfo.fileName) && title.equals(fileInfo.title) && dateTime.equals(fileInfo.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, title, dateTime);
    }

    @Override
    public int compareTo(FileInfo o) {
        if (this.dateTime.compareTo(o.dateTime) == 0) {
            return this.getName().compareTo(o.getName());
        }

        return this.dateTime.compareTo(o.dateTime);
    }

}
