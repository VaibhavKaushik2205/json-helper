package io.tools.json;

import static org.junit.jupiter.api.Assertions.*;

import io.tools.json.enums.ArrayMergeStrategy;
import org.junit.jupiter.api.BeforeEach;
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

    MergeOptions options;
    @BeforeEach
    public void setup() {
        options = new MergeOptions();
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
        User target = new User("Alice", 28, List.of("admin", "architect"), Map.of());
        User source = new User(null, 0, List.of("developer"), Map.of());

        User mergedUser = JsonHelper.patch(target, source, User.class);

        System.out.println(mergedUser.roles);
        assertTrue(mergedUser.roles.contains("admin"));
        assertTrue(mergedUser.roles.contains("architect"));
        assertTrue(mergedUser.roles.contains("developer"));
        assertEquals(3, mergedUser.roles.size());
    }

    @Test
    void testMergeUniqueArrayFields() {
        User target = new User("Alice", 28, List.of("admin", "developer"), Map.of());
        User source = new User(null, 0, List.of("admin"), Map.of());

        options.setArrayMergeStrategy(ArrayMergeStrategy.UNIQUE);
        User mergedUser = JsonHelper.patch(target, source, User.class, options);

        assertTrue(mergedUser.roles.contains("admin"));
        assertTrue(mergedUser.roles.contains("developer"));
        assertEquals(2, mergedUser.roles.size());
    }

    @Test
    void testMergeAppendArrayFields() {
        User target = new User("Alice", 28, List.of("admin", "developer"), Map.of());
        User source = new User(null, 0, List.of("admin"), Map.of());

        options.setArrayMergeStrategy(ArrayMergeStrategy.APPEND);
        User mergedUser = JsonHelper.patch(target, source, User.class, options);

        long adminCount = mergedUser.roles.stream().filter(role -> role.equals("admin")).count();
        assertEquals(2, adminCount);
        assertTrue(mergedUser.roles.contains("developer"));
        assertEquals(3, mergedUser.roles.size());
    }

    @Test
    void testMergeOverwriteArrayFields() {
        User target = new User("Alice", 28, List.of("admin", "developer"), Map.of());
        User source = new User(null, 0, List.of("architect"), Map.of());

        options.setArrayMergeStrategy(ArrayMergeStrategy.OVERWRITE);
        User mergedUser = JsonHelper.patch(target, source, User.class, options);

        assertTrue(mergedUser.roles.contains("architect"));
        assertFalse(mergedUser.roles.contains("admin"));
        assertFalse(mergedUser.roles.contains("developer"));
        assertEquals(1, mergedUser.roles.size());
    }
}

