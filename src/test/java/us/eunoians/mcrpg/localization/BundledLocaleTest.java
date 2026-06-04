package us.eunoians.mcrpg.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BundledLocaleTest {

    @DisplayName("Every bundled locale has a non-empty folder name")
    @ParameterizedTest
    @EnumSource(BundledLocale.class)
    void getFolderName_isNotEmpty(BundledLocale locale) {
        assertNotNull(locale.getFolderName());
        assertFalse(locale.getFolderName().isEmpty());
    }

    @DisplayName("Every bundled locale has at least one file")
    @ParameterizedTest
    @EnumSource(BundledLocale.class)
    void getFileNames_isNotEmpty(BundledLocale locale) {
        assertNotNull(locale.getFileNames());
        assertFalse(locale.getFileNames().isEmpty());
    }

    @DisplayName("ENGLISH locale folder is 'english'")
    @Test
    void english_folderName() {
        assertEquals("english", BundledLocale.ENGLISH.getFolderName());
    }

    @DisplayName("ENGLISH locale contains en.yml")
    @Test
    void english_containsMainFile() {
        assertTrue(BundledLocale.ENGLISH.getFileNames().contains("en.yml"));
    }

    @DisplayName("ENGLISH locale contains all expected files")
    @Test
    void english_containsAllExpectedFiles() {
        var files = BundledLocale.ENGLISH.getFileNames();
        assertTrue(files.contains("en.yml"));
        assertTrue(files.contains("en_commands.yml"));
        assertTrue(files.contains("en_gui.yml"));
        assertTrue(files.contains("en_abilities.yml"));
        assertTrue(files.contains("en_skills.yml"));
        assertTrue(files.contains("en_quest.yml"));
        assertTrue(files.contains("en_stats.yml"));
    }

    @DisplayName("getFileNames returns unmodifiable list")
    @Test
    void getFileNames_isUnmodifiable() {
        var files = BundledLocale.ENGLISH.getFileNames();

        assertThrows(UnsupportedOperationException.class, () -> files.add("extra.yml"));
    }

    @DisplayName("fromFolderName returns ENGLISH for exact match")
    @Test
    void fromFolderName_exactMatch_returnsEnglish() {
        Optional<BundledLocale> result = BundledLocale.fromFolderName("english");

        assertTrue(result.isPresent());
        assertEquals(BundledLocale.ENGLISH, result.orElseThrow());
    }

    @DisplayName("fromFolderName is case-insensitive")
    @ParameterizedTest
    @ValueSource(strings = {"ENGLISH", "English", "eNgLiSh"})
    void fromFolderName_caseInsensitive_returnsEnglish(String input) {
        Optional<BundledLocale> result = BundledLocale.fromFolderName(input);

        assertTrue(result.isPresent());
        assertEquals(BundledLocale.ENGLISH, result.orElseThrow());
    }

    @DisplayName("fromFolderName returns empty for unknown folder")
    @Test
    void fromFolderName_unknownFolder_returnsEmpty() {
        Optional<BundledLocale> result = BundledLocale.fromFolderName("klingon");

        assertTrue(result.isEmpty());
    }

    @DisplayName("fromFolderName returns empty for empty string")
    @Test
    void fromFolderName_emptyString_returnsEmpty() {
        Optional<BundledLocale> result = BundledLocale.fromFolderName("");

        assertTrue(result.isEmpty());
    }
}
