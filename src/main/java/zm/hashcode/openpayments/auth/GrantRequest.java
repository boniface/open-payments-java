package zm.hashcode.openpayments.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request object for requesting a grant.
 */
public final class GrantRequest {
    private final List<AccessRight> accessRights;
    private final InteractMode interactMode;

    private GrantRequest(Builder builder) {
        this.accessRights = builder.accessRights != null ? List.copyOf(builder.accessRights) : List.of();
        this.interactMode = builder.interactMode;
    }

    public List<AccessRight> getAccessRights() {
        return accessRights;
    }

    public InteractMode getInteractMode() {
        return interactMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<AccessRight> accessRights = new ArrayList<>();
        private InteractMode interactMode;

        private Builder() {
        }

        public Builder addAccessRight(AccessRight accessRight) {
            this.accessRights.add(accessRight);
            return this;
        }

        public Builder accessRights(List<AccessRight> accessRights) {
            this.accessRights = new ArrayList<>(accessRights);
            return this;
        }

        public Builder interactMode(InteractMode interactMode) {
            this.interactMode = interactMode;
            return this;
        }

        public GrantRequest build() {
            return new GrantRequest(this);
        }
    }

    /**
     * Defines how user interaction should be handled in the grant flow.
     */
    public enum InteractMode {
        /** Redirect the user to a web page for interaction */
        REDIRECT,
        /** No interaction required (client credentials flow) */
        NONE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GrantRequest that = (GrantRequest) o;
        return Objects.equals(accessRights, that.accessRights) && interactMode == that.interactMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessRights, interactMode);
    }
}
