package am.trade.common.models.enums;

/**
 * Types of fields that can be used in journal templates
 */
public enum TemplateFieldType {
    TEXT("Text Input"),
    TEXTAREA("Text Area"),
    CHECKBOX("Checkbox"),
    CHECKBOX_LIST("Checkbox List"),
    DROPDOWN("Dropdown"),
    DATE("Date Picker"),
    TIME("Time Picker"),
    NUMBER("Number Input"),
    IMAGE_UPLOAD("Image Upload");

    private final String displayName;

    TemplateFieldType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
