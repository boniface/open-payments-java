package zm.hashcode.openpayments.payment.quote;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service for quote operations in the Open Payments API.
 *
 * <p>
 * Quotes provide information about exchange rates and fees for payments before they are executed.
 */
public interface QuoteService {

    /**
     * Creates a new quote for a payment.
     *
     * @param requestBuilder
     *            a consumer to build the request
     * @return a CompletableFuture containing the created quote
     */
    CompletableFuture<Quote> create(Consumer<QuoteRequest.Builder> requestBuilder);

    /**
     * Retrieves a quote by its URL.
     *
     * @param url
     *            the quote URL
     * @return a CompletableFuture containing the quote
     */
    CompletableFuture<Quote> get(String url);

    /**
     * Retrieves a quote by its URI.
     *
     * @param uri
     *            the quote URI
     * @return a CompletableFuture containing the quote
     */
    CompletableFuture<Quote> get(URI uri);
}
