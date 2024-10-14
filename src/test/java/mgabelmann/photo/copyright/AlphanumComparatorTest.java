package mgabelmann.photo.copyright;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


class AlphanumComparatorTest {

    private FileInfo f1, f2, f3, f4, f5, f6, f7, f8, f9, f10;

    @BeforeEach
    void setup() {
        this.f1 = this.getFileInfo("10");
        this.f2 = this.getFileInfo("215");
        this.f3 = this.getFileInfo("0");
        this.f4 = this.getFileInfo("3");
        this.f5 = this.getFileInfo("1");
        this.f6 = this.getFileInfo("2");

        this.f7 = this.getFileInfo("a");
        this.f8 = this.getFileInfo("A");
        this.f9 = this.getFileInfo("b");
        this.f10 = this.getFileInfo("B");
    }

    @Test
    @DisplayName("sort list")
    void test1_compare() {
        List<FileInfo> items = Arrays.asList(f1, f2, f3, f4, f5, f6);

        items.sort(new AlphanumComparator());

        Assertions.assertEquals(Arrays.asList(f3, f5, f6, f4, f1, f2), items);
    }

    @Test
    @DisplayName("sort list with a null in it")
    void test2_compare() {
        List<FileInfo> items = Arrays.asList(null, f1);
        items.sort(new AlphanumComparator());
        Assertions.assertEquals(Arrays.asList(null, f1), items);
    }

    @Test
    @DisplayName("sort case")
    void test3_compare() {
        List<FileInfo> items = Arrays.asList(f10, f9, f8, f7);
        items.sort(new AlphanumComparator());
        Assertions.assertEquals(Arrays.asList(f8, f10, f7, f9), items);
    }

    private FileInfo getFileInfo(String path) {
        return new FileInfo(Path.of(path), path, LocalDateTime.now());
    }

}
