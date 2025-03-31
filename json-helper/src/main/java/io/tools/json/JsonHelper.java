package io.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.tools.json.enums.ArrayMergeStrategy;

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
        return patch(target, patch, type, new MergeOptions());
    }


    public static <T> T patch(T target, T patch, Class<T> type, MergeOptions options) {
        if (target == null || patch == null) {
            throw new IllegalArgumentException("Original object and patch must be non-null");
        }
        try {
            JsonNode originalNode = objectMapper.valueToTree(target);
            JsonNode patchNode = objectMapper.valueToTree(patch);
            JsonNode mergedNode = mergeJsonNode(patchNode, originalNode, options);
            return objectMapper.treeToValue(mergedNode, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error merging JSON: " + e.getMessage());
        }
    }

    /**
     * Merges non-null values from patch JSON node into original JSON node.
     */
    private static JsonNode mergeJsonNode(JsonNode patch, JsonNode original, MergeOptions options) {
        ObjectNode targetNode = (ObjectNode) original;
        patch.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode patchValue = entry.getValue();
            JsonNode originalValue = targetNode.get(fieldName);

            // If patch value is null, skip it
            if (patchValue.isNull()) {
                return;
            }

            // Handling based on field type
            if (patchValue.isObject()) {
                mergeObjectField(fieldName, patchValue, originalValue, targetNode, options);
            } else if (patchValue.isArray()) {
                mergeArrayField(fieldName, (ArrayNode) patchValue, targetNode, options);
            } else {
                // Overwrite primitive value
                targetNode.set(fieldName, patchValue);
            }
        });
        return targetNode;
    }

    /**
     * Handles merging of nested object fields.
     */
    private static void mergeObjectField(String fieldName, JsonNode patchNode, JsonNode originalValue, ObjectNode targetNode, MergeOptions options) {
        // Overwrite if no existing object
        if (originalValue == null || !originalValue.isObject()) {
            targetNode.set(fieldName, patchNode);
        } else {
            // Deep merge
            JsonNode mergedNode = mergeJsonNode(patchNode, originalValue, options);
            targetNode.set(fieldName, mergedNode);
        }
    }

    /**
     * Handles merging of array fields based on strategy.
     */
    private static void mergeArrayField(String fieldName, ArrayNode patchArray, ObjectNode targetNode, MergeOptions options) {
        JsonNode existingArray = targetNode.get(fieldName);

        // If there's no existing array or the strategy is OVERWRITE, replace it entirely
        if (existingArray == null || !existingArray.isArray()) {
            // If the existing array is null or not an array, overwrite it with the patch array
            targetNode.set(fieldName, patchArray);
            return;
        }

        // return if patch array is empty
        if (patchArray.isEmpty()) {
            return;
        }
        ArrayNode originalArray = (ArrayNode) existingArray;

        if (ArrayMergeStrategy.OVERWRITE.equals(options.getMergeStrategy())) {
            // Replace the entire array with the patch array
            targetNode.set(fieldName, patchArray);
            return;
        }
        else if (options.getMergeStrategy() == ArrayMergeStrategy.APPEND_ARRAY) {
            // Append new elements to the array
            for (JsonNode element : patchArray) {
                originalArray.add(element);
            }
        }
        else if (options.getMergeStrategy() == ArrayMergeStrategy.APPEND_UNIQUE) {
            // Handle append unique array strategy
            for (JsonNode element : patchArray) {
                // Only add if element is not already present
                if (!arrayContains(originalArray, element)) {
                    originalArray.add(element);
                }
            }
        }
        // Save the merged array back into the target node
        targetNode.set(fieldName, originalArray);
    }


    /**
     * Checks if an array already contains a given element.
     */
    private static boolean arrayContains(ArrayNode array, JsonNode element) {
        for (JsonNode node : array) {
            if (node.equals(element)) {
                return true;
            }
        }
        return false;
    }

}
