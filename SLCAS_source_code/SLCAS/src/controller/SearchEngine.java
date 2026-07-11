package controller;

import model.LibraryItem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Collection of hand-written search algorithms. Linear search works on any
 * (sorted or unsorted) list. Binary search requires the list to already be
 * sorted by the same field being searched. Recursive search demonstrates
 * the recursion requirement while behaving like a linear scan.
 */
public class SearchEngine {

    public enum SearchType { LINEAR, BINARY, RECURSIVE }
    public enum Field { TITLE, AUTHOR, TYPE }

    /** Case-insensitive substring linear search. Works regardless of sort order. */
    public static List<LibraryItem> linearSearch(List<LibraryItem> list, String query, Field field) {
        List<LibraryItem> results = new ArrayList<>();
        String q = query.toLowerCase();
        for (LibraryItem item : list) {
            if (matches(item, q, field)) results.add(item);
        }
        return results;
    }

    /**
     * Binary search for an EXACT title match. The caller must pass a list
     * already sorted by title (ascending); otherwise results are undefined.
     * Returns the matching item or null.
     */
    public static LibraryItem binarySearchByTitle(List<LibraryItem> sortedByTitle, String title) {
        int low = 0, high = sortedByTitle.size() - 1;
        String target = title.toLowerCase();
        while (low <= high) {
            int mid = (low + high) / 2;
            String midTitle = sortedByTitle.get(mid).getTitle().toLowerCase();
            int cmp = midTitle.compareTo(target);
            if (cmp == 0) return sortedByTitle.get(mid);
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }

    /**
     * Recursive linear search over the list (recursive requirement #1 -
     * "recursive search in the library catalogue"). Collects every item
     * whose field contains the query substring.
     */
    public static List<LibraryItem> recursiveSearch(List<LibraryItem> list, String query, Field field) {
        List<LibraryItem> results = new ArrayList<>();
        recursiveSearchHelper(list, query.toLowerCase(), field, 0, results);
        return results;
    }

    private static void recursiveSearchHelper(List<LibraryItem> list, String query, Field field,
                                               int index, List<LibraryItem> results) {
        if (index >= list.size()) {
            return; // base case
        }
        if (matches(list.get(index), query, field)) {
            results.add(list.get(index));
        }
        recursiveSearchHelper(list, query, field, index + 1, results); // recursive case
    }

    private static boolean matches(LibraryItem item, String query, Field field) {
        switch (field) {
            case TITLE: return item.getTitle().toLowerCase().contains(query);
            case AUTHOR: return item.getAuthor().toLowerCase().contains(query);
            case TYPE: return item.getType().toLowerCase().contains(query);
            default: return false;
        }
    }
}
