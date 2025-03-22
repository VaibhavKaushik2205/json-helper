package io.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for merging non-null values recursively from a source object into a target object.
 * Handles nested objects, arrays, and maps within the JSON structure.
 * Ensures that existing values in the target object remain unchanged if null values are encountered in the source.
 * Supports deep merging for lists and arrays.
 */
public class JsonHelper {

    /**
     * ObjectMapper instance for handling JSON serialization and deserialization.
     * - `findAndRegisterModules()`: Automatically registers available Jackson modules (e.g., Java 8 time module, Kotlin module).
     * - `setSerializationInclusion(JsonInclude.Include.NON_NULL)`: Ensures that null fields are not serialized, reducing payload size.
     * - `configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)`: Ensures date fields are serialized as ISO-8601 strings instead of timestamps.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    /**
     * Merges a patch object into a target object, preserving existing values if patch fields are null.
     *
     * @param target The original object to be updated.
     * @param patch The object containing updates.
     * @param type  The class type of the target object.
     * @return The merged object.
     */
    public static <T> T patch(T target, T patch, Class<T> type) {
        if (target == null || patch == null) {
            throw new IllegalArgumentException("Original object and patch must be non-null");
        }
        try {
            JsonNode originalNode = objectMapper.valueToTree(target);
            JsonNode patchNode = objectMapper.valueToTree(patch);
            JsonNode mergedNode = mergeJsonNode(patchNode, originalNode);
            return objectMapper.treeToValue(mergedNode, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error merging JSON: " + e.getMessage());
        }
    }

    /**
     * Merges non-null values from patch JSON node into original JSON node.
     */
    private static JsonNode mergeJsonNode(JsonNode patch, JsonNode original) {
        if (patch == null || original == null || !patch.isObject() || !original.isObject()) {
            throw new IllegalArgumentException("Patch and original must be non-null JSON objects.");
        }
        patch.fields().forEachRemaining(entry -> {
            if (!entry.getValue().isNull()) {
                if (!entry.getValue().isObject() && !entry.getValue().isArray()) {
                    ((ObjectNode) original).set(entry.getKey(), entry.getValue());
                } else {
                    mergeJsonNodeField(entry.getKey(), entry.getValue(), original);
                }
            }
        });
        return original;
    }

    /**
     * Determines whether the field should be merged as an array or an object.
     */
    private static void mergeJsonNodeField(String fieldName, JsonNode patchNode, JsonNode originalNode) {
        if (patchNode.isArray()) {
            mergeArrayField(fieldName, (ArrayNode) patchNode, (ObjectNode) originalNode);
        } else if (patchNode.isObject()) {
            mergeObjectField(fieldName, patchNode, originalNode);
        }
    }

    /**
     * Merges an array field by adding new elements instead of replacing the entire array.
     */
    private static void mergeArrayField(String fieldName, ArrayNode patchArray, ObjectNode originalNode) {
        ArrayNode originalArray = (ArrayNode) originalNode.get(fieldName);
        if (originalArray == null || !originalArray.isArray()) {
            originalArray = originalNode.putArray(fieldName);
        }
        for (JsonNode element : patchArray) {
            if (element != null && !element.isNull()) {
                if (element.isObject()) {
                    mergeObjectField(fieldName, element, originalArray.addObject());
                } else if (element.isArray()) {
                    mergeArrayField(fieldName, (ArrayNode) element, originalArray.addObject());
                } else {
                    originalArray.add(element);
                }
            }
        }
    }

    /**
     * Merges a nested object field while preserving existing structure.
     */
    private static void mergeObjectField(String fieldName, JsonNode patchNode, JsonNode originalNode) {
        JsonNode originalField = originalNode.get(fieldName);
        if (originalField == null || !originalField.isObject()) {
            originalField = objectMapper.createObjectNode();
        }
        mergeJsonNode(patchNode, originalField);
        ((ObjectNode) originalNode).set(fieldName, originalField);
    }
}
