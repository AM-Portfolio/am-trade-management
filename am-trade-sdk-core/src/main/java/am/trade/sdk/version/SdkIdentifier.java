package am.trade.sdk.version;

import java.util.HashMap;
import java.util.Map;

/**
 * SDK identifier for backend tracking and authentication.
 * All SDK requests include these headers.
 */
public class SdkIdentifier {

    public static final String SDK_NAME = "am-trade-sdk-java";
    public static final String SDK_TYPE = "SDK";
    public static final String LANGUAGE = "java";

    /**
     * Get SDK identification headers for API requests.
     *
     * @param version SDK version
     * @return Map of headers to add to requests
     */
    public static Map<String, String> getSdkHeader(String version) {
        if (version == null) {
            version = VersionMetadata.getCurrentVersion();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-SDK-Name", SDK_NAME);
        headers.put("X-SDK-Version", version);
        headers.put("X-SDK-Type", SDK_TYPE);
        headers.put("X-SDK-Language", LANGUAGE);

        return headers;
    }

    /**
     * Get complete SDK metadata.
     *
     * @param version SDK version
     * @return Map with SDK metadata
     */
    public static Map<String, String> getSdkMetadata(String version) {
        if (version == null) {
            version = VersionMetadata.getCurrentVersion();
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("sdk_name", SDK_NAME);
        metadata.put("sdk_version", version);
        metadata.put("sdk_type", SDK_TYPE);
        metadata.put("language", LANGUAGE);

        return metadata;
    }
}
