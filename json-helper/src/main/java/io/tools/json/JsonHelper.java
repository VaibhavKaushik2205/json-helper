package io.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for merging non-null values recursively from a patch object into an original object.
 * Handles nested objects, arrays, and maps within the JSON structure.
 * Ensures that existing values in the original object remain unchanged if null values are encountered in the patch.
 * Compatible with DTOs containing collections (lists) and maps.
 */
public class JsonHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static <T> T patch(T original, T patch, Class<T> type) {
        if (original == null || patch == null) {
            throw new IllegalArgumentException("Original object and patch must be non-null");
        }
        try {
            JsonNode originalNode = objectMapper.valueToTree(original);
            JsonNode patchNode = objectMapper.valueToTree(patch);
            JsonNode mergedNode = mergeJsonNode(patchNode, originalNode);
            return objectMapper.treeToValue(mergedNode, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error merging JSON: " + e.getMessage());
        }
    }

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

    private static void mergeJsonNodeField(String fieldName, JsonNode patchNode, JsonNode originalNode) {
        if (patchNode.isArray()) {
            mergeArrayField(fieldName, (ArrayNode) patchNode, (ObjectNode) originalNode);
        } else if (patchNode.isObject()) {
            mergeObjectField(fieldName, patchNode, originalNode);
        }
    }

    private static void mergeArrayField(String fieldName, ArrayNode patchArray, ObjectNode originalNode) {
        ArrayNode originalArray = originalNode.putArray(fieldName);
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

    private static void mergeObjectField(String fieldName, JsonNode patchNode, JsonNode originalNode) {
        JsonNode originalField = originalNode.get(fieldName);
        if (originalField == null || !originalField.isObject()) {
            originalField = objectMapper.createObjectNode();
        }
        mergeJsonNode(patchNode, originalField);
        ((ObjectNode) originalNode).set(fieldName, originalField);
    }
}
