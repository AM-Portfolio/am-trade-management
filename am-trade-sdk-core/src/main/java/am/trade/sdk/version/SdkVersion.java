package am.trade.sdk.version;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SDK version enumeration.
 */
public enum SdkVersion {
    V1_0_0("1.0.0"),
    V1_1_0("1.1.0"),
    V2_0_0("2.0.0");

    private final String version;

    SdkVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return version;
    }
}
