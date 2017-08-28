# Ason

[ ![Download](https://api.bintray.com/packages/drummer-aidan/maven/ason/images/download.svg) ](https://bintray.com/drummer-aidan/maven/ason/_latestVersion)
[![Build Status](https://travis-ci.org/afollestad/ason.svg)](https://travis-ci.org/afollestad/ason)
[![Codecov](https://img.shields.io/codecov/c/github/afollestad/ason.svg)](https://codecov.io/gh/afollestad/ason)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/4e54317135c441df974e41ba868a954c)](https://www.codacy.com/app/drummeraidan_50/ason?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=afollestad/ason&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

This library intends to make JSON very easy to interact with in Java; it also makes (de)serialization painless.
 
It wraps around the well-known `org.json` classes (`JSONObject`, `JSONArray`, etc.) which also happen to be included 
in the Android SDK. As we all know, those stock classes tend to be a pain. They feel bulky, and make you try/catch 
*way* too many Exceptions.

---

# Table of Contents

1. [Dependency](https://github.com/afollestad/ason#dependency)
    1. [Gradle (Java)](https://github.com/afollestad/ason#gradle-java)
    2. [Gradle (Android)](https://github.com/afollestad/ason#gradle-android)
    3. [Gradle (Kotlin)](https://github.com/afollestad/ason#gradle-kotlin)
    4. [Maven](https://github.com/afollestad/ason#maven)
2. [Parsing and Building Objects](https://github.com/afollestad/ason#parsing-and-building-objects)
3. [Retrieving Values from Objects](https://github.com/afollestad/ason#retrieving-values-from-objects)
4. [Parsing and Building Arrays](https://github.com/afollestad/ason#parsing-and-building-arrays)
5. [Pretty Print](https://github.com/afollestad/ason#pretty-print)
6. [Paths](https://github.com/afollestad/ason#paths)
    1. [Dot Notation](https://github.com/afollestad/ason#dot-notation)
    2. [Index Notation](https://github.com/afollestad/ason#index-notation)
    3. [Escaping Periods and Dollar Signs](https://github.com/afollestad/ason#escaping-periods-and-dollar-signs)
7. [Serialization](https://github.com/afollestad/ason#serialization)
    1. [Serializing Objects](https://github.com/afollestad/ason#serializing-objects)
    2. [Serializing Arrays](https://github.com/afollestad/ason#serializing-arrays)
    3. [Serializing Lists](https://github.com/afollestad/ason#serializing-lists)
    4. [Automatic Serialization](https://github.com/afollestad/ason#automatic-serialization)
8. [Deserialization](https://github.com/afollestad/ason#deserialization)
    1. [Deserializing Objects](https://github.com/afollestad/ason#deserializing-objects)
    2. [Deserializing Arrays](https://github.com/afollestad/ason#deserializing-arrays)
    3. [Deserializing Lists](https://github.com/afollestad/ason#deserializing-lists)
    4. [Automatic Deserialization](https://github.com/afollestad/ason#automatic-deserialization)
9. [Annotations](https://github.com/afollestad/ason#annotations)
    1. [@AsonName](https://github.com/afollestad/ason#asonname)
    2. [@AsonIgnore](https://github.com/afollestad/ason#asonignore)
10. [Retrofit](https://github.com/afollestad/ason#retrofit)

---

# Dependency

The dependency is available via jCenter.

### Gradle (Java)

```Gradle
dependencies {
    ...
    compile 'com.afollestad:ason:[latest-version]'
}
```

### Gradle (Android)

Since Android includes `org.json` classes, you'll want to exclude the copies provided by this library:

```Gradle
dependencies {
    ...
    compile('com.afollestad:ason:[latest-version]') {
        exclude group: 'org.json', module: 'json'
    }
}
```

### Gradle (Kotlin)

In Kotlin, you'll want to exclude IntelliJ's annotations library to avoid a DexException. *If you are using Kotlin with 
Android, make sure you also exclude org.json as shown in the section above.*

```Gradle
dependencies {
    ...
    compile('com.afollestad:ason:[latest-version]') {
        exclude group: 'com.intellij', module: 'annotations'
    }
}
```

### Maven

```xml
<dependency>
  <groupId>com.afollestad</groupId>
  <artifactId>ason</artifactId>
  <version>[latest-version]</version>
  <type>pom</type>
</dependency>
```

---

# Parsing and Building Objects

There are various ways that this library allows you to construct JSON objects...

Parsing strings is the first, just use the constructor which accepts a `String`:

```java
String input = // ...
Ason ason = new Ason(input);
```

Second, you can build objects using Java fields:

```java
// Translates to {"id":1,"name":"Aidan","born":1995}
Ason ason = new Ason() {
    int id = 1;
    String name = "Aidan";
    int born = 1995;
};
```

Third, you can add values with the `put()` method:

```java
Ason ason = new Ason()
    .put("_id", 1)
    .put("name", "Aidan")
    .put("born", 1995);
```

You can quickly put in arrays just by passing multiple values to `put()`:

```java
// Translates to {"greetings":["Hello","World"]}
Ason ason = new Ason();
// The first parameter is a key, you can pass any type for the rest of the varargs parameters
ason.put("greetings", "Hello", "World");
```

---

# Retrieving Values from Objects

Various methods exist for retrieving existing values (default values are returned if they don't exist). The one parameter 
version uses whatever the usual default of a type is (0 for number types, null for everything else), if the no value is found 
for the key. The two parameter version lets you specify a custom default.

```java
Ason ason = // ...

String str = ason.getString("name");
String strWithDefault = ason.getString("name", null);

Character chr = ason.getChar("name");
Character chrWithDefault = ason.getChar("name", null);

boolean bool = ason.getBool("name");
boolean boolWithDefault = ason.getBool("name", true);

short shrt = ason.getShort("name");
short shrtWithDefault = ason.getShort("name", (short)0);

int integer = ason.getInt("name");
int integerWithDefault = ason.getInt("name", 0);

long lng = ason.getLong("name");
long lngWithDefault = ason.getLong("name", 0L);

float flt = ason.getFloat("name");
float fltWithDefault = ason.getFloat("name", 0f);

double doub = ason.getDouble("name");
double doubWithDefault = ason.getDouble("name", 0d);

byte byt = ason.getByte("name");
byte bytWithDefault = ason.getByte("name", 0d);

Ason obj = ason.getAsonObject("name");
AsonArray ary = ason.getAsonArray("name");
```

Further, the `get(String)` method will actually automatically cast its return value to whatever variable you're setting it to:

```java
String str = ason.get("name");
long lng = ason.get("name");
```

It will also infer its type if you pass a default value, removing the need to use explicit `get[Type]` methods:

```java
if (ason.get("name", false)) {
    // do something
}
```

---

You can check if values exist, are null, equal another value, or even remove them by key:

```java
Ason ason = // ...

boolean exists = ason.has("name");
boolean isNull = ason.isNull("name");
boolean valueEqual = ason.equal("key-name", value);
ason.remove("name");
```

---

# Parsing and Building Arrays

Like objects, you can parse arrays from Strings:

```java
String input = // ...
AsonArray<Ason> array = new AsonArray<Ason>(input);
```

You can add new objects with `.add()`:

```java
AsonArray<String> array = new AsonArray<String>();
// You can add multiple items with a single .put() call, you could use multiple if necessary too
array.add("Hello", "World!");
```

You can retrieve and remove objects by index:

```java
AsonArray<Ason> array = // ...

Ason firstItem = array.get(0);
array.remove(0);
```

Some other utility methods exist, also:

```java
AsonArray<String> array = // ...

int size = array.size();
boolean empty = array.isEmpty();
boolean itemEqual = array.equal(0, "Does index 0 equal this value?")
```

---

# Pretty Print

Objects and arrays can both be converted to strings simply with the `toString()` method:

```java
Ason ason = // ...

String value = ason.toString(); // all on one line, no formatting
String formatted = ason.toString(4); // 4 spaces being the indent size
```

---

# Paths 

Paths use periods and dollar signs to let you quickly add, retrieve, or remove items which are deeper 
down in your JSON hierarchy without manually traversing.

### Dot Notation

Lets create an object using a few dot notation keys:

```java
Ason ason = new Ason()
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

---

You can retrieve values from objects using dot notation:

```java
Ason ason = // ...

String name = ason.get("name");
String month = ason.get("birthday.month");
int day = ason.get("birthday.day");
int year = ason.get("birthday.year");
```

If you wanted to remove the inner "year" value:
 
```java
Ason ason = // ...
ason.remove("birthday.year");
```

---

You can use dot notation with arrays too, but you need to specify the index of the object to pull from:

```java
AsonArray ason = // ...
String name = ason.get(1, "birthday.month");
```

---

As a bonus, you can check equality without doing a manual value comparison:

```java
Ason ason = // ...
boolean birthYearCheck = ason.equal("birthday.year", 1995);

AsonArray ason2 = // ...
boolean birthYearCheck2 = ason2.equal(2, "birthday.year", 1995);
```

### Index Notation

To extend on dot notations in paths, you can use this notation to perform operations on array children.

Take this JSON:

```json
{
    "group_id": 1,
    "title": "Hello, world!",
    "participants": [
        {
            "name": "Aidan",
            "id": 2
        },
        {
            "name": "Nina",
            "id": 1
        }
    ]
}
```

You could create this using index notation as such:

```java
Ason ason = new Ason()
    .put("group_id", 1)
    .put("title", "Hello, world!")
    .put("participants.$0.name", "Aidan")
    .put("participants.$0.id", 2)
    .put("participants.$1.name", "Nina")
    .put("participants.$1.id", 1);
```

The dollar sign followed by the number 0 indicates that you want the item at index 0 (position 1) 
within an array called "participants".

---

You can retrieve the value of "name" in the second participant like this:

```java
Ason object = // ...
String name = object.get("participants.$1.name");
```

If you wanted to remove the first item from the inner array, you can do that with index notation. This avoids the 
need to first retrieve the "participants" object:

```java
Ason object = // ...
object.remove("participants.$0");
```

### Escaping Periods and Dollar Signs

If your keys have literal periods in them, you can escape the periods so that they aren't used when 
following a path.

Take this JSON: 

```json
{
    "files": {
        "test.txt": "Hello, world!"
    }
}
```

You can retrieve the value if the inner `test.txt` string like this:

```java
Ason ason = // ...
String value = ason.get("files.test\\.txt");
```

We use two forward slashes since Java requires that you escape slashes. The literal string 
is just *"files.test\.txt"*.

---

You can also escape dollar signs which are normally used with index notation.

Take this JSON:

```json
{
    "participants": {
        "$1": {
            "name": "Nina"
        }
    }
}
```

To retrieve the inner string called "name":

```java
Ason ason = // ...
String value = ason.get("participants.\\$1.name");
```

We use an escaped forward slash (\\) in front of the dollar sign to indicate that the dollar sign 
is literal, and actually used in the name of a key.

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

### Serializing Objects

We can serialize an instance as follows:

```java
Person aidan = new Person(1, "Aidan");
aidan.spouse = new Person(2, "Nina");

Ason ason = Ason.serialize(aidan);
```

The resulting `Ason` object is:

```json
{
    "id": 1,
    "name": "Aidan",
    "spouse": {
        "id": 2,
        "name": "Nina"
    }
}
```

### Serializing Arrays

Serializing arrays is very similar to serializing objects, it uses the `serializeArray` method:

```java
Person[] people = // ...
AsonArray<Person> array = Ason.serializeArray(people);
```

Don't forget, you can serialize primitive arrays:

```java
int[] ids = new int[] { 1, 2, 3, 4 };
AsonArray<Integer> array = Ason.serializeArray(ids);
```

### Serializing Lists

Serializing lists uses the `serializeList` method: 

```java
List<Person> people2 = // ...
AsonArray<Person> array2 = AsonArray.serializeList(people2);
```

### Automatic Serialization

If you already have a `Ason` instance, you can add Java class instances into the object and serialize them automatically:

```java
Ason ason = new Ason();
Person person1 = new Person(1, "Aidan");
Person person2 = new Person(2, "Nina");

ason.put("person1", person);
ason.put("person2", person2);
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
        "name": "Nina"
    }
}
```

---

This automatic serialization works with `AsonArray`'s too:

```java
AsonArray<Person> array = new AsonArray<Person>();
Person person1 = new Person(1, "Aidan");
Person person2 = new Person(2, "Nina");

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
        "name": "Nina"
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

### Deserializing Objects

We can deserialize an object as follows:

```java
String input = "{\"id\":1,\"name\":\"Aidan\",\"spouse\":{\"id\":2,\"name\":\"Nina\"}}";
Ason ason = new Ason(input);

Person person = ason.deserialize(Person.class);
```

There is also a static method which has the same effect:

```java
String input = "{\"id\":1,\"name\":\"Aidan\",\"spouse\":{\"id\":2,\"name\":\"Nina\"}}";
Ason ason = new Ason(input);

Person person = Ason.deserialize(ason, Person.class);
```

Lastly, you can directly deserialize JSON strings:

```java
String input = "{\"id\":1,\"name\":\"Aidan\",\"spouse\":{\"id\":2,\"name\":\"Nina\"}}";
Person person = Ason.deserialize(input, Person.class);
```

### Deserializing Arrays

You can deserialize JSON arrays to Java lists and arrays too:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
AsonArray<Person> array = new AsonArray<>(input);

Person[] peopleArray = array.deserialize(Person[].class);
```

There is also static methods which have the same effect:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
AsonArray<Person> array = new AsonArray<>(input);

Person[] peopleArray = Ason.deserialize(array, Person[].class);
```

Lastly, you can directly deserialize JSON strings:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
Person[] peopleArray = Ason.deserialize(input, Person[].class);
```

One more thing, you can deserialize arrays containing primitive types:

```java
String input = "[1,2,3,4]";
int[] primitiveArray = Ason.deserialize(input, int[].class);
```

### Deserializing Lists

Unlike objects and arrays, deserializing lists requires a separate method:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
AsonArray<Person> array = new AsonArray<>(input);

List<Person> peopleList = array.deserializeList(Person.class);
```

There is also a static method which has the same effect:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
AsonArray<Person> array = new AsonArray<>(input);

List<Person> peopleList = Ason.deserializeList(array, Person.class);
```

You can deserialize strings directly:

```java
String input = "[{\"name\":\"Aidan\",\"_id\":1},{\"name\":\"Nina\",\"_id\":2}]";
List<Person> peopleList = Ason.deserializeList(input, Person.class);
```

### Automatic Deserialization

If you already have a `Ason` instance, you can automatically pull out and deserialize Java class instances without 
using the `AsonSerializer` directly:

```java
Ason ason = // ...
// The JSON object needs to contain a child object with the key "person" representing the Person class.
Person person = ason.get("person", Person.class);
```

The same works for `AsonArray`'s:

```java
AsonArray<Person> array = // ...
// The JSON array needs to contain a list of objects representing the Person class.
Person person = array.get(0, Person.class);
```

For Lists, you need to use a separate method since it requires that you specific the type held by the list:

```java
Ason ason = // ...
// The JSON object needs to contain a child array with the key "people" representing an array of Person classes.
List<Person> people = array.getList("people", Person.class); 
```

---

# Annotations

This library comes with a two annotations that have their own special use cases.

### @AsonName

This annotation allows you to assign a custom name to fields.

It's used with `Ason` field construction:

```java
Ason ason = new Ason() {
    @AsonName(name = "_id") int id = 1;
    String name = "Aidan";
    int born = 1995;
};
```

And of course during serialization and deserialization:

```java
public class Person {
    
    @AsonName(name = "_id") int id;
    String name;
    int born;
    
    public Person(int id, String name, int born) {
        this.id = id;
        this.name = name;
        this.born = born;
    }
}
```

### @AsonIgnore

This annotation tells the library to ignore and fields it's used to mark. That means the field is not serialized, 
deserialized, or used with field construction:

```java
public class Person {
    
    @AsonIgnore String invisibleField = "Hello, world!";
    int id;
    String name;
    int born;
    
    public Person(int id, String name, int born) {
        this.id = id;
        this.name = name;
        this.born = born;
    }
}
```

----

# Retrofit

Ason's built in Retrofit converters let you send and receive objects with Retrofit, they are 
automatically serialized or deserialized. Just add the converter factory to your Retrofit instances:

```java
Retrofit retrofit =
  new Retrofit.Builder()
      ...
      .addConverterFactory(new AsonConverterFactory())
      .build();
```

`AsonConverterFactory` exists in a separate dependency:

```Gradle
dependencies {
    ...
    compile 'com.afollestad:ason-retrofit:[latest-version]'
}
```