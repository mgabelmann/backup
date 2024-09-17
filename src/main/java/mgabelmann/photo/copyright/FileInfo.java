package mgabelmann.photo.copyright;

import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Contains information about a file.
 */
public final class FileInfo implements Serializable, Comparable<FileInfo> {
    private final Path path;
    private final LocalDateTime dateTime;

    private String title;


    /**
     * Constructor.
     * @param path path to file
     * @param title file title if exists
     * @param dateTime date and time
     */
    public FileInfo(final Path path, final String title, final LocalDateTime dateTime) {
        if (path == null) {
            throw new IllegalArgumentException("path can not be null");

//        } else if (!Files.exists(path)) {
//            throw new IllegalArgumentException("path does not exist");

        } else if (dateTime == null) {
            throw new IllegalArgumentException("dateTime can not be null");
        }

        this.path = path;
        this.dateTime = dateTime;
        this.setTitle(title);
    }

    public Path getPath() {
        return path;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title == null ? "" : title;
    }

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public String getName() {
        return title.isEmpty() ? path.getFileName().toString() : title;
    }

    public String getDirectory() {
        return path.getParent().toString();
    }

    public String getFilename() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileName='" + path.getFileName() + '\'' +
                ", title='" + title + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo fileInfo)) return false;

        return path.equals(fileInfo.path) && title.equals(fileInfo.title) && dateTime.equals(fileInfo.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, title, dateTime);
    }

    @Override
    public int compareTo(FileInfo o) {
        if (this.dateTime.isEqual(o.dateTime)) {
            return this.getName().compareTo(o.getName());
        }

        return this.dateTime.compareTo(o.dateTime);
    }

}
