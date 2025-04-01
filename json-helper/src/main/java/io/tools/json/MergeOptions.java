package io.tools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.tools.json.enums.MergeStrategy;

import java.util.function.BiPredicate;


public class MergeOptions {

    MergeStrategy arrayMergeStrategy = MergeStrategy.APPEND;
    MergeStrategy objectMergeStrategy = MergeStrategy.OVERWRITE;

    BiPredicate<ArrayNode, JsonNode> customMergeStrategy = null;

    public MergeOptions setArrayMergeStrategy(MergeStrategy strategy) {
        this.arrayMergeStrategy = strategy;
        return this;
    }

    public MergeOptions setObjectMergeStrategy(MergeStrategy strategy) {
        this.objectMergeStrategy = strategy;
        return this;
    }

    public MergeOptions setCustomMergeStrategy(BiPredicate<ArrayNode, JsonNode> strategy) {
        this.customMergeStrategy = strategy;
        return this;
    }
}