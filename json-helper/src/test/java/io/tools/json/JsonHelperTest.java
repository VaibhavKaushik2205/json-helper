package io.tools.json;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

class JsonHelperTest {

    static class User {
        public String name;
        public int age;
        public Map<String, String> metadata;

        public User() {
        }

        public User(String name, int age, Map<String, String> metadata) {
            this.name = name;
            this.age = age;
            this.metadata = metadata;
        }
    }

    @Test
    void testMergeSimpleFields() {
        User original = new User("John", 25, new HashMap<>());
        User patch = new User(null, 30, null);

        User mergedUser = JsonHelper.patch(original, patch, User.class);

        assertEquals("John", mergedUser.name); // Should retain existing value
        assertEquals(30, mergedUser.age); // Should update
    }

    @Test
    void testMergeNestedFields() {
        Map<String, String> originalMetadata = new HashMap<>();
        originalMetadata.put("role", "admin");

        Map<String, String> patchMetadata = new HashMap<>();
        patchMetadata.put("department", "engineering");

        User original = new User("Alice", 28, originalMetadata);
        User patch = new User(null, 39, patchMetadata);

        User mergedUser = JsonHelper.patch(original, patch, User.class);

        assertEquals("Alice", mergedUser.name);
        assertEquals(39, mergedUser.age);
        assertEquals("admin", mergedUser.metadata.get("role"));
        assertEquals("engineering", mergedUser.metadata.get("department"));
    }
}

