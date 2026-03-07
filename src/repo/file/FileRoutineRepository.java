package repo.file;

import models.routine.RoutineEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileRoutineRepository {
    private static final String FILE_PATH = "data/routines/student_routines.txt";

    public List<RoutineEntry> findAll() {
        List<RoutineEntry> entries = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return entries;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                RoutineEntry entry = RoutineEntry.fromFileString(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read routine file: " + e.getMessage());
        }

        return entries;
    }

    public List<RoutineEntry> findByStudent(String studentId) {
        List<RoutineEntry> result = new ArrayList<>();
        for (RoutineEntry entry : findAll()) {
            if (entry.getStudentId().equals(studentId)) {
                result.add(entry);
            }
        }
        return result;
    }

    public String getSlotContent(String studentId, DayOfWeek day, int slotIndex) {
        List<RoutineEntry> entries = findByStudent(studentId);
        for (RoutineEntry entry : entries) {
            if (entry.getDay() == day && entry.getSlotIndex() == slotIndex) {
                return entry.getContent();
            }
        }
        return "";
    }

    public void upsert(String studentId, DayOfWeek day, int slotIndex, String content) {
        List<RoutineEntry> entries = findAll();
        List<RoutineEntry> updated = new ArrayList<>();

        for (RoutineEntry entry : entries) {
            boolean sameKey = entry.getStudentId().equals(studentId)
                    && entry.getDay() == day
                    && entry.getSlotIndex() == slotIndex;
            if (!sameKey) {
                updated.add(entry);
            }
        }

        if (content != null && !content.trim().isEmpty()) {
            updated.add(new RoutineEntry(studentId, day, slotIndex, content));
        }

        saveAll(updated);
    }

    private void saveAll(List<RoutineEntry> entries) {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        Collections.sort(entries, Comparator
                .comparing(RoutineEntry::getStudentId)
                .thenComparing(entry -> entry.getDay().getValue())
                .thenComparing(RoutineEntry::getSlotIndex));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (RoutineEntry entry : entries) {
                bw.write(entry.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save routine file: " + e.getMessage());
        }
    }
}
