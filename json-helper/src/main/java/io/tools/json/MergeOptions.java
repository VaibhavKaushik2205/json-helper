/*
 * Copyright (c) 2024 Vaibhav Kaushik
 * All rights reserved.
 *
 */
package io.tools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.tools.json.enums.MergeStrategy;
import java.util.function.BiPredicate;

public class MergeOptions {

  MergeStrategy arrayMergeStrategy = MergeStrategy.APPEND;
  MergeStrategy objectMergeStrategy = MergeStrategy.OVERWRITE;
  boolean IGNORE_EMPTY_STRINGS = true;

  BiPredicate<ArrayNode, JsonNode> customMergeStrategy = null;

  public MergeOptions setArrayMergeStrategy(MergeStrategy strategy) {
    this.arrayMergeStrategy = strategy;
    return this;
  }

  public MergeOptions setObjectMergeStrategy(MergeStrategy strategy) {
    this.objectMergeStrategy = strategy;
    return this;
  }

  public MergeOptions setIgnoreEmptyStrings(boolean ignoreEmptyStrings) {
    this.IGNORE_EMPTY_STRINGS = ignoreEmptyStrings;
    return this;
  }

  public MergeOptions setCustomMergeStrategy(BiPredicate<ArrayNode, JsonNode> strategy) {
    this.customMergeStrategy = strategy;
    return this;
  }

  public MergeStrategy getArrayMergeStrategy() {
    return this.arrayMergeStrategy;
  }

  public MergeStrategy getObjectMergeStrategy() {
    return this.objectMergeStrategy;
  }

  public boolean getIgnoreEmptyStrings() {
    return this.IGNORE_EMPTY_STRINGS;
  }
}
