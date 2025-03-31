package io.tools.json;

import io.tools.json.enums.ArrayMergeStrategy;


public class MergeOptions {

    private ArrayMergeStrategy arrayMergeStrategy;  // Strategy for merging the arrays

    // Constructor
    public MergeOptions() {
        this.arrayMergeStrategy = ArrayMergeStrategy.APPEND_ARRAY;
    }

    public MergeOptions(ArrayMergeStrategy arrayMergeStrategy) {
        this.arrayMergeStrategy = arrayMergeStrategy;
    }

    // Getter
    public ArrayMergeStrategy getMergeStrategy() {
        return arrayMergeStrategy;
    }

    // Setter
    public void setMergeStrategy(ArrayMergeStrategy arrayMergeStrategy) {
        this.arrayMergeStrategy = arrayMergeStrategy;
    }
}