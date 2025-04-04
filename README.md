# JsonHelper

JsonHelper is a lightweight Java utility that helps perform **deep merges** between Java objects using JSON serialization. It provides fine-grained control over how fields and arrays are merged, making it useful for patch operations, updates, and partial overrides.

---

## 🚀 Features

- Deep merge Java objects via JSON.
- Supports custom merge strategies for:
    - Arrays: `APPEND`, `UNIQUE`, `OVERWRITE`
    - Objects: `OVERWRITE`, `DEEP_MERGE`
- Skip nulls or empty values.
- Works with nested structures, maps, lists, etc.

---

## 📦 Installation

### 1. Clone the Repo
```bash
git clone https://github.com/VaibhavKaushik2205/json-helper.git
```

### 2. Build & Use JAR
```bash
./gradlew build
```
You’ll find the output JAR inside `build/libs/`. You can then import it manually in your project.

### 3. Use as Source Code
If you're not using it as a jar dependency, you can:

> 🛠️ Copy the `io.tools.json` package (from `src/main/java`) into your own project's source directory.

---

## 🧪 Example Usage

```java
User target = new User("Alice", 28, List.of("admin", "dev"), Map.of("team", "alpha"));
User patch = new User(null, 30, List.of("admin", "security"), Map.of("location", "NY"));

JsonHelper jsonHelper = new JsonHelper().setArrayMergeStrategy(ArrayMergeStrategy.UNIQUE).setObjectMergeStrategy(ObjectMergeStrategy.DEEP_MERGE).setIgnoreEmptyStrings(false);
User merged = jsonHelper.patch(target, patch, User.class);

output:
{
  "name": "Alice",
  "age": 30,
  "roles": ["admin", "dev", "security"],
  "attributes": {
    "team": "alpha",
    "location": "NY"
  }
}
```

---

## 🧠 MergeOptions Explained

| Option | Description |
|--------|-------------|
| `arrayMergeStrategy` | Strategy for merging arrays: `APPEND`, `UNIQUE`, `OVERWRITE` |
| `objectMergeStrategy` | Strategy for merging objects: `OVERWRITE`, `DEEP_MERGE` |
| `ignoreEmptyStrings` | If true, ignores empty strings in the patch |

---

## 📚 Tests
- Located in: `src/test/java/io/tools/json/JsonHelperTest.java`
- Covers:
    - Simple merge
    - Nested object merging
    - Array merging strategies

---

## ✨ Future Ideas
- Publish to Maven Central
- Add annotations to configure merge behavior per-field
- Add support for merging POJO collections by ID

---


## 🤝 Contributing
Pull requests are welcome! Feel free to open issues or suggest features.

---

## 🙌 Credits
Developed by [Vaibhav Kaushik](https://github.com/VaibhavKaushik2205).
