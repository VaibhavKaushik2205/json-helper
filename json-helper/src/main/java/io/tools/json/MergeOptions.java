package io.tools.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.tools.json.enums.ArrayMergeStrategy;
import io.tools.json.enums.ObjectMergeStrategy;

import java.util.function.BiPredicate;


public class MergeOptions {

    ArrayMergeStrategy arrayMergeStrategy = ArrayMergeStrategy.UNIQUE;
    ObjectMergeStrategy objectMergeStrategy = ObjectMergeStrategy.OVERWRITE;

    BiPredicate<ArrayNode, JsonNode> customMergeStrategy = null;

    public MergeOptions setArrayMergeStrategy(ArrayMergeStrategy strategy) {
        this.arrayMergeStrategy = strategy;
        return this;
    }

    public MergeOptions setObjectMergeStrategy(ObjectMergeStrategy strategy) {
        this.objectMergeStrategy = strategy;
        return this;
    }

    public MergeOptions setCustomMergeStrategy(BiPredicate<ArrayNode, JsonNode> strategy) {
        this.customMergeStrategy = strategy;
        return this;
    }
}