package io.tools.json.enums;

public enum ArrayMergeStrategy {
    APPEND_ARRAY,    // Append elements to arrays
    APPEND_UNIQUE,   // Append unique elements to arrays
    OVERWRITE       // Replace the original node with the patch node (including null)
}
