# Json

[ ![Download](https://api.bintray.com/packages/drummer-aidan/maven/json/images/download.svg) ](https://bintray.com/drummer-aidan/maven/json/_latestVersion)
[![Build Status](https://travis-ci.org/afollestad/json.svg)](https://travis-ci.org/afollestad/json)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

This library intends to make JSON very easy to interact with in Java; it also makes (de)serialization painless.
 
It wraps around the well-known `org.json` classes (`JSONObject`, `JSONArray`, etc.) which also happen to be included 
in the Android SDK. As we all know, those stock classes tend to be a pain. They feel bulky, and make you try/catch 
*way* too many Exceptions.

---

# Dependency

The dependency is available via jCenter.

### Gradle (Java)

```Gradle
dependencies {
    ...
    compile 'com.afollestad:json:0.1.1'
}
```

### Gradle (Android)

Since Android includes `org.json` classes, you'll want to exclude the copies provided by this library:

```Gradle
dependencies {
    ...
    compile('com.afollestad:json:0.1.1') {
        exclude group: 'org.json', module: 'json'
    }
}
```

### Maven

```xml
<dependency>
  <groupId>com.afollestad</groupId>
  <artifactId>json</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

---

# Parsing and Building Objects

There are various ways that this library allows you to construct JSON objects...

Parsing strings is the first, just use the constructor which accepts a `String`:

```java
String input = // ...
Json json = new Json(input);
```

Second, you can build objects using Java fields:

```java
// Translates to {"id":1,"name":"Aidan","born":1995}
Json json = new Json() {
    int id = 1;
    String name = "Aidan";
    int born = 1995;
};
```

Third, you can add values with the `put()` method:

```java
Json json = new Json()
    .put("_id", 1)
    .put("name", "Aidan")
    .put("born", 1995);
```

You can quickly put in arrays just by passing multiple values to `put()`:

```java
// Translates to {"greetings":["Hello","Hey"]}
Json json = new Json();
// The first parameter is a key, you can pass any type for the rest of the varargs parameters
json.put("greetings", "Hello", "World");
```

---

# Retrieving Values from Objects

Various methods exist for retrieving existing values (default values are returned if they don't exist). The one parameter 
version uses whatever the usual default of a type is (0 for number types, null for everything else), if the no value is found 
for the key. The two parameter version lets you specify a custom default.

```java
Json json = // ...

String str = json.getString("name");
String strWithDefault = json.getString("name", null);

boolean bool = json.getBool("name");
boolean boolWithDefault = json.getBool("name", true);

short shrt = json.getShort("name");
short shrtWithDefault = json.getShort("name", (short)0);

int integer = json.getInt("name");
int integerWithDefault = json.getInt("name", 0);

long lng = json.getLong("name");
long lngWithDefault = json.getLong("name", 0L);

float flt = json.getFloat("name");
float fltWithDefault = json.getFloat("name", 0f);

double doub = json.getDouble("name");
double doubWithDefault = json.getDouble("name", 0d);

Json obj = json.getJsonObject("name");
JsonArray ary = json.getJsonArray("name");
```

Further, the `get(String)` method will actually automatically cast its return value to whatever variable you're setting it to:

```java
String str = json.get("name");
long lng = json.get("name");
```

It will also infer its type if you pass a default value, removing the need to use explicit `get[Type]` methods:

```java
if (json.get("name", false)) {
    // do something
}
```

---

You can check if values exist, are null, equal another value, or even remove them by key:

```java
Json json = // ...

boolean exists = json.has("name");
boolean isNull = json.isNull("name");
boolean valueEqual = json.equal("key-name", value);
json.remove("name");
```

---

# Parsing and Building Arrays

Like objects, you can parse arrays from Strings:

```java
String input = // ...
JsonArray<Json> array = new JsonArray<Json>(input);
```

You can add new objects with `.add()`:

```java
JsonArray<String> array = new JsonArray<String>();
// You can add multiple items with a single .put() call, you could use multiple if necessary too
array.add("Hello", "World!");
```

You can retrieve and remove objects by index:

```java
JsonArray<Json> array = // ...

Json firstItem = array.get(0);
array.remove(0);
```

Some other utility methods exist, also:

```java
JsonArray<String> array = // ...

int size = array.size();
boolean empty = array.isEmpty();
boolean itemEqual = array.equal(0, "Does index 0 equal this value?")
```

---

# Pretty Print

Objects and arrays can both be converted to strings simply with the `toString()` method:

```java
Json json = // ...

String value = json.toString(); // all on one line, no formatting
String formatted = json.toString(4); // 4 spaces being the indent size
```

---

# Paths 

Paths let you quickly add, retrieve, or remove items which are deeper down in your JSON hierarchy without manually 
traversing.

Lets create an object using path keys:

```java
Json json = new Json()
    .put("id", 1)
    .put("name", "Aidan")
    .put("birthday.month", "July")
    .put("birthday.day", 28)
    .put("birthday.year", 1995);
```

The above would construct this:

```json
{
    "id": 1,
    "name": "Aidan",
    "birthday": {
        "month": "July",
        "day": 28,
        "year": 1995
    }
}
```

As you can see, a child object is automatically created for you. We only use two levels, but you could create many more 
 just by using more periods to separate child names.

We can use this same dot notation to retrieve these child values:

```java
Json json = // ...

String name = json.get("name");
String month = json.get("birthday.month");
int day = json.get("birthday.day");
int year = json.get("birthday.year");
```

You can quickly check equality in objects...

```java
Json json = // ...
boolean birthYearCheck = json.equal("birthday.year", 1995);
```

And arrays:

```java
Json json = new Json()
    .put("id", 1)
    .put("name", "Aidan")
    .put("birthday.month", "July")
    .put("birthday.day", 28)
    .put("birthday.year", 1995);
JsonArray<Json> array = JsonArray<Json>();
array.put(json);

// The first parameter is the index of the item, the second is a key path, the third is the value you're comparing to
boolean firstItemBirthYearCheck = array.equal(0, "birthday.year", 1995);
```

---

# Serialization

This library allows very easy serialization and deserialization. Serialization is converting a Java class instance 
to JSON.

Take this class for the coming set of examples:

```java
public class Person {

    public int id;
    public String name;
    public Person spouse;
    
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### The JsonSerializer

We can serialize an instance as follows:

```java
Person aidan = new Person(1, "Aidan");
aidan.spouse = new Person(2, "Waverly");

JsonSerializer serializer = JsonSerializer.get();
Json json = serializer.serialize(aidan);
```

The resulting `Json` object is:

```json
{
    "id": 1,
    "name": "Aidan",
    "spouse": {
        "id": 2,
        "name": "Waverly"
    }
}
```

Notice that the `Person` instance inside of parent `Person` instance is automatically serialized too.

In addition to objects, you can serialize arrays and lists:

```java
JsonSerializer serializer = JsonSerializer.get();

Person[] people = // ...
JsonArray<Person> array = serializer.serializeArray(people);

List<Person> people2 = // ...
JsonArray<Person> array2 = serializer.serializeList(people2);
```

### Automatic Serialization

If you already have a `Json` instance, you can add Java class instances into the object and serialize them automatically:

```java
Json json = new Json();
Person person1 = new Person(1, "Aidan");
Person person2 = new Person(2, "Waverly");
json.put("person1", person);
json.put("person2", person2);
```

This would result in:

```json
{
    "person1": {
        "id": 1,
        "name": "Aidan"
    },
    "person2": {
        "id": 2,
        "name": "Waverly"
    }
}
```

This automatic serialization works with `JsonArray`'s too:

```java
JsonArray<Person> array = new JsonArray<Person>();
Person person1 = new Person(1, "Aidan");
Person person2 = new Person(2, "Waverly");
array.add(person1, person2);
```

This would result in:

```json
[
    {
        "id": 1,
        "name": "Aidan"
    },
    {
        "id": 2,
        "name": "Waverly"
    }
]
```

---

# Deserialization

This library allows very easy serialization and deserialization. Deserialization is converting JSON to a Java class instance.

Again, take this class for the coming set of examples:

```java
public class Person {

    public int id;
    public String name;
    public Person spouse;
    
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

### The JsonSerializer

We can deserialize an object as follows:

```java
String input = "{\"id\":1,\"name\":\"Aidan\",\"spouse\":{\"id\":2,\"name\":\"Waverly\"}}";
Json json = new Json(input);

JsonSerializer serializer = JsonSerializer.get();
Person person = serializer.deserialize(json, Person.class);
```

The ID, name, and spouse object would all be populated to match the values in the JSON string.

And of course, you can deserialize JSON arrays to lists and arrays too:

```java
JsonSerializer serializer = JsonSerializer.get();
JsonArray<Person> array = // ...

Person[] peopleArray = serializer.deserializeArray(array, Person.class);
List<Person> peopleList = serializer.deserializeList(array, Person.class);
```

### Automatic Deserialization

If you already have a `Json` instance, you can automatically pull out and deserialize Java class instances without 
using the `JsonSerializer` directly:

```java
Json json = // ...
// The JSON object needs to contain a child object with the key "person" representing the Person class.
Person person = json.get("person", Person.class);
```

The same works for `JsonArray`'s:

```java
JsonArray<Person> array = // ...
// The JSON array needs to contain a list of objects representing the Person class.
Person person = array.get(0, Person.class);
```

---

# Annotations

This library comes with a two annotations that have their own special use cases.

### @JsonName

This annotation allows you to assign a custom name to fields.

It's used with `Json` field construction:

```java
Json json = new Json() {
    @JsonName(name = "_id") int id = 1;
    String name = "Aidan";
    int born = 1995;
};
```

And of course during serialization and deserialization:

```java
public class Person {
    
    @JsonName(name = "_id") int id;
    String name;
    int born;
    
    public Person(int id, String name, int born) {
        this.id = id;
        this.name = name;
        this.born = born;
    }
}
```

### @JsonIgnore

This annotation tells the library to ignore and fields it's used to mark. That means the field is not serialized, 
deserialized, or used with field construction:

```java
public class Person {
    
    @JsonName(name = "_id") int id;
    String name;
    int born;
    
    @JsonIgnore String invisibleField = "Hello, world!";
    
    public Person(int id, String name, int born) {
        this.id = id;
        this.name = name;
        this.born = born;
    }
}
```
