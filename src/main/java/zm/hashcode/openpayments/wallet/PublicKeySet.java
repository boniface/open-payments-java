package zm.hashcode.openpayments.wallet;

import java.util.List;

/**
 * Represents a set of public keys associated with a wallet address.
 *
 * @param keys
 *            the list of public keys
 */
public record PublicKeySet(List<PublicKey> keys) {

    public PublicKeySet {
        keys = List.copyOf(keys);
    }

    public static PublicKeySet of(List<PublicKey> keys) {
        return new PublicKeySet(keys);
    }
}
