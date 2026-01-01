package am.trade.sdk.version;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SDK version metadata and capabilities management.
 * Tracks version features and supports multiple SDK versions.
 */
@Slf4j
public class VersionMetadata {

    public static final SdkVersion CURRENT_VERSION = SdkVersion.V1_0_0;

    private static final Map<String, VersionInfo> VERSION_FEATURES = new HashMap<>();

    static {
        // Version 1.0.0 features
        VERSION_FEATURES.put("1.0.0", VersionInfo.builder()
                .version("1.0.0")
                .released("2025-12-05")
                .features(Arrays.asList(
                        "Basic CRUD operations",
                        "Trade management",
                        "Portfolio tracking",
                        "Analytics support",
                        "Journal entries",
                        "Filter management"
                ))
                .breakingChanges(Arrays.asList())
                .deprecatedEndpoints(Arrays.asList())
                .maxRetries(3)
                .timeout(30)
                .paginationMaxSize(100)
                .build());

        // Version 1.1.0 features
        VERSION_FEATURES.put("1.1.0", VersionInfo.builder()
                .version("1.1.0")
                .released("2026-01-15")
                .features(Arrays.asList(
                        "Advanced filtering",
                        "Batch operations",
                        "Export/import",
                        "Performance metrics"
                ))
                .breakingChanges(Arrays.asList())
                .deprecatedEndpoints(Arrays.asList())
                .maxRetries(5)
                .timeout(60)
                .paginationMaxSize(500)
                .build());

        // Version 2.0.0 features
        VERSION_FEATURES.put("2.0.0", VersionInfo.builder()
                .version("2.0.0")
                .released("2026-03-01")
                .features(Arrays.asList(
                        "WebSocket support",
                        "Real-time updates",
                        "Advanced AI analytics"
                ))
                .breakingChanges(Arrays.asList(
                        "Removed legacy endpoints",
                        "Changed response format"
                ))
                .deprecatedEndpoints(Arrays.asList("/api/v1/old-endpoint"))
                .maxRetries(5)
                .timeout(120)
                .paginationMaxSize(1000)
                .build());
    }

    public static String getCurrentVersion() {
        return CURRENT_VERSION.getVersion();
    }

    public static VersionInfo getVersionInfo(String version) {
        if (version == null) {
            version = getCurrentVersion();
        }
        return VERSION_FEATURES.getOrDefault(version, new VersionInfo());
    }

    public static VersionCapabilities getVersionCapabilities(String version) {
        if (version == null) {
            version = getCurrentVersion();
        }
        VersionInfo info = getVersionInfo(version);
        return VersionCapabilities.builder()
                .version(version)
                .maxRetries(info.getMaxRetries())
                .timeout(info.getTimeout())
                .paginationMaxSize(info.getPaginationMaxSize())
                .features(info.getFeatures())
                .breakingChanges(info.getBreakingChanges())
                .deprecatedEndpoints(info.getDeprecatedEndpoints())
                .build();
    }

    public static boolean isCompatible(String version) {
        return VERSION_FEATURES.containsKey(version);
    }

    public static List<String> getDeprecationWarnings(String version) {
        if (version == null) {
            version = getCurrentVersion();
        }
        VersionInfo info = getVersionInfo(version);
        return info.getDeprecatedEndpoints();
    }

    @Data
    public static class VersionInfo {
        private String version;
        private String released;
        private List<String> features;
        private List<String> breakingChanges;
        private List<String> deprecatedEndpoints;
        private int maxRetries;
        private int timeout;
        private int paginationMaxSize;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String version;
            private String released;
            private List<String> features;
            private List<String> breakingChanges;
            private List<String> deprecatedEndpoints;
            private int maxRetries = 3;
            private int timeout = 30;
            private int paginationMaxSize = 100;

            public Builder version(String version) {
                this.version = version;
                return this;
            }

            public Builder released(String released) {
                this.released = released;
                return this;
            }

            public Builder features(List<String> features) {
                this.features = features;
                return this;
            }

            public Builder breakingChanges(List<String> changes) {
                this.breakingChanges = changes;
                return this;
            }

            public Builder deprecatedEndpoints(List<String> endpoints) {
                this.deprecatedEndpoints = endpoints;
                return this;
            }

            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }

            public Builder timeout(int timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder paginationMaxSize(int size) {
                this.paginationMaxSize = size;
                return this;
            }

            public VersionInfo build() {
                VersionInfo info = new VersionInfo();
                info.version = this.version;
                info.released = this.released;
                info.features = this.features;
                info.breakingChanges = this.breakingChanges;
                info.deprecatedEndpoints = this.deprecatedEndpoints;
                info.maxRetries = this.maxRetries;
                info.timeout = this.timeout;
                info.paginationMaxSize = this.paginationMaxSize;
                return info;
            }
        }
    }

    @Data
    public static class VersionCapabilities {
        private String version;
        private int maxRetries;
        private int timeout;
        private int paginationMaxSize;
        private List<String> features;
        private List<String> breakingChanges;
        private List<String> deprecatedEndpoints;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String version;
            private int maxRetries;
            private int timeout;
            private int paginationMaxSize;
            private List<String> features;
            private List<String> breakingChanges;
            private List<String> deprecatedEndpoints;

            public Builder version(String version) {
                this.version = version;
                return this;
            }

            public Builder maxRetries(int retries) {
                this.maxRetries = retries;
                return this;
            }

            public Builder timeout(int timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder paginationMaxSize(int size) {
                this.paginationMaxSize = size;
                return this;
            }

            public Builder features(List<String> features) {
                this.features = features;
                return this;
            }

            public Builder breakingChanges(List<String> changes) {
                this.breakingChanges = changes;
                return this;
            }

            public Builder deprecatedEndpoints(List<String> endpoints) {
                this.deprecatedEndpoints = endpoints;
                return this;
            }

            public VersionCapabilities build() {
                VersionCapabilities cap = new VersionCapabilities();
                cap.version = this.version;
                cap.maxRetries = this.maxRetries;
                cap.timeout = this.timeout;
                cap.paginationMaxSize = this.paginationMaxSize;
                cap.features = this.features;
                cap.breakingChanges = this.breakingChanges;
                cap.deprecatedEndpoints = this.deprecatedEndpoints;
                return cap;
            }
        }
    }
}
