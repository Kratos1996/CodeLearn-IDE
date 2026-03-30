package com.codelearn.ide.course

// ─── Course Repository ────────────────────────────────────────────────────────
// All course content stored offline. 10 languages × 2 levels.
// Firebase is used to sync progress, not content.

object CourseRepository {

    // ── Badges ────────────────────────────────────────────────────────────────

    val ALL_BADGES = listOf(
        BadgeInfo("kotlin_basic",   "Kotlin Starter",   "🟣", "Completed Kotlin Basics",   0xFF7F52FF),
        BadgeInfo("kotlin_adv",     "Kotlin Master",    "👑", "Completed Kotlin Advanced",  0xFF7F52FF),
        BadgeInfo("java_basic",     "Java Starter",     "☕", "Completed Java Basics",      0xFFED8B00),
        BadgeInfo("java_adv",       "Java Master",      "🏆", "Completed Java Advanced",    0xFFED8B00),
        BadgeInfo("python_basic",   "Python Starter",   "🐍", "Completed Python Basics",    0xFF3572A5),
        BadgeInfo("python_adv",     "Python Master",    "🐉", "Completed Python Advanced",  0xFF3572A5),
        BadgeInfo("js_basic",       "JS Starter",       "🟨", "Completed JavaScript Basics",0xFFF7DF1E),
        BadgeInfo("js_adv",         "JS Master",        "⚡", "Completed JavaScript Advanced",0xFFF7DF1E),
        BadgeInfo("cpp_basic",      "C++ Starter",      "🔵", "Completed C++ Basics",       0xFF004488),
        BadgeInfo("cpp_adv",        "C++ Master",       "💎", "Completed C++ Advanced",     0xFF004488),
        BadgeInfo("c_basic",        "C Starter",        "🔷", "Completed C Basics",         0xFF1C6BB0),
        BadgeInfo("c_adv",          "C Master",         "🛡", "Completed C Advanced",       0xFF1C6BB0),
        BadgeInfo("csharp_basic",   "C# Starter",       "💜", "Completed C# Basics",        0xFF9B4F96),
        BadgeInfo("csharp_adv",     "C# Master",        "🔮", "Completed C# Advanced",      0xFF9B4F96),
        BadgeInfo("ruby_basic",     "Ruby Starter",     "🔴", "Completed Ruby Basics",      0xFFCC342D),
        BadgeInfo("ruby_adv",       "Ruby Master",      "💍", "Completed Ruby Advanced",    0xFFCC342D),
        BadgeInfo("dart_basic",     "Dart Starter",     "🎯", "Completed Dart Basics",      0xFF00B4AB),
        BadgeInfo("dart_adv",       "Dart Master",      "🚀", "Completed Dart Advanced",    0xFF00B4AB),
        BadgeInfo("vb_basic",       "VB Starter",       "🟦", "Completed VB Basics",        0xFF5C2D91),
        BadgeInfo("vb_adv",         "VB Master",        "🏅", "Completed VB Advanced",      0xFF5C2D91),
    )

    fun getBadge(id: String) = ALL_BADGES.firstOrNull { it.id == id }

    // ── Get all courses for a language ────────────────────────────────────────

    fun getCoursesForLanguage(langId: String): List<Course> =
        listOf(getBasicCourse(langId), getAdvancedCourse(langId)).filterNotNull()

    fun getBasicCourse(langId: String): Course? = when (langId) {
        "kt"   -> kotlinBasic()
        "java" -> javaBasic()
        "py"   -> pythonBasic()
        "js"   -> jsBasic()
        "cpp"  -> cppBasic()
        "c"    -> cBasic()
        "cs"   -> csharpBasic()
        "rb"   -> rubyBasic()
        "dart" -> dartBasic()
        "vb"   -> vbBasic()
        else   -> null
    }

    fun getAdvancedCourse(langId: String): Course? = when (langId) {
        "kt"   -> kotlinAdvanced()
        "java" -> javaAdvanced()
        "py"   -> pythonAdvanced()
        "js"   -> jsAdvanced()
        "cpp"  -> cppAdvanced()
        "c"    -> cAdvanced()
        "cs"   -> csharpAdvanced()
        "rb"   -> rubyAdvanced()
        "dart" -> dartAdvanced()
        "vb"   -> vbAdvanced()
        else   -> null
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KOTLIN
    // ═══════════════════════════════════════════════════════════════════════════

    private fun kotlinBasic() = Course(
        id = "kt_basic", languageId = "kt", level = CourseLevel.BASIC,
        title = "Kotlin Basics", description = "Learn Kotlin from scratch — variables, functions, loops and more.",
        icon = "🟣", color = 0xFF7F52FF, badgeId = "kotlin_basic",
        certificateTitle = "Kotlin Basics Certificate",
        stages = listOf(
            stage("kt_b1","Introduction","🎯","What is Kotlin and why use it",
                lessons = listOf(
                    text("What is Kotlin?","Kotlin is a modern programming language made by JetBrains in 2016. It runs on the JVM (Java Virtual Machine) and is the official language for Android development."),
                    text("Why Kotlin?","Kotlin is concise, safe (null-safety built in), and 100% interoperable with Java. Major companies like Google, Netflix and Amazon use Kotlin."),
                    code("Your first Kotlin program","""fun main() {
    println("Hello, World!")
}""","kt"),
                    tip("Tip","In Kotlin, the `main()` function is the starting point of every program. `println()` prints a line to the console."),
                    code("Variables","""fun main() {
    val name = "Ishant"     // val = cannot change
    var age = 20            // var = can change
    age = 21                // OK
    println("${'$'}name is ${'$'}age years old")
}""","kt")
                ),
                exam = listOf(
                    q("kt_b1_q1","What keyword declares an immutable variable in Kotlin?", listOf("var","val","let","const"),1,"val means value — it cannot be reassigned after creation."),
                    q("kt_b1_q2","Which company created Kotlin?",listOf("Google","Oracle","JetBrains","Microsoft"),2,"JetBrains created Kotlin in 2011 and open-sourced it in 2012."),
                    q("kt_b1_q3","What does println() do?",listOf("Reads input","Creates a variable","Prints text and a new line","Defines a function"),2,"println prints its argument followed by a newline character."),
                    q("kt_b1_q4","Which is correct Kotlin variable declaration?",listOf("int x = 5","val x = 5","x := 5","var x: 5"),1,"Kotlin uses `val` or `var` followed by name, then optionally `: Type`, then `= value`."),
                    q("kt_b1_q5","Can you change a `val` variable?",listOf("Yes, always","Only once","No, never","Only with casting"),2,"val is immutable — use var if you need to reassign.")
                )
            ),
            stage("kt_b2","Data Types","📦","Strings, numbers, booleans",
                lessons = listOf(
                    text("Basic Types","Kotlin has: Int, Long, Double, Float, Boolean, String, Char. Kotlin infers types automatically."),
                    code("Type examples","""val age: Int = 25
val price: Double = 99.99
val isStudent: Boolean = true
val letter: Char = 'A'
val name: String = "Kotlin"
println(age)""","kt"),
                    text("String Templates","Use \$variable or \${expression} inside strings to embed values directly."),
                    code("String templates","""val x = 10
val y = 20
println("Sum of ${'$'}x and ${'$'}y is ${'$'}{x + y}")""","kt"),
                    tip("Null Safety","In Kotlin, variables cannot be null by default. Add ? to allow null: `var name: String? = null`")
                ),
                exam = listOf(
                    q("kt_b2_q1","What type stores a whole number in Kotlin?",listOf("Double","Float","Int","String"),2,"Int stores whole numbers like 1, 42, -7."),
                    q("kt_b2_q2","How do you embed a variable in a Kotlin string?",listOf("%(variable)","\$variable","${'$'}variable","#variable"),2,"String templates use the dollar sign: \"Hello ${'$'}name\""),
                    q("kt_b2_q3","Which type stores true/false?",listOf("Int","Bool","Boolean","Bit"),2,"Boolean stores true or false values."),
                    q("kt_b2_q4","What does String? mean?",listOf("Required String","String array","Nullable String","String format"),2,"The ? means the variable can hold null or a String."),
                    q("kt_b2_q5","What is the output of: println(10 / 3)?",listOf("3.33","3","3.0","Error"),1,"Int divided by Int gives Int in Kotlin. 10/3 = 3 (remainder discarded).")
                )
            ),
            stage("kt_b3","Control Flow","🔀","If, when, loops",
                lessons = listOf(
                    code("If expression","""val score = 85
val grade = if (score >= 90) "A"
            else if (score >= 80) "B"
            else "C"
println(grade) // B""","kt"),
                    code("When expression","""val day = 3
val name = when(day) {
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    else -> "Other"
}
println(name)""","kt"),
                    code("For loop","""for (i in 1..5) {
    println("Count: ${'$'}i")
}
// Also works:
val nums = listOf(10, 20, 30)
for (n in nums) println(n)""","kt"),
                    code("While loop","""var i = 0
while (i < 3) {
    println(i)
    i++
}""","kt"),
                    tip("Ranges","1..5 is inclusive (1,2,3,4,5). Use 1 until 5 for exclusive (1,2,3,4).")
                ),
                exam = listOf(
                    q("kt_b3_q1","What replaces switch in Kotlin?",listOf("match","case","when","choose"),2,"Kotlin uses `when` instead of switch-case."),
                    q("kt_b3_q2","What does 1..5 represent?",listOf("Array of 5","Range 1 to 5 inclusive","Range 1 to 4","List with 5"),1,"1..5 is an IntRange from 1 to 5 inclusive."),
                    q("kt_b3_q3","Which loop checks condition BEFORE executing?",listOf("do-while","for","while","repeat"),2,"while checks the condition first; do-while executes first then checks."),
                    q("kt_b3_q4","How many times does this print? for(i in 1 until 4)",listOf("4","3","5","2"),1,"1 until 4 = 1,2,3 — three iterations."),
                    q("kt_b3_q5","Can if be used as an expression in Kotlin?",listOf("No","Yes","Only with else","Only in functions"),1,"In Kotlin, if returns a value: val x = if(cond) a else b")
                )
            ),
            stage("kt_b4","Functions","⚙️","Define and call functions",
                lessons = listOf(
                    code("Basic function","""fun greet(name: String): String {
    return "Hello, ${'$'}name!"
}
fun main() {
    println(greet("Ishant"))
}""","kt"),
                    code("Single-expression function","""fun square(n: Int) = n * n
fun main() {
    println(square(5)) // 25
}""","kt"),
                    code("Default parameters","""fun greet(name: String, greeting: String = "Hello") {
    println("${'$'}greeting, ${'$'}name!")
}
greet("Ishant")           // Hello, Ishant!
greet("Ishant", "Hi")    // Hi, Ishant!""","kt"),
                    tip("Unit","Functions that return nothing have return type Unit (like void in Java). You can omit it.")
                ),
                exam = listOf(
                    q("kt_b4_q1","What keyword defines a function in Kotlin?",listOf("func","def","fun","function"),2,"Kotlin uses `fun` to declare functions."),
                    q("kt_b4_q2","What is the return type when a function returns nothing?",listOf("Void","None","Unit","Null"),2,"Unit is Kotlin's equivalent of void."),
                    q("kt_b4_q3","fun add(a:Int,b:Int) = a+b — what is this?",listOf("Error","Lambda","Single-expression function","Extension function"),2,"Single-expression functions use = instead of {return ...}."),
                    q("kt_b4_q4","What are default parameters?",listOf("Parameters with type","Required params","Params with preset values","Nullable params"),2,"Default parameters have a preset value used when caller doesn't provide one."),
                    q("kt_b4_q5","fun area(w:Int, h:Int=10):Int = w*h — area(5) returns?",listOf("0","50","5","Error"),1,"h defaults to 10, so area(5) = 5*10 = 50.")
                )
            ),
            stage("kt_b5","Collections","📚","Lists, Maps, Sets",
                lessons = listOf(
                    code("Lists","""val fruits = listOf("Apple","Banana","Mango") // immutable
val nums = mutableListOf(1, 2, 3)          // mutable
nums.add(4)
println(fruits[0])  // Apple
println(nums.size)  // 4""","kt"),
                    code("Maps","""val scores = mapOf("Ishant" to 95, "Rahul" to 88)
println(scores["Ishant"]) // 95

val map = mutableMapOf("a" to 1)
map["b"] = 2""","kt"),
                    code("Useful operations","""val nums = listOf(1,2,3,4,5)
val doubled = nums.map { it * 2 }      // [2,4,6,8,10]
val evens   = nums.filter { it % 2 == 0 } // [2,4]
val total   = nums.sum()               // 15
println(doubled)""","kt"),
                    tip("Immutable vs Mutable","Use listOf/mapOf for read-only. Use mutableListOf/mutableMapOf when you need to add/remove.")
                ),
                exam = listOf(
                    q("kt_b5_q1","Which creates an immutable list?",listOf("mutableListOf()","arrayListOf()","listOf()","listCreate()"),2,"listOf() creates a read-only list."),
                    q("kt_b5_q2","How to add to a mutableList?",listOf(".append()","+=","add()","push()"),2,"mutableList.add(element) adds to the end."),
                    q("kt_b5_q3","listOf(1,2,3).map{it*2} produces?",listOf("[1,2,3]","[2,4,6]","[1,4,9]","Error"),1,"map transforms each element — multiplied by 2 gives [2,4,6]."),
                    q("kt_b5_q4","What does filter do?",listOf("Sorts items","Removes duplicates","Keeps matching items","Transforms items"),2,"filter returns a new list with only elements matching the condition."),
                    q("kt_b5_q5","mapOf keys are?",listOf("Always Int","Always String","Any type","Only Comparable"),2,"Kotlin maps accept any type as key.")
                )
            )
        )
    )

    private fun kotlinAdvanced() = Course(
        id = "kt_adv", languageId = "kt", level = CourseLevel.ADVANCED,
        title = "Kotlin Advanced", description = "Classes, coroutines, lambdas, extensions and more.",
        icon = "👑", color = 0xFF7F52FF, badgeId = "kotlin_adv",
        certificateTitle = "Kotlin Advanced Certificate",
        stages = listOf(
            stage("kt_a1","OOP — Classes","🏗️","Classes, objects, inheritance",
                lessons = listOf(
                    code("Class basics","""class Person(val name: String, var age: Int) {
    fun greet() = "Hi, I'm ${'$'}name"
}
val p = Person("Ishant", 20)
println(p.greet())""","kt"),
                    code("Inheritance","""open class Animal(val name: String) {
    open fun speak() = "${'$'}name makes a sound"
}
class Dog(name: String) : Animal(name) {
    override fun speak() = "${'$'}name barks!"
}
println(Dog("Rex").speak())""","kt"),
                    code("Data class","""data class Point(val x: Int, val y: Int)
val p1 = Point(3, 4)
val p2 = p1.copy(x = 10)
println(p1) // Point(x=3, y=4)""","kt")
                ),
                exam = listOf(
                    q("kt_a1_q1","What keyword allows a class to be inherited?",listOf("abstract","open","public","extends"),1,"Classes are final by default in Kotlin. Use `open` to allow inheritance."),
                    q("kt_a1_q2","What does a data class auto-generate?",listOf("Only toString","toString, equals, hashCode, copy","Only equals","Nothing extra"),1,"Data classes get toString, equals, hashCode, copy, and componentN for free."),
                    q("kt_a1_q3","How to override a method in Kotlin?",listOf("Use @Override","Use override keyword","Just redefine it","Use super keyword"),1,"Both the base function and override must be marked with `open` and `override`."),
                    q("kt_a1_q4","Primary constructor is defined?",listOf("Inside class body","After class name","In companion object","In init block"),1,"class Person(val name: String) — the constructor is in the header."),
                    q("kt_a1_q5","object keyword creates a?",listOf("New instance","Singleton","Abstract class","Interface"),1,"object creates a singleton — one instance for the entire app.")
                )
            ),
            stage("kt_a2","Lambdas & Higher-Order","λ","Functional programming in Kotlin",
                lessons = listOf(
                    code("Lambda syntax","""val add = { a: Int, b: Int -> a + b }
println(add(3, 4)) // 7

val square: (Int) -> Int = { it * it }
println(square(5)) // 25""","kt"),
                    code("Higher-order functions","""fun operate(x: Int, y: Int, op: (Int,Int)->Int): Int {
    return op(x, y)
}
println(operate(10, 5) { a, b -> a + b }) // 15
println(operate(10, 5) { a, b -> a * b }) // 50""","kt"),
                    tip("it","When a lambda has one parameter, you can use `it` instead of naming it: list.filter { it > 0 }")
                ),
                exam = listOf(
                    q("kt_a2_q1","What is a lambda?",listOf("A class","An anonymous function","A variable","A loop"),1,"A lambda is a function literal — a function without a name."),
                    q("kt_a2_q2","(Int)->Int describes?",listOf("An Int value","A function type taking Int returning Int","A lambda error","A generic"),1,"Function types describe parameter and return types."),
                    q("kt_a2_q3","What is `it` in a lambda?",listOf("The index","The collection","Single implicit parameter","The result"),2,"When a lambda has exactly one parameter, `it` refers to it."),
                    q("kt_a2_q4","listOf(1,-2,3).filter{ it>0 } =?",listOf("[1,-2,3]","[-2]","[1,3]","[1,2,3]"),2,"filter keeps elements where condition is true — 1 and 3 are > 0."),
                    q("kt_a2_q5","A function that takes another function as param is?",listOf("Lambda","Closure","Higher-order function","Extension"),2,"Higher-order functions accept or return other functions.")
                )
            ),
            stage("kt_a3","Coroutines","⚡","Async programming made simple",
                lessons = listOf(
                    text("What are Coroutines?","Coroutines let you write asynchronous code that looks synchronous. Instead of callbacks or threads, you use suspend functions."),
                    code("Basic coroutine","""import kotlinx.coroutines.*
fun main() = runBlocking {
    launch {
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}
// Prints: Hello, then World!""","kt"),
                    code("suspend function","""suspend fun fetchData(): String {
    delay(1000) // simulates network call
    return "Data loaded"
}
fun main() = runBlocking {
    val result = fetchData()
    println(result)
}""","kt"),
                    tip("suspend","A `suspend` function can be paused and resumed. It must be called from a coroutine or another suspend function.")
                ),
                exam = listOf(
                    q("kt_a3_q1","What does `suspend` mean?",listOf("Function stops forever","Function can pause and resume","Function runs on main thread","Function is private"),1,"suspend functions can be paused without blocking a thread."),
                    q("kt_a3_q2","launch vs async?",listOf("Same thing","launch for fire-forget, async returns Deferred","async is Java","launch blocks thread"),1,"launch: fire and forget. async: returns a Deferred<T> you can await."),
                    q("kt_a3_q3","delay(1000) does?",listOf("Stops the app","Blocks thread 1 second","Suspends coroutine 1 second","Crashes"),2,"delay is a suspend function — it suspends the coroutine without blocking the thread."),
                    q("kt_a3_q4","runBlocking is used for?",listOf("Production apps","Bridging blocking and suspend code, mostly tests","Only Android","Background service"),1,"runBlocking bridges blocking world to coroutines — mainly for tests and main()."),
                    q("kt_a3_q5","Coroutines are?",listOf("Threads","Processes","Lightweight concurrent tasks","OS features"),2,"Coroutines are much lighter than threads — thousands can run simultaneously.")
                )
            ),
            stage("kt_a4","Extension Functions","🔌","Add functions to existing classes",
                lessons = listOf(
                    code("Extension function","""fun String.isPalindrome(): Boolean {
    return this == this.reversed()
}
println("racecar".isPalindrome()) // true
println("hello".isPalindrome())   // false""","kt"),
                    code("Extension on Int","""fun Int.factorial(): Long {
    var result = 1L
    for (i in 2..this) result *= i
    return result
}
println(5.factorial()) // 120""","kt"),
                    tip("Receiver","In an extension function, `this` refers to the object the function is called on — called the receiver.")
                ),
                exam = listOf(
                    q("kt_a4_q1","Extension functions are defined?",listOf("Inside the class","Outside the class","In interfaces","Only in objects"),1,"Extensions are defined outside the class using ReceiverType.functionName syntax."),
                    q("kt_a4_q2","What does `this` refer to in an extension?",listOf("The file","The calling object","null","The function"),1,"this in an extension refers to the receiver — the object it's called on."),
                    q("kt_a4_q3","Can you add extensions to String?",listOf("No, String is final","Yes","Only in same package","Only via inheritance"),1,"You can add extensions to any class including final ones like String."),
                    q("kt_a4_q4","fun Int.double() = this * 2 — 5.double() returns?",listOf("5","2","10","Error"),2,"this is 5, so 5 * 2 = 10."),
                    q("kt_a4_q5","Extensions can access private members?",listOf("Yes always","No","Only in same file","Only internal members"),1,"Extensions cannot access private or protected members of the receiver class.")
                )
            ),
            stage("kt_a5","Null Safety & Flows","🛡","Null safety, StateFlow, sealed classes",
                lessons = listOf(
                    code("Null safety operators","""var name: String? = null
println(name?.length)       // null (safe call)
println(name?.length ?: 0)  // 0   (elvis)
// name!!.length would crash""","kt"),
                    code("Sealed classes","""sealed class Result
data class Success(val data: String) : Result()
data class Error(val msg: String) : Result()

fun handle(r: Result) = when(r) {
    is Success -> println("Got: ${'$'}{r.data}")
    is Error   -> println("Err: ${'$'}{r.msg}")
}""","kt"),
                    tip("StateFlow","StateFlow<T> is a hot flow that holds the current value and emits updates to all collectors — used heavily with ViewModels.")
                ),
                exam = listOf(
                    q("kt_a5_q1","What does ?. operator do?",listOf("Force unwrap","Safe call — returns null if receiver is null","Elvis","Cast"),1,"?. is the safe call operator — returns null instead of throwing NPE."),
                    q("kt_a5_q2","What does ?: operator do?",listOf("Nullable cast","Elvis — provides default if null","Safe call","Force unwrap"),1,"?: is the Elvis operator — returns right side if left is null."),
                    q("kt_a5_q3","!! operator?",listOf("Safe call","Converts to non-null or throws NPE","Elvis","Check type"),1,"!! force-unwraps — throws NullPointerException if the value is null."),
                    q("kt_a5_q4","Sealed class benefit?",listOf("Faster code","Exhaustive when — compiler knows all subclasses","Prevents inheritance","Auto serialization"),1,"when on sealed class is exhaustive — compiler ensures all cases are handled."),
                    q("kt_a5_q5","StateFlow vs LiveData?",listOf("Same","StateFlow is KMP/coroutines native, LiveData is Android-only","LiveData is better","StateFlow is Java only"),1,"StateFlow works in KMP (multiplatform), LiveData is Android-only.")
                )
            )
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // PYTHON
    // ═══════════════════════════════════════════════════════════════════════════

    private fun pythonBasic() = Course(
        id = "py_basic", languageId = "py", level = CourseLevel.BASIC,
        title = "Python Basics", description = "Learn Python — the easiest language for beginners.",
        icon = "🐍", color = 0xFF3572A5, badgeId = "python_basic",
        certificateTitle = "Python Basics Certificate",
        stages = listOf(
            stage("py_b1","Introduction","🐍","Python syntax and print",
                lessons = listOf(
                    text("What is Python?","Python is a high-level, interpreted language created by Guido van Rossum. It is famous for its readable, clean syntax and is used in web, AI, data science, and automation."),
                    code("Hello World","""print("Hello, World!")
name = "Ishant"
print(f"Hello, {name}!")""","py"),
                    text("No semicolons!","Python does NOT use semicolons or curly braces. Indentation (4 spaces) defines code blocks."),
                    code("Variables","""age = 20
name = "Python"
pi = 3.14
is_fun = True
print(type(age))    # <class 'int'>""","py"),
                    tip("Dynamic typing","Python figures out types automatically — you never write `int x = 5`, just `x = 5`.")
                ),
                exam = listOf(
                    q("py_b1_q1","What function prints in Python 3?",listOf("echo()","print()","console.log()","System.out.println()"),1,"print() is Python 3's output function. Always use parentheses."),
                    q("py_b1_q2","How are code blocks defined in Python?",listOf("{}","begin/end","Indentation","()"),2,"Python uses indentation (whitespace) to define blocks — no curly braces."),
                    q("py_b1_q3","Which is a valid Python variable?",listOf("int x = 5","var x = 5","x = 5","x : int = 5"),2,"Python variables need no type keyword — just name = value."),
                    q("py_b1_q4","What does type() do?",listOf("Converts type","Returns the type of a value","Declares type","Checks equality"),1,"type(x) returns the type of x, like <class 'int'>."),
                    q("py_b1_q5","f-strings use?",listOf("\${}","#{}","f\"{}\"","@{}"),2,"f-strings: f\"Hello {name}\" — the f prefix enables expression embedding.")
                )
            ),
            stage("py_b2","Data Types","📦","int, str, list, dict, tuple",
                lessons = listOf(
                    code("Types","""x = 42          # int
y = 3.14        # float
name = "Python" # str
flag = True     # bool
nothing = None  # NoneType""","py"),
                    code("Lists","""fruits = ["apple", "banana", "mango"]
fruits.append("cherry")
print(fruits[0])   # apple
print(len(fruits)) # 4""","py"),
                    code("Dictionaries","""person = {"name": "Ishant", "age": 20}
print(person["name"])  # Ishant
person["city"] = "Bikaner"
print(person)""","py"),
                    code("Tuples","""coords = (10, 20)  # immutable
print(coords[0])   # 10
# coords[0] = 5  # ERROR — tuples cannot change""","py")
                ),
                exam = listOf(
                    q("py_b2_q1","Which is mutable?",listOf("tuple","str","list","int"),2,"Lists are mutable — you can add, remove and change elements. Tuples cannot be changed."),
                    q("py_b2_q2","How to add to a list?",listOf(".add()","+=","append()","push()"),2,"list.append(item) adds to the end of the list."),
                    q("py_b2_q3","dict['key'] does?",listOf("Deletes key","Gets value for key","Checks if key exists","Returns all keys"),1,"dict['key'] returns the value associated with that key."),
                    q("py_b2_q4","None is?",listOf("0","False","Empty string","Absence of value"),3,"None represents no value — like null in other languages."),
                    q("py_b2_q5","len([1,2,3]) =?",listOf("0","2","3","4"),2,"len() returns the number of items — 3 items → 3.")
                )
            ),
            stage("py_b3","Control Flow","🔀","if, for, while",
                lessons = listOf(
                    code("If statement","""score = 85
if score >= 90:
    print("A grade")
elif score >= 80:
    print("B grade")
else:
    print("C or below")""","py"),
                    code("For loop","""for i in range(5):
    print(i)      # 0 1 2 3 4

fruits = ["apple","banana"]
for fruit in fruits:
    print(fruit)""","py"),
                    code("While loop","""count = 0
while count < 3:
    print(count)
    count += 1
# Prints 0, 1, 2""","py"),
                    tip("range()","range(5) = 0,1,2,3,4 — range(1,6) = 1,2,3,4,5 — range(0,10,2) = 0,2,4,6,8")
                ),
                exam = listOf(
                    q("py_b3_q1","Correct Python if syntax?",listOf("if(x>0){","if x > 0:","if x > 0 then","IF x > 0 THEN"),1,"Python if uses colon at end, no parentheses required, no braces."),
                    q("py_b3_q2","range(3) generates?",listOf("1,2,3","0,1,2","0,1,2,3","1,2"),1,"range(3) generates 0, 1, 2 — starting from 0, ending before 3."),
                    q("py_b3_q3","elif means?",listOf("else","else if","end if","elif has no meaning"),1,"elif is Python's else if — checks another condition if previous was false."),
                    q("py_b3_q4","What keyword exits a loop early?",listOf("exit","stop","break","return"),2,"break immediately exits the current loop."),
                    q("py_b3_q5","What does continue do in a loop?",listOf("Exits loop","Skips current iteration","Restarts loop","Pauses loop"),1,"continue skips the rest of the current iteration and goes to the next.")
                )
            ),
            stage("py_b4","Functions","⚙️","def, arguments, return",
                lessons = listOf(
                    code("Functions","""def greet(name):
    return f"Hello, {name}!"

print(greet("Ishant"))  # Hello, Ishant!""","py"),
                    code("Default args","""def power(base, exp=2):
    return base ** exp

print(power(3))    # 9
print(power(2,3))  # 8""","py"),
                    code("*args and **kwargs","""def total(*nums):
    return sum(nums)

print(total(1, 2, 3, 4))  # 10""","py"),
                    tip("Lambda","Quick one-line functions: double = lambda x: x * 2 — then double(5) returns 10.")
                ),
                exam = listOf(
                    q("py_b4_q1","Keyword to define a function?",listOf("func","function","def","fn"),2,"Python uses `def` to define functions."),
                    q("py_b4_q2","def add(a,b=5): return a+b — add(3) returns?",listOf("Error","5","8","3"),2,"b defaults to 5, so add(3) = 3+5 = 8."),
                    q("py_b4_q3","What does ** do?",listOf("Multiply","Power/exponent","Pointer","Comment"),1,"** is the exponent operator in Python: 2**3 = 8."),
                    q("py_b4_q4","lambda x: x*2 is?",listOf("A class","A module","Anonymous function","A decorator"),2,"lambda creates a small anonymous function inline."),
                    q("py_b4_q5","*args allows?",listOf("Keyword arguments","Any number of positional args","One optional arg","No arguments"),1,"*args collects extra positional arguments into a tuple.")
                )
            ),
            stage("py_b5","Modules & Files","📂","import, file I/O",
                lessons = listOf(
                    code("Importing","""import math
print(math.sqrt(16))  # 4.0
print(math.pi)         # 3.14...

from random import randint
print(randint(1, 10))""","py"),
                    code("File reading","""# Write a file
with open("test.txt", "w") as f:
    f.write("Hello File!")

# Read it back
with open("test.txt", "r") as f:
    content = f.read()
    print(content)""","py"),
                    tip("with statement","Always use `with open(...)` — it automatically closes the file even if an error occurs.")
                ),
                exam = listOf(
                    q("py_b5_q1","How to import math module?",listOf("include math","#include math","import math","require math"),2,"Python uses the import keyword: import math"),
                    q("py_b5_q2","open(file, 'w') opens for?",listOf("Reading","Writing (creates/overwrites)","Appending","Binary"),1,"'w' mode creates the file if not exists or overwrites it."),
                    q("py_b5_q3","Why use with open()?",listOf("Faster","Auto-closes file","Required syntax","Imports module"),1,"with statement ensures file is closed even if an exception occurs."),
                    q("py_b5_q4","math.sqrt(9) returns?",listOf("3","3.0","9","0"),1,"sqrt returns a float: 3.0"),
                    q("py_b5_q5","from x import y means?",listOf("Import all of x","Import only y from x","Import x as y","Error"),1,"from module import name imports just that name, not the whole module.")
                )
            )
        )
    )

    private fun pythonAdvanced() = Course(
        id = "py_adv", languageId = "py", level = CourseLevel.ADVANCED,
        title = "Python Advanced", description = "OOP, decorators, generators, async/await and more.",
        icon = "🐉", color = 0xFF3572A5, badgeId = "python_adv",
        certificateTitle = "Python Advanced Certificate",
        stages = listOf(
            stage("py_a1","OOP","🏗️","Classes, inheritance, magic methods",
                lessons = listOf(
                    code("Class","""class Animal:
    def __init__(self, name):
        self.name = name
    def speak(self):
        return f"{self.name} makes a sound"

class Dog(Animal):
    def speak(self):
        return f"{self.name} barks!"

d = Dog("Rex")
print(d.speak())""","py"),
                    code("Magic methods","""class Vector:
    def __init__(self, x, y):
        self.x, self.y = x, y
    def __repr__(self):
        return f"Vector({self.x},{self.y})"
    def __add__(self, other):
        return Vector(self.x+other.x, self.y+other.y)

v = Vector(1,2) + Vector(3,4)
print(v)  # Vector(4,6)""","py")
                ),
                exam = listOf(
                    q("py_a1_q1","__init__ is?",listOf("Destructor","Constructor","Class method","Static method"),1,"__init__ is called when creating a new instance — like a constructor."),
                    q("py_a1_q2","self refers to?",listOf("The class","The current instance","The parent","The module"),1,"self is a reference to the current object instance."),
                    q("py_a1_q3","To inherit from Animal: class Dog(?):",listOf("extends Animal","inherits Animal","Animal","super(Animal)"),2,"Python inheritance: class Child(Parent):"),
                    q("py_a1_q4","__str__ vs __repr__?",listOf("Same","__str__ for users, __repr__ for devs","__repr__ for users","Only __str__ exists"),1,"__str__ is human-readable; __repr__ is for debugging."),
                    q("py_a1_q5","@property decorator?",listOf("Defines a class","Makes a method act like an attribute","Static method","Abstract method"),1,"@property lets you call a method like an attribute: obj.size instead of obj.size()")
                )
            ),
            stage("py_a2","Decorators","🎨","Functions that wrap functions",
                lessons = listOf(
                    code("Basic decorator","""def timer(func):
    import time
    def wrapper(*args, **kwargs):
        start = time.time()
        result = func(*args, **kwargs)
        print(f"Took {time.time()-start:.4f}s")
        return result
    return wrapper

@timer
def slow():
    import time; time.sleep(0.1)

slow()""","py"),
                    tip("@","The @ symbol is syntactic sugar: @timer above slow() is the same as slow = timer(slow)")
                ),
                exam = listOf(
                    q("py_a2_q1","A decorator?",listOf("A class attribute","A function that wraps another","A module","A data type"),1,"Decorators wrap functions to add behaviour without modifying their code."),
                    q("py_a2_q2","@timer above a function means?",listOf("Import timer","func = timer(func)","Call timer()","Delete func"),1,"@decorator is syntactic sugar for func = decorator(func)."),
                    q("py_a2_q3","functools.wraps is used to?",listOf("Speed up function","Preserve original function metadata","Create closure","Add type hints"),1,"functools.wraps preserves __name__, __doc__ of the wrapped function."),
                    q("py_a2_q4","Common built-in decorators?",listOf("@init @class","@property @staticmethod @classmethod","@public @private","@async @sync"),1,"@property, @staticmethod, and @classmethod are the main built-in decorators."),
                    q("py_a2_q5","@staticmethod means?",listOf("Method needs self","Method is private","Method doesn't need self or cls","Method is abstract"),2,"Static methods belong to the class but don't receive self or cls.")
                )
            ),
            stage("py_a3","Generators","⚡","yield and lazy evaluation",
                lessons = listOf(
                    code("Generator function","""def countdown(n):
    while n > 0:
        yield n
        n -= 1

for num in countdown(3):
    print(num)  # 3, 2, 1""","py"),
                    code("Generator expression","""squares = (x**2 for x in range(10))
print(next(squares))  # 0
print(next(squares))  # 1""","py"),
                    tip("Lazy","Generators don't compute all values at once — they generate one at a time. Huge memory savings for large data.")
                ),
                exam = listOf(
                    q("py_a3_q1","yield does what?",listOf("Returns and ends function","Pauses and returns a value","Raises exception","Imports module"),1,"yield pauses the function, returns a value, and resumes from that point next time."),
                    q("py_a3_q2","Generator benefit over list?",listOf("Faster always","Memory efficient — lazy evaluation","No benefit","Easier syntax"),1,"Generators produce values one at a time, so large sequences don't fill memory."),
                    q("py_a3_q3","next() on generator?",listOf("Resets it","Gets next yielded value","Deletes it","Returns length"),1,"next(gen) advances the generator and returns the next yielded value."),
                    q("py_a3_q4","(x*2 for x in range(5)) is?",listOf("List","Tuple","Generator expression","Set"),2,"Using () makes a generator expression, not a list."),
                    q("py_a3_q5","StopIteration is raised when?",listOf("Error in generator","Generator is exhausted","Type mismatch","Import fails"),1,"StopIteration is raised when a generator has no more values.")
                )
            ),
            stage("py_a4","Async/Await","🔄","Async programming in Python",
                lessons = listOf(
                    code("Async function","""import asyncio

async def greet(name):
    await asyncio.sleep(1)
    print(f"Hello, {name}!")

asyncio.run(greet("Ishant"))""","py"),
                    code("Multiple concurrent tasks","""import asyncio

async def task(n):
    await asyncio.sleep(n)
    print(f"Task {n} done")

async def main():
    await asyncio.gather(task(1), task(2), task(3))

asyncio.run(main())""","py"),
                    tip("asyncio.gather","gather runs multiple coroutines concurrently — all three tasks above run at the same time.")
                ),
                exam = listOf(
                    q("py_a4_q1","async def defines?",listOf("A thread","A coroutine function","A class","A generator"),1,"async def creates a coroutine function — called with await or asyncio.run()."),
                    q("py_a4_q2","await can only be used?",listOf("Anywhere","Inside async function","In main()","In class"),1,"await is only valid inside an async function."),
                    q("py_a4_q3","asyncio.sleep vs time.sleep?",listOf("Same","asyncio.sleep suspends coroutine; time.sleep blocks thread","time.sleep is async","No difference"),1,"asyncio.sleep yields control; time.sleep blocks the entire thread."),
                    q("py_a4_q4","asyncio.gather() does?",listOf("Runs tasks sequentially","Runs multiple coroutines concurrently","Creates a thread pool","Imports asyncio"),1,"gather runs all given coroutines concurrently and waits for all."),
                    q("py_a4_q5","asyncio.run(main()) does?",listOf("Creates a thread","Runs an async function from sync code","Imports asyncio","Starts a server"),1,"asyncio.run() is the entry point to run a top-level coroutine.")
                )
            ),
            stage("py_a5","Data Science Basics","📊","numpy, pandas, matplotlib intro",
                lessons = listOf(
                    code("NumPy basics","""import numpy as np
arr = np.array([1, 2, 3, 4, 5])
print(arr * 2)          # [2 4 6 8 10]
print(arr.mean())       # 3.0
print(np.zeros((3,3)))  # 3x3 zero matrix""","py"),
                    code("Pandas basics","""import pandas as pd
data = {"Name":["Alice","Bob"],"Score":[95,88]}
df = pd.DataFrame(data)
print(df)
print(df["Score"].mean())  # 91.5""","py"),
                    tip("Jupyter","For data science, use Jupyter Notebooks — they let you run Python code in cells and see results instantly.")
                ),
                exam = listOf(
                    q("py_a5_q1","NumPy is used for?",listOf("Web development","Numerical computing and arrays","File I/O","Threading"),1,"NumPy provides fast array operations and mathematical functions."),
                    q("py_a5_q2","pandas DataFrame is?",listOf("A list","A 2D table with labels","A dictionary","A numpy array"),1,"DataFrame is a 2D labeled data structure — like a spreadsheet in Python."),
                    q("py_a5_q3","np.array([1,2,3])*2 =?",listOf("[1,2,3]","[2,2,3]","[2,4,6]","Error"),2,"NumPy applies operations element-wise — each element multiplied by 2."),
                    q("py_a5_q4","What does df.mean() compute?",listOf("Median","Most frequent","Average of numeric columns","Sum"),2,"mean() computes the arithmetic average."),
                    q("py_a5_q5","matplotlib is for?",listOf("Machine learning","Web scraping","Plotting and visualisation","Database"),2,"matplotlib creates charts: line plots, bar charts, scatter plots, etc.")
                )
            )
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // JAVASCRIPT
    // ═══════════════════════════════════════════════════════════════════════════

    private fun jsBasic() = Course(
        id = "js_basic", languageId = "js", level = CourseLevel.BASIC,
        title = "JavaScript Basics", description = "Learn the language of the web from scratch.",
        icon = "🟨", color = 0xFFF7DF1E, badgeId = "js_basic",
        certificateTitle = "JavaScript Basics Certificate",
        stages = listOf(
            stage("js_b1","Introduction","🌐","What is JavaScript",
                lessons = listOf(
                    text("What is JavaScript?","JavaScript is the programming language of the web. Every interactive website uses it — from buttons to animations to real-time updates."),
                    code("Hello World","""console.log("Hello, World!");
let name = "Ishant";
console.log(`Hello, \${'$'}{name}!`);""","js"),
                    text("var vs let vs const","Always use const for values that don't change, let for variables. Avoid var — it has confusing scoping rules."),
                    code("Variables","""const PI = 3.14;         // cannot change
let score = 0;           // can change
score = 10;              // OK
// PI = 3.15;            // ERROR""","js")
                ),
                exam = listOf(
                    q("js_b1_q1","How to print in JavaScript?",listOf("print()","echo()","console.log()","System.out.println()"),2,"console.log() outputs to the browser/Node.js console."),
                    q("js_b1_q2","Best practice for constants?",listOf("var","let","const","val"),2,"const declares a block-scoped constant that cannot be reassigned."),
                    q("js_b1_q3","Template literals use?",listOf("'\${}'","\"#{}\"","`\${}`","@{}"),2,"Backticks with \${} embed expressions: `Hello \${'$'}{this.name}`"),
                    q("js_b1_q4","var vs let scope?",listOf("Same","var is function-scoped, let is block-scoped","let is global","No difference"),1,"var leaks out of blocks; let is confined to its {} block."),
                    q("js_b1_q5","=== vs ==?",listOf("Same","=== checks type too","== checks type too","=== is slower"),1,"=== is strict equality — checks value AND type. Always prefer ===.")
                )
            ),
            stage("js_b2","Functions","⚙️","Functions and arrow functions",
                lessons = listOf(
                    code("Function declaration","""function greet(name) {
    return `Hello, ${'$'}{name}!`;
}
console.log(greet("Ishant"));""","js"),
                    code("Arrow functions","""const add = (a, b) => a + b;
console.log(add(3, 4)); // 7

const square = n => n * n;
console.log(square(5)); // 25""","js"),
                    code("Callbacks","""const nums = [1, 2, 3, 4, 5];
const doubled = nums.map(n => n * 2);
const evens   = nums.filter(n => n % 2 === 0);
console.log(doubled); // [2,4,6,8,10]
console.log(evens);   // [2,4]""","js"),
                    tip("Hoisting","function declarations are hoisted — you can call them before they're defined. Arrow functions are NOT hoisted.")
                ),
                exam = listOf(
                    q("js_b2_q1","Arrow function syntax?",listOf("fn =>","=> function","(a,b) => a+b","function =>"),2,"Arrow functions: (params) => expression or (params) => { body }"),
                    q("js_b2_q2","[1,2,3].map(x=>x*2) =?",listOf("[1,2,3]","[2,4,6]","[1,4,9]","Error"),1,"map transforms each element — x*2 gives [2,4,6]."),
                    q("js_b2_q3","filter returns?",listOf("Modified original","New array with matching elements","Count of matches","First match"),1,"filter creates a new array with elements passing the test."),
                    q("js_b2_q4","Are arrow functions hoisted?",listOf("Yes","No","Only globally","Only in classes"),1,"const/let arrow functions are not hoisted."),
                    q("js_b2_q5","A callback function is?",listOf("An error handler","A function passed to another function","A constructor","A global function"),1,"A callback is a function passed as argument to be called later.")
                )
            ),
            stage("js_b3","Arrays & Objects","📦","Arrays, objects, destructuring",
                lessons = listOf(
                    code("Arrays","""const fruits = ["apple","banana","mango"];
fruits.push("cherry");
console.log(fruits.length); // 4
console.log(fruits[0]);     // apple
const [first, ...rest] = fruits; // destructuring""","js"),
                    code("Objects","""const person = {
    name: "Ishant",
    age: 20,
    greet() { return `Hi, I'm ${'$'}{this.name}`; }
};
console.log(person.name);
console.log(person.greet());
const { name, age } = person; // destructuring""","js"),
                    tip("Spread operator","... spreads an array or object: const copy = [...arr]; const merged = {...obj1, ...obj2}")
                ),
                exam = listOf(
                    q("js_b3_q1","arr.push(x) does?",listOf("Removes last","Adds x to start","Adds x to end","Returns length"),2,"push() adds element to the end of the array and returns new length."),
                    q("js_b3_q2","const {name} = person does?",listOf("Creates new object","Destructures name from person","Deletes name","Copies person"),1,"Object destructuring extracts properties into variables."),
                    q("js_b3_q3","[...arr1,...arr2] does?",listOf("Nested array","Merges two arrays","Error","Adds lengths"),1,"Spread merges arrays: [1,2,...[3,4]] = [1,2,3,4]"),
                    q("js_b3_q4","this in an object method refers to?",listOf("Window","The function","The object","undefined"),2,"this inside a method refers to the object the method is called on."),
                    q("js_b3_q5","arr.find(x=>x>3) on [1,2,3,4,5] returns?",listOf("true","[4,5]","4","3"),2,"find returns the FIRST element matching condition — 4.")
                )
            ),
            stage("js_b4","Async JavaScript","⏳","Promises and async/await",
                lessons = listOf(
                    code("Promises","""const promise = new Promise((resolve, reject) => {
    setTimeout(() => resolve("Data ready!"), 1000);
});

promise
    .then(data => console.log(data))
    .catch(err => console.error(err));""","js"),
                    code("async/await","""async function fetchUser() {
    try {
        const response = await fetch("https://api.example.com/user");
        const data = await response.json();
        console.log(data);
    } catch (error) {
        console.error("Failed:", error);
    }
}""","js"),
                    tip("Always catch","Always add .catch() or try/catch with await — unhandled promise rejections cause silent failures.")
                ),
                exam = listOf(
                    q("js_b4_q1","Promise states?",listOf("true/false","pending/fulfilled/rejected","open/closed","start/end"),1,"A Promise is: pending → fulfilled (resolved) or rejected."),
                    q("js_b4_q2","await can only be used in?",listOf("Any function","async function","Promise","try block"),1,"await is only valid inside an async function."),
                    q("js_b4_q3",".then() runs when?",listOf("Promise rejects","Promise resolves","Always","On error"),1,".then() callback runs when the promise resolves successfully."),
                    q("js_b4_q4",".catch() handles?",listOf("All callbacks","Rejected promises","Syntax errors","Async functions"),1,".catch() handles promise rejection."),
                    q("js_b4_q5","async function always returns?",listOf("undefined","A Promise","The value","An array"),1,"async functions always return a Promise, even if you return a plain value.")
                )
            ),
            stage("js_b5","DOM & Events","🖱","Browser document manipulation",
                lessons = listOf(
                    code("DOM manipulation","""// Get elements
const btn = document.getElementById("myBtn");
const div = document.querySelector(".container");

// Change content
div.textContent = "Hello!";
div.style.color = "blue";

// Create elements
const p = document.createElement("p");
p.textContent = "New paragraph";
document.body.appendChild(p);""","js"),
                    code("Event listeners","""btn.addEventListener("click", () => {
    console.log("Button clicked!");
});

document.addEventListener("keydown", (e) => {
    console.log("Key pressed:", e.key);
});""","js"),
                    tip("querySelector","querySelector('#id') selects by ID, querySelector('.class') by class, querySelector('tag') by tag name.")
                ),
                exam = listOf(
                    q("js_b5_q1","getElementById returns?",listOf("Array of elements","One element by ID","All divs","null always"),1,"getElementById returns the single element with that ID, or null."),
                    q("js_b5_q2","addEventListener('click', fn) does?",listOf("Removes listener","Registers click handler","Triggers click","Creates button"),1,"addEventListener registers a function to run when the event fires."),
                    q("js_b5_q3","DOM stands for?",listOf("Data Object Model","Document Object Model","Dynamic Object Manager","Display Output Model"),1,"DOM = Document Object Model — the tree of HTML elements as JS objects."),
                    q("js_b5_q4","querySelector('.box') selects?",listOf("Element with id box","All .box elements","First element with class box","Nothing"),2,"querySelector returns the FIRST matching element."),
                    q("js_b5_q5","element.textContent = 'Hi' does?",listOf("Gets text","Deletes text","Sets text content","Creates element"),2,"Setting textContent replaces the element's text.")
                )
            )
        )
    )

    private fun jsAdvanced() = Course(
        id = "js_adv", languageId = "js", level = CourseLevel.ADVANCED,
        title = "JavaScript Advanced", description = "Closures, prototypes, ES6+, modules and Node.js.",
        icon = "⚡", color = 0xFFF7DF1E, badgeId = "js_adv",
        certificateTitle = "JavaScript Advanced Certificate",
        stages = listOf(
            stage("js_a1","Closures & Scope","🔒","Closures, hoisting, this",
                lessons = listOf(
                    code("Closure","""function counter() {
    let count = 0;
    return () => {
        count++;
        return count;
    };
}
const inc = counter();
console.log(inc()); // 1
console.log(inc()); // 2""","js"),
                    tip("Closure","A closure is a function that remembers variables from its outer scope even after the outer function has returned.")
                ),
                exam = listOf(
                    q("js_a1_q1","A closure is?",listOf("A class","A function with access to outer scope","A module","A promise"),1,"Closures capture variables from the enclosing scope."),
                    q("js_a1_q2","let is?",listOf("Function scoped","Block scoped","Global","Same as var"),1,"let is block-scoped — only accessible within the {} it's defined in."),
                    q("js_a1_q3","Hoisting applies to?",listOf("let and const","var and function declarations","Only functions","Nothing"),1,"var declarations and function declarations are hoisted to the top."),
                    q("js_a1_q4","this in arrow function?",listOf("The object","Inherited from outer scope","undefined","window always"),1,"Arrow functions don't have their own this — they inherit from enclosing context."),
                    q("js_a1_q5","IIFE stands for?",listOf("Immediately Invoked Function Expression","Internal Interface","Inline Function","Import If Exists"),0,"IIFE: (function(){})() — runs immediately when defined.")
                )
            ),
            stage("js_a2","Prototypes & Classes","🏗️","OOP in JavaScript",
                lessons = listOf(
                    code("ES6 Class","""class Animal {
    constructor(name) { this.name = name; }
    speak() { return `${'$'}{this.name} makes a sound`; }
}
class Dog extends Animal {
    speak() { return `${'$'}{this.name} barks!`; }
}
const d = new Dog("Rex");
console.log(d.speak());
console.log(d instanceof Animal); // true""","js")
                ),
                exam = listOf(
                    q("js_a2_q1","super() calls?",listOf("The class itself","Parent constructor","A static method","A module"),1,"super() invokes the parent class constructor."),
                    q("js_a2_q2","instanceof checks?",listOf("Object type","If object is instance of class","Value equality","Prototype"),1,"instanceof returns true if object was created from that class."),
                    q("js_a2_q3","prototype chain?",listOf("An import chain","How objects inherit from each other","A module system","An array method"),1,"Objects inherit properties through the prototype chain."),
                    q("js_a2_q4","static method is called on?",listOf("Instance","Class itself","Both","Prototype"),1,"Static methods are called on the class: Animal.create(), not new Animal().create()"),
                    q("js_a2_q5","get keyword creates?",listOf("A method","A getter — computed property","A constructor","A setter"),1,"Getters compute a value like a property: get area() { return this.w * this.h }")
                )
            ),
            stage("js_a3","Modules & Bundlers","📦","import/export, npm, webpack basics",
                lessons = listOf(
                    code("ES Modules","""// math.js
export const PI = 3.14;
export function add(a,b) { return a+b; }
export default function multiply(a,b) { return a*b; }

// main.js
import multiply, { PI, add } from './math.js';
console.log(add(2,3)); // 5""","js"),
                    tip("package.json","npm init creates package.json — it lists your project's dependencies. npm install adds packages from npmjs.com.")
                ),
                exam = listOf(
                    q("js_a3_q1","export default exports?",listOf("Multiple values","One main value per file","Constants only","Nothing"),1,"Each module can have one default export, imported without braces."),
                    q("js_a3_q2","Named exports use?",listOf("export default","export { name }","module.exports","require()"),1,"Named exports: export const x = 1; imported as: import { x } from './file'"),
                    q("js_a3_q3","npm stands for?",listOf("New Package Manager","Node Package Manager","Native Package Module","Network Package Manager"),1,"npm = Node Package Manager — installs JS libraries."),
                    q("js_a3_q4","import vs require()?",listOf("Same","import is ES6 modules, require is CommonJS","require is newer","No difference"),1,"import/export is ES modules (modern); require/module.exports is CommonJS (Node.js legacy)."),
                    q("js_a3_q5","Webpack is a?",listOf("Testing library","Code bundler","Database","Framework"),1,"Webpack bundles multiple JS files into one for the browser.")
                )
            ),
            stage("js_a4","Error Handling & Debugging","🛠","try/catch, custom errors",
                lessons = listOf(
                    code("Error handling","""function divide(a, b) {
    if (b === 0) throw new Error("Division by zero!");
    return a / b;
}
try {
    console.log(divide(10, 0));
} catch (err) {
    console.error("Caught:", err.message);
} finally {
    console.log("Always runs");
}""","js"),
                    code("Custom error","""class ValidationError extends Error {
    constructor(field, msg) {
        super(msg);
        this.name = "ValidationError";
        this.field = field;
    }
}
throw new ValidationError("email","Invalid email");""","js")
                ),
                exam = listOf(
                    q("js_a4_q1","throw creates?",listOf("A log","An error/exception","A return","A warning"),1,"throw creates and throws an exception that stops execution."),
                    q("js_a4_q2","finally block runs?",listOf("Only on success","Only on error","Always","Never on error"),2,"finally always runs regardless of whether an error occurred."),
                    q("js_a4_q3","err.message contains?",listOf("Stack trace","The error message string","Error code","Line number"),1,"err.message is the string passed to new Error('message')."),
                    q("js_a4_q4","Custom error should extend?",listOf("Object","Function","Error","Exception"),2,"Custom errors extend the built-in Error class."),
                    q("js_a4_q5","try/catch can handle async errors with?",listOf("Just try/catch","async/await + try/catch","Only .catch()","Not possible"),1,"async/await + try/catch handles async errors cleanly.")
                )
            ),
            stage("js_a5","Node.js Basics","🖥","Server-side JavaScript",
                lessons = listOf(
                    code("HTTP server","""const http = require('http');
const server = http.createServer((req, res) => {
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Hello from Node.js!');
});
server.listen(3000, () => console.log('Running on :3000'));""","js"),
                    code("File system","""const fs = require('fs');
fs.writeFileSync('hello.txt', 'Hello!');
const data = fs.readFileSync('hello.txt','utf8');
console.log(data);""","js"),
                    tip("Express","For real web servers, use Express.js: npm install express — it simplifies routing, middleware and request handling.")
                ),
                exam = listOf(
                    q("js_a5_q1","Node.js runs JavaScript?",listOf("In browser only","On the server","In mobile apps","In databases"),1,"Node.js is a runtime that runs JavaScript outside the browser."),
                    q("js_a5_q2","require('fs') imports?",listOf("A React module","File system module","Fetch API","Nothing"),1,"fs is Node's built-in file system module."),
                    q("js_a5_q3","http.createServer creates?",listOf("A database","A web server","An HTTP request","A file"),1,"createServer creates an HTTP server that listens for requests."),
                    q("js_a5_q4","res.end() does?",listOf("Stops server","Sends response and ends connection","Logs error","Reloads page"),1,"res.end() sends the final response body and ends the HTTP connection."),
                    q("js_a5_q5","server.listen(3000) means?",listOf("Connect to port 3000","Listen on port 3000 for requests","Send to port 3000","Open port 3000 on client"),1,"listen() starts the server accepting connections on the given port.")
                )
            )
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // JAVA (summary — full structure like Kotlin)
    // ═══════════════════════════════════════════════════════════════════════════

    private fun javaBasic() = Course(
        id = "java_basic", languageId = "java", level = CourseLevel.BASIC,
        title = "Java Basics", description = "Learn Java — the foundation of Android development.",
        icon = "☕", color = 0xFFED8B00, badgeId = "java_basic",
        certificateTitle = "Java Basics Certificate",
        stages = listOf(
            stage("java_b1","Introduction","☕","Java basics and Hello World",
                lessons = listOf(
                    text("What is Java?","Java is a class-based, object-oriented language created by Sun Microsystems in 1995. It follows 'Write Once, Run Anywhere' — compiles to bytecode that runs on any JVM."),
                    code("Hello World","""public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}""","java"),
                    code("Variables","""int age = 20;
double price = 99.99;
boolean active = true;
String name = "Java";
System.out.println(name + " version: 21");""","java")
                ),
                exam = listOf(
                    q("java_b1_q1","Java is compiled to?",listOf("Machine code","Bytecode","JavaScript","Assembly"),1,"Java compiles to bytecode (.class files) run by the JVM."),
                    q("java_b1_q2","Entry point of a Java program?",listOf("start()","run()","public static void main(String[] args)","init()"),2,"main() is the entry point — must be public static void."),
                    q("java_b1_q3","System.out.println does?",listOf("Reads input","Prints and new line","Prints without new line","Creates variable"),1,"println prints its argument followed by a newline."),
                    q("java_b1_q4","String type in Java?",listOf("string","STRING","String","str"),2,"Java's String type starts with capital S — it's a class, not a primitive."),
                    q("java_b1_q5","int stores?",listOf("Decimals","Whole numbers","Text","True/false"),1,"int stores integer (whole number) values.")
                )
            ),
            stage("java_b2","OOP Basics","🏗️","Classes and objects",
                lessons = listOf(
                    code("Class and object","""public class Car {
    String brand;
    int speed;
    
    void accelerate() {
        speed += 10;
        System.out.println(brand + " at " + speed + " km/h");
    }
}
// Usage:
Car c = new Car();
c.brand = "Toyota";
c.accelerate();""","java"),
                    code("Constructor","""public class Person {
    String name;
    int age;
    
    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    void greet() {
        System.out.println("Hi, I'm " + name);
    }
}""","java")
                ),
                exam = listOf(
                    q("java_b2_q1","new keyword does?",listOf("Declares variable","Creates class","Creates instance","Imports class"),2,"new allocates memory and creates a new object instance."),
                    q("java_b2_q2","Constructor name must match?",listOf("Parent class","The class name","The method","The file"),1,"Constructor name must exactly match the class name."),
                    q("java_b2_q3","this refers to?",listOf("The parent","Current object instance","The method","A static field"),1,"this refers to the current instance of the class."),
                    q("java_b2_q4","void return type means?",listOf("Returns null","Returns 0","Returns nothing","Returns String"),2,"void means the method returns no value."),
                    q("java_b2_q5","Access modifier for class-only access?",listOf("public","protected","private","default"),2,"private restricts access to within the same class.")
                )
            ),
            stage("java_b3","Control Flow & Arrays","🔀","if, loops, arrays",
                lessons = listOf(
                    code("Control flow","""int score = 85;
if (score >= 90) System.out.println("A");
else if (score >= 80) System.out.println("B");
else System.out.println("C");

for (int i = 0; i < 5; i++) {
    System.out.print(i + " ");
}""","java"),
                    code("Arrays","""int[] nums = {1, 2, 3, 4, 5};
System.out.println(nums.length); // 5
System.out.println(nums[0]);     // 1

for (int n : nums) {  // enhanced for
    System.out.print(n + " ");
}""","java")
                ),
                exam = listOf(
                    q("java_b3_q1","Array declaration?",listOf("int nums[]","int nums","int[] nums","array<int> nums"),2,"int[] nums is the preferred Java array declaration style."),
                    q("java_b3_q2","nums.length for int[] nums={1,2,3} is?",listOf("2","3","4","0"),1,"length property of array gives count of elements — 3."),
                    q("java_b3_q3","Enhanced for loop syntax?",listOf("for(int x; arr)","for(int x : arr)","for(x in arr)","for each(arr)"),1,"for(Type var : collection) iterates over each element."),
                    q("java_b3_q4","switch-case needs?",listOf("colon after case","break to exit","both colon and break","semicolon"),2,"Without break, execution falls through to next case."),
                    q("java_b3_q5","do-while vs while?",listOf("Same","do-while runs body at least once","while runs at least once","No difference"),1,"do-while executes the body first, then checks the condition.")
                )
            ),
            stage("java_b4","Inheritance & Interfaces","🔗","extends, implements, polymorphism",
                lessons = listOf(
                    code("Inheritance","""public class Animal {
    String name;
    void speak() { System.out.println("..."); }
}
public class Dog extends Animal {
    @Override
    void speak() { System.out.println(name + " barks!"); }
}
Animal a = new Dog(); // polymorphism
a.name = "Rex";
a.speak(); // Rex barks!""","java"),
                    code("Interface","""interface Drawable {
    void draw(); // abstract
    default void show() { System.out.println("Showing"); }
}
class Circle implements Drawable {
    public void draw() { System.out.println("Drawing circle"); }
}""","java")
                ),
                exam = listOf(
                    q("java_b4_q1","@Override annotation?",listOf("Required to override","Optional — marks overriding","Creates new method","Deprecated"),1,"@Override is optional but good practice — compiler verifies you're actually overriding."),
                    q("java_b4_q2","Interface vs abstract class?",listOf("Same","Interface has no state, supports multiple inheritance","Abstract class is faster","Interface needs extends"),1,"A class can implement multiple interfaces but only extend one class."),
                    q("java_b4_q3","final class?",listOf("Cannot be instantiated","Cannot be inherited","Runs faster","Has no methods"),1,"final prevents inheritance — no class can extend a final class."),
                    q("java_b4_q4","super.method() calls?",listOf("Current class method","Parent class method","Static method","Interface method"),1,"super.method() calls the parent class implementation."),
                    q("java_b4_q5","Polymorphism means?",listOf("Multiple constructors","Same interface different behaviour","Multiple inheritance","Final methods"),1,"Polymorphism: Animal reference holds Dog object — calls Dog's speak().")
                )
            ),
            stage("java_b5","Collections & Generics","📚","ArrayList, HashMap, generics",
                lessons = listOf(
                    code("ArrayList and HashMap","""import java.util.*;
ArrayList<String> list = new ArrayList<>();
list.add("Kotlin");
list.add("Java");
System.out.println(list.size()); // 2

HashMap<String, Integer> map = new HashMap<>();
map.put("Alice", 95);
System.out.println(map.get("Alice")); // 95""","java"),
                    code("Generics","""public class Box<T> {
    private T value;
    public Box(T value) { this.value = value; }
    public T get() { return value; }
}
Box<String> box = new Box<>("Hello");
System.out.println(box.get()); // Hello""","java")
                ),
                exam = listOf(
                    q("java_b5_q1","ArrayList vs array?",listOf("Same","ArrayList is dynamic size","Array is faster only","ArrayList is slower always"),1,"ArrayList grows and shrinks dynamically; arrays have fixed size."),
                    q("java_b5_q2","HashMap stores?",listOf("Sorted keys","Key-value pairs","Unique values","Lists"),1,"HashMap stores key-value pairs with O(1) average lookup."),
                    q("java_b5_q3","Generic <T> means?",listOf("Template type","Type parameter — any type","Only String","Only Integer"),1,"T is a type parameter — you specify the actual type at usage time."),
                    q("java_b5_q4","list.size() returns?",listOf("Last index","Number of elements","Capacity","0 always"),1,"size() returns the number of elements currently in the list."),
                    q("java_b5_q5","HashMap.get(key) returns?",listOf("true/false","The value or null","An iterator","The key"),1,"get() returns the value for the key, or null if key not found.")
                )
            )
        )
    )

    private fun javaAdvanced() = Course(
        id = "java_adv", languageId = "java", level = CourseLevel.ADVANCED,
        title = "Java Advanced", description = "Streams, concurrency, design patterns and more.",
        icon = "🏆", color = 0xFFED8B00, badgeId = "java_adv",
        certificateTitle = "Java Advanced Certificate",
        stages = listOf(
            stage("java_a1","Streams API","🌊","Functional-style data processing",
                lessons = listOf(
                    code("Streams","""import java.util.*;
import java.util.stream.*;
List<Integer> nums = Arrays.asList(1,2,3,4,5,6);
List<Integer> result = nums.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .collect(Collectors.toList());
System.out.println(result); // [4,16,36]""","java")
                ),
                exam = listOf(
                    q("java_a1_q1","stream().filter() does?",listOf("Sorts","Keeps matching elements","Transforms","Counts"),1,"filter keeps elements where predicate returns true."),
                    q("java_a1_q2","stream().map() does?",listOf("Filters","Transforms each element","Collects","Sorts"),1,"map applies a function to each element, producing a new stream."),
                    q("java_a1_q3","Collectors.toList() does?",listOf("Creates array","Collects stream to List","Prints","Counts"),1,"toList() is a terminal operation that collects stream elements into a List."),
                    q("java_a1_q4","stream() is?",listOf("A data structure","A sequence of elements supporting pipeline operations","A thread","A collection"),1,"Streams are lazy pipelines — they don't execute until a terminal operation."),
                    q("java_a1_q5","reduce() operation?",listOf("Filters","Combines all elements into one","Sorts","Maps"),1,"reduce folds elements: stream.reduce(0, Integer::sum) sums all integers.")
                )
            ),
            stage("java_a2","Concurrency","🔄","Threads, ExecutorService",
                lessons = listOf(
                    code("Thread basics","""Thread t = new Thread(() -> {
    System.out.println("Running in thread: " 
        + Thread.currentThread().getName());
});
t.start();

// ExecutorService (better approach)
var executor = Executors.newFixedThreadPool(4);
executor.submit(() -> System.out.println("Task done"));
executor.shutdown();""","java")
                ),
                exam = listOf(
                    q("java_a2_q1","Thread.start() vs run()?",listOf("Same","start() spawns new thread, run() executes on current","run() spawns","No difference"),1,"start() creates and starts a new thread; run() executes on the calling thread."),
                    q("java_a2_q2","ExecutorService benefit?",listOf("No benefit","Thread pool reuse — less overhead","More complex","Slower"),1,"ExecutorService manages a thread pool — reusing threads avoids creation overhead."),
                    q("java_a2_q3","synchronized keyword?",listOf("Speeds up code","Ensures one thread at a time in method","Creates thread","Stops thread"),1,"synchronized prevents race conditions by allowing only one thread at a time."),
                    q("java_a2_q4","volatile keyword?",listOf("Constant","Variable visible to all threads immediately","Private field","Static field"),1,"volatile ensures changes to a variable are visible across all threads."),
                    q("java_a2_q5","Race condition is?",listOf("Fast code","Multiple threads accessing shared data unsafely","A type of loop","A design pattern"),1,"Race condition: two threads read/write shared data simultaneously causing bugs.")
                )
            ),
            stage("java_a3","Design Patterns","📐","Singleton, Factory, Observer",
                lessons = listOf(
                    code("Singleton pattern","""public class Database {
    private static Database instance;
    private Database() {}
    
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }
}
Database db = Database.getInstance();""","java")
                ),
                exam = listOf(
                    q("java_a3_q1","Singleton ensures?",listOf("Many instances","Exactly one instance","Thread safety","Immutability"),1,"Singleton pattern restricts a class to one instance."),
                    q("java_a3_q2","Factory pattern?",listOf("Creates one object","Creates objects without specifying exact class","Stores objects","Destroys objects"),1,"Factory provides an interface for creating objects — subclasses decide which class to create."),
                    q("java_a3_q3","Observer pattern?",listOf("Database pattern","Objects subscribe to events of another object","Sorting algorithm","Creation pattern"),1,"Observer: subject notifies all observers when state changes (like event listeners)."),
                    q("java_a3_q4","Builder pattern is for?",listOf("Inheritance","Constructing complex objects step by step","Caching","Sorting"),1,"Builder pattern separates construction of complex objects from their representation."),
                    q("java_a3_q5","SOLID stands for?",listOf("A pattern","5 OOP design principles","A Java library","A framework"),1,"SOLID: Single responsibility, Open/closed, Liskov substitution, Interface segregation, Dependency inversion.")
                )
            ),
            stage("java_a4","Exception Handling","⚠️","Checked vs unchecked, custom exceptions",
                lessons = listOf(
                    code("Custom exception","""public class InsufficientFundsException extends Exception {
    private double amount;
    public InsufficientFundsException(double amount) {
        super("Insufficient funds: need " + amount + " more");
        this.amount = amount;
    }
    public double getAmount() { return amount; }
}""","java")
                ),
                exam = listOf(
                    q("java_a4_q1","Checked exception must be?",listOf("Caught or ignored","Caught or declared with throws","Only caught","Only declared"),1,"Checked exceptions must be caught or declared in method signature with throws."),
                    q("java_a4_q2","RuntimeException is?",listOf("Checked","Unchecked","Both","Neither"),1,"RuntimeException and its subclasses are unchecked — no mandatory handling."),
                    q("java_a4_q3","finally block?",listOf("Only on error","Only on success","Always runs","Optional"),2,"finally always executes — used for cleanup like closing files."),
                    q("java_a4_q4","multi-catch syntax?",listOf("catch(E1,E2)","catch(E1|E2 e)","catch(E1+E2)","catch(E1 & E2)"),1,"Multi-catch: catch(IOException | SQLException e) — handles multiple exception types."),
                    q("java_a4_q5","try-with-resources?",listOf("try(resource) auto-closes","Same as try-finally","Only for files","Java 11 only"),0,"try-with-resources auto-closes Closeable resources when done.")
                )
            ),
            stage("java_a5","Modern Java","🆕","Records, sealed classes, pattern matching",
                lessons = listOf(
                    code("Records (Java 16+)","""// Immutable data class — no boilerplate
public record Point(int x, int y) {}

Point p = new Point(3, 4);
System.out.println(p.x());      // 3
System.out.println(p);           // Point[x=3, y=4]""","java"),
                    code("Pattern matching (Java 16+)","""Object obj = "Hello";
if (obj instanceof String s) {
    System.out.println(s.toUpperCase()); // HELLO
}""","java")
                ),
                exam = listOf(
                    q("java_a5_q1","Java record?",listOf("Mutable class","Immutable data class with auto toString/equals","Abstract class","Interface"),1,"Records are immutable data classes — auto-generate constructor, getters, equals, hashCode, toString."),
                    q("java_a5_q2","Pattern matching instanceof?",listOf("Old instanceof","instanceof with variable binding","Reflection","Generics"),1,"if(obj instanceof String s) casts and binds in one step."),
                    q("java_a5_q3","Sealed class in Java?",listOf("Final class","Class with restricted permitted subclasses","Abstract class","Interface"),1,"Sealed classes restrict which classes can extend them — exhaustive switch."),
                    q("java_a5_q4","var keyword in Java?",listOf("Like JavaScript var","Local variable type inference","Global variable","Dynamic type"),1,"var lets the compiler infer the type of local variables."),
                    q("java_a5_q5","Text blocks (Java 15)?",listOf("String arrays","Multi-line string literals with \"\"\"","Formatted strings","String buffers"),1,"Text blocks: \"\"\"...\"\"\" — multi-line strings without escape characters.")
                )
            )
        )
    )

    // ═══════════════════════════════════════════════════════════════════════════
    // STUB courses for remaining 6 languages (full content same pattern)
    // C++, C, C#, Ruby, Dart, VB
    // ═══════════════════════════════════════════════════════════════════════════

    private fun cppBasic() = buildGenericCourse("cpp","C++","C++ Basics","Learn C++ — powerful systems programming.",
        "🔵",0xFF004488,"cpp_basic","C++ Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Variables & Types","Control Flow","Functions","Arrays & Pointers"))

    private fun cppAdvanced() = buildGenericCourse("cpp","C++","C++ Advanced","OOP, STL, templates and memory management.",
        "💎",0xFF004488,"cpp_adv","C++ Advanced Certificate", CourseLevel.ADVANCED,
        listOf("Classes & OOP","Templates","STL Containers","Memory Management","Modern C++17/20"))

    private fun cBasic() = buildGenericCourse("c","C","C Basics","Learn C — the mother of all languages.",
        "🔷",0xFF1C6BB0,"c_basic","C Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Data Types","Control Flow","Functions","Pointers & Arrays"))

    private fun cAdvanced() = buildGenericCourse("c","C","C Advanced","Memory, structs, file I/O and system programming.",
        "🛡",0xFF1C6BB0,"c_adv","C Advanced Certificate", CourseLevel.ADVANCED,
        listOf("Pointers Deep Dive","Structs & Unions","Dynamic Memory","File I/O","Preprocessor"))

    private fun csharpBasic() = buildGenericCourse("cs","C#","C# Basics","Learn C# — the language of .NET and Unity.",
        "💜",0xFF9B4F96,"csharp_basic","C# Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Variables & Types","Control Flow","Methods","Classes & Objects"))

    private fun csharpAdvanced() = buildGenericCourse("cs","C#","C# Advanced","LINQ, async/await, delegates and .NET.",
        "🔮",0xFF9B4F96,"csharp_adv","C# Advanced Certificate", CourseLevel.ADVANCED,
        listOf("OOP Deep Dive","LINQ","Async/Await","Delegates & Events","Unity Basics"))

    private fun rubyBasic() = buildGenericCourse("rb","Ruby","Ruby Basics","Learn Ruby — elegant and fun to write.",
        "🔴",0xFFCC342D,"ruby_basic","Ruby Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Variables & Types","Control Flow","Methods","Arrays & Hashes"))

    private fun rubyAdvanced() = buildGenericCourse("rb","Ruby","Ruby Advanced","Blocks, procs, lambdas, Rails basics.",
        "💍",0xFFCC342D,"ruby_adv","Ruby Advanced Certificate", CourseLevel.ADVANCED,
        listOf("OOP","Blocks & Procs","Modules & Mixins","Metaprogramming","Rails Basics"))

    private fun dartBasic() = buildGenericCourse("dart","Dart","Dart Basics","Learn Dart — the language behind Flutter.",
        "🎯",0xFF00B4AB,"dart_basic","Dart Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Variables & Types","Control Flow","Functions","Collections"))

    private fun dartAdvanced() = buildGenericCourse("dart","Dart","Dart Advanced","OOP, async, streams and Flutter basics.",
        "🚀",0xFF00B4AB,"dart_adv","Dart Advanced Certificate", CourseLevel.ADVANCED,
        listOf("Classes & OOP","Null Safety","Async & Futures","Streams","Flutter Basics"))

    private fun vbBasic() = buildGenericCourse("vb","Visual Basic","VB Basics","Learn Visual Basic .NET.",
        "🟦",0xFF5C2D91,"vb_basic","VB Basics Certificate", CourseLevel.BASIC,
        listOf("Introduction","Variables & Types","Control Flow","Procedures","Collections"))

    private fun vbAdvanced() = buildGenericCourse("vb","Visual Basic","VB Advanced","OOP, LINQ and .NET integration.",
        "🏅",0xFF5C2D91,"vb_adv","VB Advanced Certificate", CourseLevel.ADVANCED,
        listOf("Classes & OOP","Error Handling","LINQ","File I/O","Windows Forms"))

    // ── Generic course builder for stub courses ────────────────────────────────

    private fun buildGenericCourse(
        langId: String, langName: String, title: String, desc: String,
        icon: String, color: Long, badgeId: String, certTitle: String,
        level: CourseLevel, stageNames: List<String>
    ) = Course(
        id = "${langId}_${if(level==CourseLevel.BASIC)"basic" else "adv"}",
        languageId = langId, level = level, title = title,
        description = desc, icon = icon, color = color,
        badgeId = badgeId, certificateTitle = certTitle,
        stages = stageNames.mapIndexed { i, name ->
            stage("${langId}_${if(level==CourseLevel.BASIC)"b" else "a"}${i+1}",
                name, "📖", "Learn $name in $langName",
                lessons = listOf(
                    text("$name — Coming Soon","Full lesson content for $name in $langName is being prepared. You can already try the practice code in the IDE!"),
                    code("Example",when(langId){
                        "cpp"  -> "// $name example\n#include<iostream>\nusing namespace std;\nint main(){\n    cout<<\"Hello!\"<<endl;\n    return 0;\n}"
                        "c"    -> "// $name example\n#include<stdio.h>\nint main(){\n    printf(\"Hello!\\n\");\n    return 0;\n}"
                        "cs"   -> "// $name example\nusing System;\nclass Program{\n    static void Main(){\n        Console.WriteLine(\"Hello!\");\n    }\n}"
                        "rb"   -> "# $name example\nputs \"Hello!\""
                        "dart" -> "// $name example\nvoid main() {\n  print('Hello!');\n}"
                        "vb"   -> "' $name example\nModule Program\n    Sub Main()\n        Console.WriteLine(\"Hello!\")\n    End Sub\nEnd Module"
                        else   -> "// $name"
                    }, langId)
                ),
                exam = listOf(
                    q("${langId}_${i}_q1","What language is this course about?",
                        listOf("Python","Java",langName,"JavaScript"),2,"This course teaches $langName."),
                    q("${langId}_${i}_q2","Is $langName compiled or interpreted (general)?",
                        when(langId){
                            "py","rb" -> listOf("Compiled","Interpreted","Both","Neither")
                            else -> listOf("Compiled","Interpreted","Both","Neither")
                        },
                        when(langId){"py","rb"->1;"js"->1;else->0},
                        "Compilation model for $langName."),
                    q("${langId}_${i}_q3","Which company maintains $langName?",
                        when(langId){
                            "cpp","c" -> listOf("ISO/IEC","Google","Oracle","Microsoft")
                            "cs","vb" -> listOf("Google","Oracle","Microsoft","Apple")
                            "dart" -> listOf("Google","Oracle","JetBrains","Apple")
                            "rb" -> listOf("Matz/Community","Oracle","Google","Microsoft")
                            else -> listOf("ISO","Community","Google","Oracle")
                        },
                        when(langId){"cpp","c"->0;"cs","vb"->2;"dart"->0;"rb"->0;else->2},
                        "$langName maintenance info."),
                    q("${langId}_${i}_q4","$langName is used for?",
                        when(langId){
                            "cpp","c" -> listOf("Web only","Mobile only","Systems/games/embedded","Databases only")
                            "cs" -> listOf("Web only","Android only",".NET/Unity/Windows","Database only")
                            "dart" -> listOf("Servers only","Flutter/mobile/web","Embedded only","Desktop only")
                            "rb" -> listOf("Mobile only","Web (Rails), scripting","Embedded","Games")
                            "vb" -> listOf("Mobile","Windows/.NET desktop","Embedded","Android")
                            else -> listOf("A","B","C","D")
                        },2,"Primary use cases for $langName."),
                    q("${langId}_${i}_q5","Good practice in any language is?",
                        listOf("No comments","Meaningful variable names","Short variable names","Avoid functions"),1,"Clear, meaningful names make code readable and maintainable.")
                )
            )
        }
    )

    // ── Builder helpers ────────────────────────────────────────────────────────

    private fun stage(id:String, name:String, icon:String, desc:String,
                      lessons:List<LessonCard>, exam:List<ExamQuestion>) =
        CourseStage(id=id, name=name, icon=icon, description=desc,
            lessons=lessons, examQuestions=exam, passMark=70)

    private fun text(title:String, body:String) =
        LessonCard(type=LessonType.TEXT, title=title, body=body)

    private fun code(title:String, code:String, lang:String) =
        LessonCard(type=LessonType.CODE, title=title, code=code, language=lang)

    private fun tip(title:String, body:String) =
        LessonCard(type=LessonType.TIP, title=title, body=body)

    private fun q(id:String, question:String, options:List<String>,
                  correct:Int, explanation:String) =
        ExamQuestion(id=id, question=question, options=options,
            correctIndex=correct, explanation=explanation)
}
