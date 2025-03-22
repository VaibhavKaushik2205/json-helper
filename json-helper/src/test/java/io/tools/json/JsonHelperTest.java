package io.tools.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonHelperTest {

    static class User {
        public String name;
        public int age;
        public List<String> roles;
        public Map<String, String> metadata;

        public User() {
        }

        public User(String name, int age, List<String> roles, Map<String, String> metadata) {
            this.name = name;
            this.age = age;
            this.roles = roles;
            this.metadata = metadata;
        }
    }

    @Test
    void testMergeSimpleFields() {
        User original = new User("John", 25, Collections.emptyList(), new HashMap<>());
        User patch = new User(null, 30, Collections.emptyList(), null);

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

        User original = new User("Alice", 28, Collections.emptyList(), originalMetadata);
        User patch = new User(null, 39, Collections.emptyList(), patchMetadata);

        User mergedUser = JsonHelper.patch(original, patch, User.class);

        assertEquals("Alice", mergedUser.name);
        assertEquals(39, mergedUser.age);
        assertEquals("admin", mergedUser.metadata.get("role"));
        assertEquals("engineering", mergedUser.metadata.get("department"));
    }

    @Test
    void testMergeArrayFields() {
        User target = new User("Alice", 28, List.of("admin"),
            Map.of("team", "engineering"));
        User source = new User(null, 0, List.of("developer"), Map.of());

        User mergedUser = JsonHelper.patch(target, source, User.class);

        System.out.println(mergedUser.roles);
        assertTrue(mergedUser.roles.contains("admin"));
        assertTrue(mergedUser.roles.contains("developer"));
        assertEquals(2, mergedUser.roles.size());
    }
}

