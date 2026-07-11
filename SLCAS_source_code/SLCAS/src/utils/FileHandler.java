package utils;

import model.*;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading the library catalogue and user accounts to
 * a simple pipe-delimited text file (a lightweight stand-in for JSON),
 * plus CSV import/export helpers used by the Admin panel's file chooser.
 */
public class FileHandler {

    public static void saveDatabase(LibraryDatabase db, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("#ITEMS");
            writer.newLine();
            for (LibraryItem item : db.getItems()) {
                writer.write(item.toDataLine());
                writer.newLine();
            }
            writer.write("#USERS");
            writer.newLine();
            for (UserAccount user : db.getUsers()) {
                writer.write(user.getUserId() + "|" + user.getName());
                writer.newLine();
            }
        }
    }

    public static void loadDatabase(LibraryDatabase db, String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) return;
        db.getItems().clear();
        db.getUsers().clear();

        List<String> lines = Files.readAllLines(Paths.get(path));
        String section = "";
        for (String line : lines) {
            if (line.isBlank()) continue;
            if (line.equals("#ITEMS")) { section = "ITEMS"; continue; }
            if (line.equals("#USERS")) { section = "USERS"; continue; }

            if (section.equals("ITEMS")) {
                LibraryItem item = parseItemLine(line);
                if (item != null) {
                    db.addItem(item);
                    IDGenerator.reportExistingItemId(item.getId());
                }
            } else if (section.equals("USERS")) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 2) {
                    db.addUser(new UserAccount(p[0], p[1]));
                    IDGenerator.reportExistingUserId(p[0]);
                }
            }
        }
    }

    private static LibraryItem parseItemLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 8) return null;
        String type = p[0];
        String id = p[1];
        String title = p[2];
        String author = p[3];
        int year = Integer.parseInt(p[4]);
        String category = p[5];
        boolean available = Boolean.parseBoolean(p[6]);
        int accessCount = Integer.parseInt(p[7]);
        String extra = p.length > 8 ? p[8] : "";

        LibraryItem item;
        switch (type) {
            case "Book": item = new Book(id, title, author, year, category, extra); break;
            case "Magazine": item = new Magazine(id, title, author, year, category, extra); break;
            case "Journal": item = new Journal(id, title, author, year, category, extra); break;
            default: return null;
        }
        item.setAvailable(available);
        for (int i = 0; i < accessCount; i++) item.incrementAccessCount();
        return item;
    }

    /** Exports the current catalogue to a CSV file (used by the file-chooser export action). */
    public static void exportCsv(LibraryDatabase db, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("Type,ID,Title,Author,Year,Category,Available,AccessCount");
            writer.newLine();
            for (LibraryItem item : db.getItems()) {
                writer.write(String.join(",",
                        item.getType(), item.getId(), csvSafe(item.getTitle()), csvSafe(item.getAuthor()),
                        String.valueOf(item.getYear()), csvSafe(item.getCategory()),
                        String.valueOf(item.isAvailable()), String.valueOf(item.getAccessCount())));
                writer.newLine();
            }
        }
    }

    /** Imports items from a CSV file with the same header format produced by exportCsv. */
    public static List<LibraryItem> importCsv(String path) throws IOException {
        List<LibraryItem> imported = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(path));
        for (int i = 1; i < lines.size(); i++) { // skip header
            String line = lines.get(i);
            if (line.isBlank()) continue;
            String[] p = line.split(",", -1);
            if (p.length < 8) continue;
            String type = p[0];
            String id = p[1].isBlank() ? IDGenerator.nextItemId() : p[1];
            String title = p[2], author = p[3];
            int year;
            try { year = Integer.parseInt(p[4]); } catch (NumberFormatException e) { year = 0; }
            String category = p[5];
            LibraryItem item;
            switch (type) {
                case "Magazine": item = new Magazine(id, title, author, year, category, "N/A"); break;
                case "Journal": item = new Journal(id, title, author, year, category, "N/A"); break;
                default: item = new Book(id, title, author, year, category, "N/A"); break;
            }
            imported.add(item);
        }
        return imported;
    }

    private static String csvSafe(String s) {
        if (s == null) return "";
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
