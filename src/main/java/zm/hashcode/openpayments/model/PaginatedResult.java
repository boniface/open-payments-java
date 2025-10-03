package zm.hashcode.openpayments.model;

import java.util.List;
import java.util.Optional;

/**
 * Represents a paginated result from the Open Payments API.
 *
 * @param items
 *            the list of items in this page
 * @param cursor
 *            the pagination cursor for the next page
 * @param hasMore
 *            whether there are more pages available
 * @param <T>
 *            the type of items in the result
 */
public record PaginatedResult<T>(List<T> items, String cursor, boolean hasMore) {

    public PaginatedResult {
        items = List.copyOf(items);
    }

    /**
     * Creates a new paginated result.
     *
     * @param items
     *            the list of items
     * @param cursor
     *            the pagination cursor
     * @param hasMore
     *            whether there are more pages
     * @param <T>
     *            the item type
     * @return a new PaginatedResult instance
     */
    public static <T> PaginatedResult<T> of(List<T> items, String cursor, boolean hasMore) {
        return new PaginatedResult<>(items, cursor, hasMore);
    }

    /**
     * Returns the cursor for the next page, if available.
     *
     * @return an Optional containing the cursor
     */
    public Optional<String> getCursor() {
        return Optional.ofNullable(cursor);
    }

    /**
     * Returns the number of items in this page.
     *
     * @return the number of items
     */
    public int size() {
        return items.size();
    }
}
