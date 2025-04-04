/*
 * Copyright (c) 2024 Vaibhav Kaushik
 * All rights reserved.
 *
 */
package io.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.tools.json.enums.MergeStrategy;
import java.util.function.BiPredicate;

/**
 * Utility class for merging non-null values recursively from a source object into a target object.
 * Handles nested objects, arrays, and maps within the JSON structure. Ensures that existing values
 * in the target object remain unchanged if null values are encountered in the source. Supports deep
 * merging for lists and arrays.
 */
public class JsonHelper {

  /**
   * ObjectMapper instance for handling JSON serialization and deserialization. -
   * `findAndRegisterModules()`: Automatically registers available Jackson modules (e.g., Java 8
   * time module, Kotlin module). - `setSerializationInclusion(JsonInclude.Include.NON_NULL)`:
   * Ensures that null fields are not serialized, reducing payload size. -
   * `configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)`: Ensures date fields are
   * serialized as ISO-8601 strings instead of timestamps.
   */
  private static final ObjectMapper objectMapper =
      new ObjectMapper()
          .findAndRegisterModules()
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  // MergeOptions to control the merge behavior
  private MergeOptions mergeOptions;

  public JsonHelper() {
    // Default MergeOptions
    this.mergeOptions = new MergeOptions();
  }

  // Fluent setter for ArrayMergeStrategy
  public JsonHelper setArrayMergeStrategy(MergeStrategy mergeStrategy) {
    this.mergeOptions.setArrayMergeStrategy(mergeStrategy);
    return this;
  }

  // Fluent setter for ObjectMergeStrategy
  public JsonHelper setObjectMergeStrategy(MergeStrategy mergeStrategy) {
    this.mergeOptions.setObjectMergeStrategy(mergeStrategy);
    return this;
  }

  // Fluent setter for ObjectMergeStrategy
  public JsonHelper setIgnoreEmptyStrings(boolean ignoreEmptyStrings) {
    this.mergeOptions.setIgnoreEmptyStrings(ignoreEmptyStrings);
    return this;
  }

  // Fluent setter for custom merge strategy (if needed)
  public JsonHelper setCustomMergeStrategy(BiPredicate<ArrayNode, JsonNode> customMergeStrategy) {
    this.mergeOptions.setCustomMergeStrategy(customMergeStrategy);
    return this;
  }

  /**
   * Merges a patch object into a target object, preserving existing values if patch fields are
   * null.
   *
   * @param target The original object to be updated.
   * @param patch The object containing updates.
   * @param type The class type of the target object.
   * @return The merged object.
   */
  public <T> T patch(T target, T patch, Class<T> type) {
    return patch(target, patch, type, mergeOptions);
  }

  private <T> T patch(T target, T patch, Class<T> type, MergeOptions options) {
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

  /** Merges non-null values from patch JSON node into original JSON node. */
  private JsonNode mergeJsonNode(JsonNode patch, JsonNode original, MergeOptions options) {
    if (patch == null || original == null || !patch.isObject() || !original.isObject()) {
      throw new IllegalArgumentException("Patch and original must be non-null JSON objects.");
    }
    patch
        .fields()
        .forEachRemaining(
            entry -> {
              // Proceed if current field is not-null
              if (!entry.getValue().isNull()) {
                if (!entry.getValue().isObject() && !entry.getValue().isArray()) {
                  // return if the string is empty
                  if (entry.getValue().asText().isBlank() && options.getIgnoreEmptyStrings()) {
                    return;
                  }
                  ((ObjectNode) original).set(entry.getKey(), entry.getValue());
                } else {
                  mergeJsonNodeField(entry.getKey(), entry.getValue(), original, options);
                }
              }
            });
    return original;
  }

  /** Determines whether the field should be merged as an array or an object. */
  private void mergeJsonNodeField(
      String fieldName, JsonNode patchNode, JsonNode originalNode, MergeOptions options) {
    if (patchNode.isArray()) {
      mergeArrayField(fieldName, (ArrayNode) patchNode, (ObjectNode) originalNode, options);
    } else if (patchNode.isObject()) {
      mergeObjectField(fieldName, patchNode, originalNode, options);
    }
  }

  /** Merges an array field by adding new elements instead of replacing the entire array. */
  private void mergeArrayField(
      String fieldName, ArrayNode patchArray, ObjectNode originalNode, MergeOptions options) {
    if (options.getArrayMergeStrategy() == MergeStrategy.OVERWRITE) {
      // Replace the entire array
      originalNode.set(fieldName, patchArray);
      return;
    }

    ArrayNode originalArray = (ArrayNode) originalNode.get(fieldName);
    if (originalArray == null || !originalArray.isArray()) {
      originalArray = originalNode.putArray(fieldName);
    }

    for (JsonNode element : patchArray) {
      if (element != null && !element.isNull()) {
        if (options.getArrayMergeStrategy() == MergeStrategy.UNIQUE
            && !containsNode(originalArray, element)) {
          originalArray.add(element);
        } else if (options.getArrayMergeStrategy() == MergeStrategy.APPEND) {
          originalArray.add(element);
        } else if (options.getArrayMergeStrategy() == MergeStrategy.DEEP_MERGE) {
          throw new IllegalArgumentException("List elements do not support Deep Merge");
        }
      }
    }
  }

  private boolean containsNode(ArrayNode array, JsonNode node) {
    for (JsonNode element : array) {
      if (element.equals(node)) {
        return true;
      }
    }
    return false;
  }

  /** Merges a nested object field while preserving existing structure. */
  private void mergeObjectField(
      String fieldName, JsonNode patchNode, JsonNode originalNode, MergeOptions options) {
    JsonNode originalField = originalNode.get(fieldName);
    if (originalField == null || !originalField.isObject()) {
      originalField = objectMapper.createObjectNode();
    }
    if (options.getObjectMergeStrategy() == MergeStrategy.OVERWRITE) {
      ((ObjectNode) originalNode).set(fieldName, patchNode);
      return;
    }
    mergeJsonNode(patchNode, originalField, options);
    ((ObjectNode) originalNode).set(fieldName, originalField);
  }
}
