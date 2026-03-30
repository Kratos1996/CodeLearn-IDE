package com.codelearn.ide.ai

import com.codelearn.ide.model.Language

// ─── Result types ─────────────────────────────────────────────────────────────

data class AiCompletion(
    val suggestion: String,
    val label: String,
    val explanation: String = "",
    val isSnippet: Boolean = false
)

data class AiErrorAnalysis(
    val errorTitle: String,
    val plainExplanation: String,
    val fixedCode: String = "",
    val tip: String = ""
)

data class AiCodeReview(
    val issues: List<AiIssue>,
    val overallFeedback: String
)

data class AiIssue(
    val line: Int,
    val severity: IssueSeverity,
    val message: String,
    val suggestion: String = ""
)

enum class IssueSeverity { ERROR, WARNING, INFO }

// ─── Code Intelligence Engine — 100% offline, zero cost ──────────────────────
// Built-in rule engine: error detection, smart autocomplete, code review,
// student-friendly explanations. No API key, no internet needed.

object CodeIntelligence {

    // ═════════════════════════════════════════════════════════════════════════
    // 1. SMART AUTOCOMPLETE
    // Analyses what the student is typing and suggests completions
    // ═════════════════════════════════════════════════════════════════════════

    fun getCompletions(codeBeforeCursor: String, language: Language): List<AiCompletion> {
        val lines = codeBeforeCursor.lines()
        val currentLine = lines.lastOrNull() ?: return emptyList()
        val trimmed = currentLine.trimStart()

        val results = mutableListOf<AiCompletion>()

        // Collect all variable/function names declared in the code so far
        val declaredSymbols = extractDeclaredSymbols(codeBeforeCursor, language)

        // What is the student currently typing?
        val wordBeforeCursor = trimmed.takeLastWhile { it.isLetterOrDigit() || it == '_' }

        // ── Context-aware snippet completions ─────────────────────────────────
        results += getSnippets(trimmed, wordBeforeCursor, language)

        // ── Symbol completions from their own code ─────────────────────────────
        results += declaredSymbols
            .filter { it.startsWith(wordBeforeCursor, ignoreCase = true) && it != wordBeforeCursor }
            .take(4)
            .map { AiCompletion(it, it, "Declared in your code") }

        // ── Language built-in completions ──────────────────────────────────────
        results += getBuiltinCompletions(wordBeforeCursor, language)

        return results.distinctBy { it.label }.take(7)
    }

    private fun getSnippets(line: String, word: String, lang: Language): List<AiCompletion> {
        val snippets = mutableListOf<AiCompletion>()
        when (lang) {
            Language.KOTLIN, Language.JAVA -> {
                if ("for".startsWith(word, true))
                    snippets += AiCompletion("for (i in 0..n) {\n    \n}", "for loop", "Iterate with index", true)
                if ("fun".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("fun name(param: Type): ReturnType {\n    \n}", "fun", "Declare a function", true)
                if ("if".startsWith(word, true))
                    snippets += AiCompletion("if (condition) {\n    \n} else {\n    \n}", "if/else", "Conditional block", true)
                if ("when".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("when (value) {\n    x -> \n    else -> \n}", "when", "Kotlin when expression", true)
                if ("class".startsWith(word, true))
                    snippets += AiCompletion("class Name {\n    \n}", "class", "Declare a class", true)
                if ("try".startsWith(word, true))
                    snippets += AiCompletion("try {\n    \n} catch (e: Exception) {\n    \n}", "try/catch", "Handle exceptions", true)
                if ("println".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("println(\"\")", "println", "Print a line to console")
                if ("print".startsWith(word, true) && lang == Language.JAVA)
                    snippets += AiCompletion("System.out.println(\"\");", "println", "Print a line to console")
                if ("list".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("listOf()", "listOf", "Create a list")
                if ("map".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("mapOf()", "mapOf", "Create a map")
                if ("null".startsWith(word, true) && lang == Language.KOTLIN)
                    snippets += AiCompletion("?: return null", "?:", "Elvis operator — null safety")
            }
            Language.PYTHON -> {
                if ("def".startsWith(word, true))
                    snippets += AiCompletion("def name(param):\n    pass", "def", "Define a function", true)
                if ("for".startsWith(word, true))
                    snippets += AiCompletion("for i in range(n):\n    ", "for loop", "Iterate with range", true)
                if ("if".startsWith(word, true))
                    snippets += AiCompletion("if condition:\n    \nelse:\n    ", "if/else", "Conditional block", true)
                if ("class".startsWith(word, true))
                    snippets += AiCompletion("class Name:\n    def __init__(self):\n        pass", "class", "Define a class", true)
                if ("try".startsWith(word, true))
                    snippets += AiCompletion("try:\n    \nexcept Exception as e:\n    ", "try/except", "Handle exceptions", true)
                if ("print".startsWith(word, true))
                    snippets += AiCompletion("print(\"\")", "print", "Print to console")
                if ("list".startsWith(word, true))
                    snippets += AiCompletion("my_list = []", "list", "Create an empty list")
                if ("dict".startsWith(word, true))
                    snippets += AiCompletion("my_dict = {}", "dict", "Create an empty dictionary")
                if ("lambda".startsWith(word, true))
                    snippets += AiCompletion("lambda x: x", "lambda", "Anonymous function")
                if ("import".startsWith(word, true))
                    snippets += AiCompletion("import ", "import", "Import a module")
                if ("with".startsWith(word, true))
                    snippets += AiCompletion("with open('file') as f:\n    ", "with open", "Open file safely")
            }
            Language.JAVASCRIPT -> {
                if ("function".startsWith(word, true))
                    snippets += AiCompletion("function name(param) {\n    \n}", "function", "Declare a function", true)
                if ("const".startsWith(word, true))
                    snippets += AiCompletion("const name = ;", "const", "Declare constant")
                if ("let".startsWith(word, true))
                    snippets += AiCompletion("let name = ;", "let", "Declare variable")
                if ("for".startsWith(word, true))
                    snippets += AiCompletion("for (let i = 0; i < n; i++) {\n    \n}", "for loop", "Classic for loop", true)
                if ("arrow".startsWith(word, true) || "=>".startsWith(word))
                    snippets += AiCompletion("const fn = (param) => {\n    \n};", "arrow fn", "Arrow function", true)
                if ("if".startsWith(word, true))
                    snippets += AiCompletion("if (condition) {\n    \n} else {\n    \n}", "if/else", "Conditional", true)
                if ("console".startsWith(word, true))
                    snippets += AiCompletion("console.log();", "console.log", "Log to console")
                if ("class".startsWith(word, true))
                    snippets += AiCompletion("class Name {\n    constructor() {\n        \n    }\n}", "class", "ES6 class", true)
                if ("try".startsWith(word, true))
                    snippets += AiCompletion("try {\n    \n} catch (e) {\n    \n}", "try/catch", "Handle errors", true)
                if ("async".startsWith(word, true))
                    snippets += AiCompletion("async function name() {\n    await \n}", "async fn", "Async function", true)
                if ("promise".startsWith(word, true))
                    snippets += AiCompletion("new Promise((resolve, reject) => {\n    \n})", "Promise", "Create a promise", true)
            }
            Language.CPP, Language.C -> {
                if ("for".startsWith(word, true))
                    snippets += AiCompletion("for (int i = 0; i < n; i++) {\n    \n}", "for loop", "Classic for loop", true)
                if ("if".startsWith(word, true))
                    snippets += AiCompletion("if (condition) {\n    \n} else {\n    \n}", "if/else", "Conditional", true)
                if ("cout".startsWith(word, true) && lang == Language.CPP)
                    snippets += AiCompletion("cout << \"\" << endl;", "cout", "Print to console")
                if ("printf".startsWith(word, true))
                    snippets += AiCompletion("printf(\"%s\\n\", \"\");", "printf", "Formatted print")
                if ("void".startsWith(word, true))
                    snippets += AiCompletion("void functionName() {\n    \n}", "void fn", "Void function", true)
                if ("int".startsWith(word, true))
                    snippets += AiCompletion("int main() {\n    \n    return 0;\n}", "main", "Main function", true)
                if ("struct".startsWith(word, true))
                    snippets += AiCompletion("struct Name {\n    \n};", "struct", "Define a struct", true)
                if ("include".startsWith(word, true) || "#".startsWith(word))
                    snippets += AiCompletion("#include <iostream>", "#include", "Include header")
            }
            Language.RUBY -> {
                if ("def".startsWith(word, true))
                    snippets += AiCompletion("def method_name\n  \nend", "def", "Define method", true)
                if ("class".startsWith(word, true))
                    snippets += AiCompletion("class Name\n  def initialize\n    \n  end\nend", "class", "Define class", true)
                if ("each".startsWith(word, true))
                    snippets += AiCompletion(".each do |item|\n  \nend", "each", "Iterate collection", true)
                if ("puts".startsWith(word, true))
                    snippets += AiCompletion("puts \"\"", "puts", "Print line")
                if ("if".startsWith(word, true))
                    snippets += AiCompletion("if condition\n  \nelse\n  \nend", "if/else", "Conditional", true)
            }
            else -> {}
        }
        return snippets
    }

    private fun getBuiltinCompletions(word: String, lang: Language): List<AiCompletion> {
        if (word.length < 2) return emptyList()
        val builtins = when (lang) {
            Language.KOTLIN -> listOf(
                "println" to "Print line", "print" to "Print", "readLine" to "Read input",
                "listOf" to "Immutable list", "mutableListOf" to "Mutable list",
                "mapOf" to "Immutable map", "mutableMapOf" to "Mutable map",
                "setOf" to "Immutable set", "arrayOf" to "Array",
                "IntArray" to "Int array type", "String" to "String type",
                "Int" to "Integer type", "Boolean" to "Boolean type",
                "TODO" to "Not implemented marker"
            )
            Language.PYTHON -> listOf(
                "print" to "Print output", "input" to "Read input",
                "len" to "Get length", "range" to "Number range",
                "list" to "Convert to list", "dict" to "Convert to dict",
                "str" to "Convert to string", "int" to "Convert to int",
                "float" to "Convert to float", "bool" to "Convert to bool",
                "enumerate" to "Enumerate with index", "zip" to "Zip iterables",
                "map" to "Apply function", "filter" to "Filter items",
                "sorted" to "Sort items", "reversed" to "Reverse items",
                "isinstance" to "Check type", "hasattr" to "Check attribute",
                "open" to "Open file", "type" to "Get type"
            )
            Language.JAVASCRIPT -> listOf(
                "console" to "Console object", "Math" to "Math object",
                "Array" to "Array class", "Object" to "Object class",
                "String" to "String class", "Number" to "Number class",
                "parseInt" to "Parse integer", "parseFloat" to "Parse float",
                "isNaN" to "Check if NaN", "JSON" to "JSON utilities",
                "Promise" to "Promise class", "fetch" to "HTTP requests",
                "setTimeout" to "Delayed execution", "setInterval" to "Repeated execution",
                "document" to "DOM document", "window" to "Browser window"
            )
            Language.CPP -> listOf(
                "cout" to "Output stream", "cin" to "Input stream",
                "endl" to "End line", "string" to "String type",
                "vector" to "Dynamic array", "map" to "Key-value map",
                "set" to "Unique set", "pair" to "Pair of values",
                "sort" to "Sort elements", "find" to "Find element",
                "push_back" to "Add to vector", "size" to "Get size",
                "begin" to "Begin iterator", "end" to "End iterator"
            )
            Language.JAVA -> listOf(
                "System" to "System class", "String" to "String class",
                "Integer" to "Integer wrapper", "ArrayList" to "Dynamic list",
                "HashMap" to "Hash map", "Scanner" to "Input scanner",
                "Math" to "Math utilities", "Arrays" to "Array utilities",
                "Collections" to "Collection utilities", "StringBuilder" to "Mutable string"
            )
            else -> emptyList()
        }
        return builtins
            .filter { it.first.startsWith(word, ignoreCase = true) }
            .take(5)
            .map { AiCompletion(it.first, it.first, it.second) }
    }

    private fun extractDeclaredSymbols(code: String, language: Language): List<String> {
        val symbols = mutableListOf<String>()
        val lines = code.lines()

        val patterns = when (language) {
            Language.KOTLIN -> listOf(
                Regex("""(?:val|var)\s+(\w+)"""),
                Regex("""fun\s+(\w+)\s*\("""),
                Regex("""class\s+(\w+)"""),
                Regex("""data class\s+(\w+)""")
            )
            Language.PYTHON -> listOf(
                Regex("""^(\w+)\s*="""),
                Regex("""def\s+(\w+)\s*\("""),
                Regex("""class\s+(\w+)"""),
                Regex("""for\s+(\w+)\s+in""")
            )
            Language.JAVASCRIPT -> listOf(
                Regex("""(?:const|let|var)\s+(\w+)"""),
                Regex("""function\s+(\w+)\s*\("""),
                Regex("""class\s+(\w+)"""),
                Regex("""(\w+)\s*=\s*\(.*\)\s*=>""")
            )
            else -> listOf(
                Regex("""(?:int|String|bool|double|float|char|void)\s+(\w+)"""),
                Regex("""(?:fun|def|void|func)\s+(\w+)\s*\(""")
            )
        }
        for (line in lines) {
            for (pattern in patterns) {
                pattern.find(line.trim())?.groupValues?.getOrNull(1)?.let {
                    if (it.length > 1) symbols += it
                }
            }
        }
        return symbols.distinct()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. ERROR ANALYSER
    // Takes the raw compiler error and produces student-friendly explanation
    // ═════════════════════════════════════════════════════════════════════════

    fun explainError(code: String, errorOutput: String, language: Language): AiErrorAnalysis {
        val error = errorOutput.lowercase()
        val lines = code.lines()

        // Extract line number from error if present
        val lineNum = Regex("""[:\s](\d+)[:\s]""").find(errorOutput)?.groupValues?.get(1)?.toIntOrNull()
        val errorLine = lineNum?.let { lines.getOrNull(it - 1)?.trim() } ?: ""

        // ── Match common error patterns ────────────────────────────────────────
        return when {
            // ── Missing semicolon ──────────────────────────────────────────────
            error.contains("expected ';'") || error.contains("missing ';'") ->
                AiErrorAnalysis(
                    errorTitle = "Missing semicolon ';'",
                    plainExplanation = "You forgot to put a semicolon `;` at the end of a statement.\n" +
                        (if (errorLine.isNotBlank()) "Check this line: `$errorLine`\n" else "") +
                        "In ${language.displayName}, every statement must end with `;`.",
                    fixedCode = tryFixMissingSemicolon(code, language),
                    tip = "Every statement line in ${language.displayName} needs a `;` at the end."
                )

            // ── Missing colon (Python) ─────────────────────────────────────────
            error.contains("expected ':'") && language == Language.PYTHON ->
                AiErrorAnalysis(
                    errorTitle = "Missing colon ':'",
                    plainExplanation = "In Python, you need a `:` at the end of `if`, `for`, `while`, `def`, and `class` lines.\n" +
                        (if (errorLine.isNotBlank()) "Check this line: `$errorLine`" else ""),
                    fixedCode = "",
                    tip = "Python blocks always start with a colon — `if x > 0:` not `if x > 0`"
                )

            // ── Indentation error (Python) ─────────────────────────────────────
            error.contains("indentationerror") || error.contains("unexpected indent") ->
                AiErrorAnalysis(
                    errorTitle = "Indentation Error",
                    plainExplanation = "Python uses spaces/tabs to know which code is inside a block.\n" +
                        "Make sure all lines inside an `if`, `for`, `def`, or `class` are indented consistently.\n" +
                        (if (errorLine.isNotBlank()) "Problem near: `$errorLine`" else ""),
                    fixedCode = "",
                    tip = "Use 4 spaces (not tabs) for indentation in Python."
                )

            // ── Undefined / undeclared variable ───────────────────────────────
            error.contains("unresolved reference") || error.contains("undefined") ||
            error.contains("is not defined") || error.contains("undeclared") || error.contains("cannot find symbol") ->
                AiErrorAnalysis(
                    errorTitle = "Variable or Function Not Found",
                    plainExplanation = buildUndefinedExplanation(errorOutput, code, language),
                    fixedCode = "",
                    tip = "Make sure you declare a variable before using it. Check spelling — `myVar` and `myvar` are different!"
                )

            // ── Type mismatch ─────────────────────────────────────────────────
            error.contains("type mismatch") || error.contains("incompatible types") ||
            error.contains("cannot convert") || error.contains("typeerror") ->
                AiErrorAnalysis(
                    errorTitle = "Wrong Type",
                    plainExplanation = buildTypeMismatchExplanation(errorOutput, language),
                    fixedCode = "",
                    tip = "Each variable has a type (like Int, String, Boolean). You cannot mix them without converting first."
                )

            // ── Missing return ─────────────────────────────────────────────────
            error.contains("missing return") || error.contains("must return a value") ||
            error.contains("not all code paths return") ->
                AiErrorAnalysis(
                    errorTitle = "Missing return statement",
                    plainExplanation = "Your function says it will return a value, but some paths don't return anything.\n" +
                        "Make sure every possible path through the function has a `return` statement.",
                    fixedCode = "",
                    tip = "If a function has a return type, every branch (if, else, when) must return a value."
                )

            // ── Null pointer / null safety ─────────────────────────────────────
            error.contains("nullpointerexception") || error.contains("null pointer") ||
            error.contains("null safety") || error.contains("only safe") ->
                AiErrorAnalysis(
                    errorTitle = "Null Pointer — value is null",
                    plainExplanation = "You are trying to use a value that is `null` (empty/nothing).\n" +
                        "This is one of the most common errors in programming.\n" +
                        if (language == Language.KOTLIN)
                            "In Kotlin, use `?.` (safe call) or `?:` (elvis operator) to handle nulls safely."
                        else
                            "Always check if something is null before using it: `if (value != null)`",
                    fixedCode = "",
                    tip = "Before using a variable, ask: 'could this ever be null?' If yes, handle that case."
                )

            // ── Index out of bounds ────────────────────────────────────────────
            error.contains("index out of bounds") || error.contains("indexerror") ||
            error.contains("arrayindexoutofbounds") ->
                AiErrorAnalysis(
                    errorTitle = "Index Out of Bounds",
                    plainExplanation = "You are trying to access a position in a list/array that doesn't exist.\n" +
                        "For example, if a list has 3 items, valid positions are 0, 1, 2 — not 3 or higher.\n" +
                        "Check your loop bounds and make sure index < size.",
                    fixedCode = "",
                    tip = "Arrays start at index 0. A list with 5 items has indices 0..4, NOT 1..5."
                )

            // ── Missing bracket or brace ──────────────────────────────────────
            error.contains("expecting '}'") || error.contains("unexpected end of file") ||
            error.contains("unexpected eof") || error.contains("unexpected token") ->
                AiErrorAnalysis(
                    errorTitle = "Missing closing bracket or brace",
                    plainExplanation = "Your code has a `{` or `(` that was never closed.\n" +
                        "Count your opening `{` and closing `}` — they must match.\n" +
                        "Most IDEs show matching brackets — click on a `{` to see its pair.",
                    fixedCode = tryFixMissingBrace(code),
                    tip = "For every `{` you open, you need a `}` to close it. Same with `(` and `)`."
                )

            // ── Division by zero ──────────────────────────────────────────────
            error.contains("division by zero") || error.contains("/ by zero") ->
                AiErrorAnalysis(
                    errorTitle = "Division by Zero",
                    plainExplanation = "Your code is trying to divide a number by zero, which is mathematically impossible.\n" +
                        "Add a check before dividing: `if (divisor != 0) { result = a / divisor }`",
                    fixedCode = "",
                    tip = "Always check the divisor is not zero before dividing: `if (b != 0) a / b`"
                )

            // ── Stack overflow / infinite recursion ───────────────────────────
            error.contains("stackoverflow") || error.contains("maximum recursion") ->
                AiErrorAnalysis(
                    errorTitle = "Infinite Recursion (Stack Overflow)",
                    plainExplanation = "Your function is calling itself forever without stopping.\n" +
                        "Every recursive function needs a 'base case' — a condition where it stops calling itself.",
                    fixedCode = "",
                    tip = "Recursive functions need a stopping condition. Example: `if (n <= 0) return 0`"
                )

            // ── Wrong number of arguments ─────────────────────────────────────
            error.contains("arguments") && (error.contains("expected") || error.contains("required")) ->
                AiErrorAnalysis(
                    errorTitle = "Wrong Number of Arguments",
                    plainExplanation = "You are calling a function with the wrong number of inputs.\n" +
                        "Check the function definition to see how many parameters it expects.",
                    fixedCode = "",
                    tip = "Count the commas in the function call. It must match the function definition."
                )

            // ── Compilation error (generic) ────────────────────────────────────
            error.contains("error:") || error.contains("syntaxerror") ->
                AiErrorAnalysis(
                    errorTitle = "Syntax Error",
                    plainExplanation = buildGenericSyntaxExplanation(errorOutput, errorLine, language),
                    fixedCode = "",
                    tip = "Read the error message carefully — it usually tells you the line number and what was expected."
                )

            // ── Runtime error (generic) ────────────────────────────────────────
            error.contains("exception") || error.contains("error") ->
                AiErrorAnalysis(
                    errorTitle = "Runtime Error",
                    plainExplanation = "Your code compiled but crashed while running.\n" +
                        "Error: ${errorOutput.lines().firstOrNull { it.isNotBlank() }?.take(120) ?: errorOutput.take(120)}\n\n" +
                        "Check for: division by zero, accessing null, or index out of bounds.",
                    fixedCode = "",
                    tip = "Runtime errors happen while the program runs. Add print statements to find where it crashes."
                )

            else ->
                AiErrorAnalysis(
                    errorTitle = "Error",
                    plainExplanation = "Your code has an error:\n${errorOutput.lines().take(5).joinToString("\n")}\n\n" +
                        "Check the line number mentioned in the error and look for typos or missing symbols.",
                    fixedCode = "",
                    tip = "Error messages tell you the line number. Go to that line and check it carefully."
                )
        }
    }

    private fun buildUndefinedExplanation(error: String, code: String, lang: Language): String {
        // Extract the name that is undefined
        val name = Regex("""['"'`](\w+)['"'`]""").find(error)?.groupValues?.get(1)
            ?: Regex("""reference:\s*(\w+)""").find(error)?.groupValues?.get(1)
            ?: Regex("""symbol:\s*(?:variable|method|class)?\s*(\w+)""").find(error)?.groupValues?.get(1)

        return buildString {
            if (name != null) {
                appendLine("'$name' is not defined anywhere in your code.")
                appendLine()
                appendLine("Possible causes:")
                appendLine("• Typo — did you spell it correctly?")
                appendLine("• You forgot to declare it. In ${lang.displayName}:")
                when (lang) {
                    Language.KOTLIN     -> appendLine("  val $name = ... or var $name = ...")
                    Language.JAVA       -> appendLine("  int $name = 0; (or whatever type)")
                    Language.PYTHON     -> appendLine("  $name = ...  (just assign it)")
                    Language.JAVASCRIPT -> appendLine("  const $name = ... or let $name = ...")
                    Language.CPP, Language.C -> appendLine("  int $name = 0; (declare before using)")
                    else -> {}
                }
                appendLine("• You declared it inside a function but used it outside")
            } else {
                appendLine("A variable, function, or class name was used that doesn't exist in your code.")
                appendLine("Check spelling and make sure you declared it before using it.")
            }
        }
    }

    private fun buildTypeMismatchExplanation(error: String, lang: Language): String {
        return buildString {
            appendLine("You are trying to use a value of the wrong type.")
            appendLine()
            appendLine("For example, you cannot:")
            appendLine("• Add a number and text directly: 5 + \"hello\"")
            appendLine("• Put a text (String) into a number (Int) variable")
            appendLine()
            appendLine("To fix it, convert the value:")
            when (lang) {
                Language.KOTLIN -> appendLine("  number.toString() → converts Int to String\n  string.toInt() → converts String to Int")
                Language.JAVA -> appendLine("  String.valueOf(number) → converts number to String\n  Integer.parseInt(str) → converts String to int")
                Language.PYTHON -> appendLine("  str(number) → convert to string\n  int(text) → convert to int")
                Language.JAVASCRIPT -> appendLine("  String(number) or number.toString()\n  Number(text) or parseInt(text)")
                else -> appendLine("  Use the language's built-in conversion functions.")
            }
        }
    }

    private fun buildGenericSyntaxExplanation(error: String, errorLine: String, lang: Language): String {
        return buildString {
            appendLine("Your code has a syntax error — the code is not written in the correct form.")
            if (errorLine.isNotBlank()) {
                appendLine()
                appendLine("Problem near: `$errorLine`")
            }
            val firstError = error.lines().firstOrNull { it.contains("error:") || it.contains("Error:") }
            if (firstError != null) {
                appendLine()
                appendLine("Message: ${firstError.trim().take(120)}")
            }
            appendLine()
            appendLine("Common syntax mistakes in ${lang.displayName}:")
            when (lang) {
                Language.KOTLIN -> appendLine("• Missing `:` in type annotation — `val x: Int`\n• Missing `{` or `}` around blocks\n• Missing `fun` keyword before function")
                Language.PYTHON -> appendLine("• Missing `:` after if/for/def/class\n• Wrong indentation (use 4 spaces)\n• Mixing tabs and spaces")
                Language.JAVASCRIPT -> appendLine("• Missing `;` at end of statement\n• Missing `{` or `}`\n• Unclosed string quotes")
                Language.JAVA -> appendLine("• Missing `;` at end of statement\n• Missing `{` or `}`\n• Missing return type on method")
                Language.CPP, Language.C -> appendLine("• Missing `;` after statements\n• Missing `#include` at top\n• Undeclared variable type")
                else -> appendLine("• Check for missing brackets, semicolons, or keywords")
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. CODE REVIEW
    // Scans code for common bugs, style issues, and improvements
    // ═════════════════════════════════════════════════════════════════════════

    fun reviewCode(code: String, language: Language): AiCodeReview {
        val issues = mutableListOf<AiIssue>()
        val lines = code.lines()

        for ((idx, rawLine) in lines.withIndex()) {
            val lineNum = idx + 1
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("//") || line.startsWith("#")) continue

            // ── Universal checks ───────────────────────────────────────────────

            // Very long lines
            if (rawLine.length > 120)
                issues += AiIssue(lineNum, IssueSeverity.INFO,
                    "Line is ${rawLine.length} characters long — consider breaking it up",
                    "Split long lines to improve readability")

            // TODO / FIXME left in
            if (line.contains("TODO") || line.contains("FIXME") || line.contains("HACK"))
                issues += AiIssue(lineNum, IssueSeverity.WARNING,
                    "Unfinished code marker found: '${line.take(40)}'",
                    "Complete or remove this TODO/FIXME before submitting")

            // ── Language-specific checks ───────────────────────────────────────
            when (language) {
                Language.KOTLIN -> reviewKotlinLine(line, lineNum, lines, issues)
                Language.JAVA   -> reviewJavaLine(line, lineNum, issues)
                Language.PYTHON -> reviewPythonLine(line, lineNum, lines, issues)
                Language.JAVASCRIPT -> reviewJavaScriptLine(line, lineNum, issues)
                Language.CPP, Language.C -> reviewCppLine(line, lineNum, issues)
                else -> {}
            }
        }

        // ── Overall structural checks ──────────────────────────────────────────
        reviewStructure(code, language, issues)

        val overallFeedback = buildOverallFeedback(code, issues, language)
        return AiCodeReview(issues.take(10), overallFeedback)
    }

    private fun reviewKotlinLine(line: String, lineNum: Int, allLines: List<String>, issues: MutableList<AiIssue>) {
        // var used when val would work (heuristic: assigned once)
        if (line.matches(Regex("""var \w+ = .+"""))) {
            val varName = Regex("""var (\w+)""").find(line)?.groupValues?.get(1)
            if (varName != null) {
                val assignCount = allLines.count { it.contains("$varName =") || it.contains("$varName++") || it.contains("$varName--") }
                if (assignCount == 1)
                    issues += AiIssue(lineNum, IssueSeverity.INFO,
                        "Consider using `val` instead of `var` for '$varName' if it's never reassigned",
                        "Replace `var` with `val` for immutable variables")
            }
        }
        // !! force-unwrap
        if (line.contains("!!"))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Using `!!` force-unwrap can crash if value is null",
                "Use `?.` safe call or `?: default` instead of `!!`")
        // println inside loop heuristic
        if ((line.contains("for") || line.contains("while")) && line.contains("println"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "Printing inside a loop can produce a lot of output",
                "Consider collecting results and printing once after the loop")
        // Empty catch
        if (line.contains("catch") && (line.contains("{}") || line.contains("{ }")))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Empty catch block — errors are silently ignored",
                "Log or handle the exception in the catch block")
    }

    private fun reviewJavaLine(line: String, lineNum: Int, issues: MutableList<AiIssue>) {
        // == on strings
        if (line.contains("== \"") || line.contains("\" =="))
            issues += AiIssue(lineNum, IssueSeverity.ERROR,
                "Using `==` to compare Strings compares references, not content",
                "Use `.equals()` instead: `str.equals(\"value\")` or `\"value\".equals(str)`")
        // System.out.println in loop
        if (line.contains("System.out.print") && line.contains("for"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "Printing inside loops is slow for large data",
                "Use StringBuilder and print once after the loop")
        // Catching Exception base class
        if (line.contains("catch (Exception "))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "Catching generic Exception hides specific errors",
                "Catch specific exceptions like IOException, NullPointerException")
        // Empty catch
        if (line.contains("catch") && (line.contains("{}") || line.contains("{ }")))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Empty catch block — exception is silently swallowed",
                "At minimum, print the error: `e.printStackTrace()`")
    }

    private fun reviewPythonLine(line: String, lineNum: Int, allLines: List<String>, issues: MutableList<AiIssue>) {
        // Bare except
        if (line.trimStart().startsWith("except:") || line.trimStart() == "except :")
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Bare `except:` catches ALL exceptions including keyboard interrupt",
                "Specify the exception type: `except ValueError:` or `except Exception as e:`")
        // == True / == False
        if (line.contains("== True") || line.contains("== False"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "Comparing to True/False explicitly is not Pythonic",
                "Use `if condition:` instead of `if condition == True:`")
        // Mutable default argument
        if (line.matches(Regex("""def \w+\(.*=\s*[\[\{].*\).*:""")))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Mutable default argument (list/dict) is shared across all calls",
                "Use `None` as default: `def f(x=None): if x is None: x = []`")
        // print without parentheses (Python 2 style)
        if (line.matches(Regex("""print\s+[^(].*""")))
            issues += AiIssue(lineNum, IssueSeverity.ERROR,
                "`print` without parentheses is Python 2 syntax",
                "Use `print(...)` with parentheses — this is Python 3")
        // range(len(...)) anti-pattern
        if (line.contains("range(len("))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "`range(len(x))` is not Pythonic",
                "Use `for item in x:` or `for i, item in enumerate(x):`")
    }

    private fun reviewJavaScriptLine(line: String, lineNum: Int, issues: MutableList<AiIssue>) {
        // == instead of ===
        if (Regex("""[^=!<>]==[^=]""").containsMatchIn(line) && !line.trimStart().startsWith("//"))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "Using `==` does type coercion — can cause unexpected results",
                "Use `===` (strict equality) instead of `==`")
        // var instead of const/let
        if (line.trimStart().startsWith("var "))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "`var` has function scope and can cause bugs",
                "Use `const` for values that don't change, `let` for variables")
        // console.log left in
        if (line.contains("console.log("))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "console.log() found — remove debug logs before production",
                "Remove or comment out console.log() when done debugging")
        // == null (could use nullish)
        if (line.contains("== null") || line.contains("=== null"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "Checking for null explicitly",
                "Consider nullish coalescing: `value ?? defaultValue`")
    }

    private fun reviewCppLine(line: String, lineNum: Int, issues: MutableList<AiIssue>) {
        // gets() - unsafe
        if (line.contains("gets("))
            issues += AiIssue(lineNum, IssueSeverity.ERROR,
                "`gets()` is unsafe and removed in C11 — causes buffer overflows",
                "Use `fgets(buf, size, stdin)` instead")
        // malloc without free check
        if (line.contains("malloc(") && !line.contains("if") && !line.contains("!= NULL"))
            issues += AiIssue(lineNum, IssueSeverity.WARNING,
                "malloc() result not checked for NULL",
                "Always check: `if (ptr == NULL) { /* handle error */ }`")
        // using namespace std in header
        if (line.contains("using namespace std"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "`using namespace std` can cause name conflicts in large projects",
                "Prefer explicit `std::cout` over `using namespace std`")
        // endl vs \\n
        if (line.contains("endl") && line.contains("for"))
            issues += AiIssue(lineNum, IssueSeverity.INFO,
                "`endl` flushes the buffer which is slow in loops",
                "Use `'\\n'` instead of `endl` inside loops for better performance")
    }

    private fun reviewStructure(code: String, language: Language, issues: MutableList<AiIssue>) {
        val lines = code.lines()
        val totalLines = lines.size

        // Check for very long functions
        if (language in listOf(Language.KOTLIN, Language.JAVA, Language.CPP, Language.C)) {
            val openBraces = code.count { it == '{' }
            val closeBraces = code.count { it == '}' }
            if (openBraces != closeBraces)
                issues.add(0, AiIssue(0, IssueSeverity.ERROR,
                    "Mismatched braces: $openBraces `{` but $closeBraces `}` — code will not compile",
                    "Count your opening and closing braces carefully"))
        }

        // Python indentation consistency check
        if (language == Language.PYTHON) {
            val indentedLines = lines.filter { it.startsWith("\t") }
            val spacedLines = lines.filter { it.startsWith("    ") }
            if (indentedLines.isNotEmpty() && spacedLines.isNotEmpty())
                issues.add(0, AiIssue(0, IssueSeverity.ERROR,
                    "Mixed tabs and spaces — Python requires one consistent style",
                    "Use 4 spaces throughout. Never mix tabs and spaces."))
        }

        // No main function / entry point
        val hasMain = when (language) {
            Language.KOTLIN     -> code.contains("fun main(")
            Language.JAVA       -> code.contains("public static void main(")
            Language.PYTHON     -> true // scripts don't need main
            Language.JAVASCRIPT -> true // scripts don't need main
            Language.CPP, Language.C -> code.contains("int main(")
            else -> true
        }
        if (!hasMain && totalLines > 5)
            issues += AiIssue(1, IssueSeverity.WARNING,
                "No main() function found — code may not run",
                "Add a main() function as the program entry point")
    }

    private fun buildOverallFeedback(code: String, issues: List<AiIssue>, language: Language): String {
        val errors = issues.count { it.severity == IssueSeverity.ERROR }
        val warnings = issues.count { it.severity == IssueSeverity.WARNING }
        val lines = code.lines().filter { it.isNotBlank() }.size

        return when {
            errors > 0  -> "❌ Found $errors error(s) that will prevent your code from running. Fix errors first."
            warnings > 0 -> "⚠️ Code looks mostly OK but has $warnings warning(s) to review."
            issues.isEmpty() && lines > 3 -> "✅ Looks good! No issues found in $lines lines of ${language.displayName} code."
            else -> "ℹ️ ${issues.size} suggestion(s) to improve your code."
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 4. CODE EXPLAINER
    // Produces a line-by-line explanation for students
    // ═════════════════════════════════════════════════════════════════════════

    fun explainCode(code: String, language: Language): String {
        val lines = code.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return "Write some code first, then press Explain to understand it."

        val sb = StringBuilder()
        sb.appendLine("📖 Code Explanation (${language.displayName})")
        sb.appendLine("─".repeat(40))
        sb.appendLine()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            val explanation = explainLine(trimmed, language)
            if (explanation != null) {
                sb.appendLine("▸ `${trimmed.take(50)}${if (trimmed.length > 50) "…" else ""}`")
                sb.appendLine("  → $explanation")
                sb.appendLine()
            }
        }

        sb.appendLine("─".repeat(40))
        sb.appendLine("💡 This is a ${language.displayName} program with ${lines.size} lines of code.")
        return sb.toString()
    }

    private fun explainLine(line: String, lang: Language): String? = when {
        // Comments
        line.startsWith("//")  -> "This is a comment — the computer ignores it. It's a note for humans."
        line.startsWith("#") && lang == Language.PYTHON -> "This is a comment — the computer ignores it."
        line.startsWith("/*")  -> "Start of a multi-line comment."

        // Imports
        line.startsWith("import ")    -> "Imports the '${line.removePrefix("import ").trim()}' library so you can use its features."
        line.startsWith("#include")   -> "Includes the '${line.removePrefix("#include").trim()}' header file."
        line.startsWith("using ")     -> "Makes the '${line.removePrefix("using ").trim()}' namespace available."

        // Function/method definitions
        line.startsWith("fun ") && lang == Language.KOTLIN -> {
            val name = Regex("""fun (\w+)""").find(line)?.groupValues?.get(1) ?: "unknown"
            "Defines a function called '$name'. A function is a reusable block of code."
        }
        line.startsWith("def ") && lang == Language.PYTHON -> {
            val name = Regex("""def (\w+)""").find(line)?.groupValues?.get(1) ?: "unknown"
            "Defines a function called '$name'. A function is a reusable block of code."
        }
        line.contains("function ") && lang == Language.JAVASCRIPT -> {
            val name = Regex("""function (\w+)""").find(line)?.groupValues?.get(1) ?: "unknown"
            "Defines a JavaScript function called '$name'."
        }
        line.contains("void ") || line.contains("int ") && line.contains("(") -> {
            val name = Regex("""(?:void|int|String|bool)\s+(\w+)\s*\(""").find(line)?.groupValues?.get(1)
            if (name != null) "Defines a function called '$name'." else null
        }

        // Variable declarations
        line.startsWith("val ") || line.startsWith("var ") -> {
            val name = Regex("""(?:val|var)\s+(\w+)""").find(line)?.groupValues?.get(1)
            val keyword = if (line.startsWith("val")) "val (constant — cannot change)" else "var (variable — can change)"
            if (name != null) "Creates a $keyword called '$name' and stores a value in it." else null
        }
        line.startsWith("const ") || line.startsWith("let ") -> {
            val kind = if (line.startsWith("const")) "constant (cannot change)" else "variable (can change)"
            val name = Regex("""(?:const|let)\s+(\w+)""").find(line)?.groupValues?.get(1)
            if (name != null) "Creates a $kind called '$name'." else null
        }
        Regex("""^(?:int|double|float|String|bool|char|long)\s+\w+\s*=""").containsMatchIn(line) -> {
            val name = Regex("""(?:int|double|float|String|bool)\s+(\w+)""").find(line)?.groupValues?.get(1)
            if (name != null) "Declares a variable called '$name' with a specific type." else null
        }

        // Print statements
        line.contains("println(") || line.contains("print(") || line.startsWith("puts ") ->
            "Prints output to the console so you can see the result."
        line.contains("System.out.print") -> "Prints output to the console (Java style)."
        line.contains("cout ") || line.contains("cout<<") -> "Prints output to the console (C++ style)."
        line.contains("printf(") -> "Prints formatted output to the console."
        line.contains("console.log(") -> "Prints a message to the browser/Node.js console."

        // Control flow
        line.startsWith("if ") || line.startsWith("if(") ->
            "Checks a condition. The code inside only runs if the condition is true."
        line.startsWith("else if") || line.startsWith("elif") ->
            "Checks another condition if the previous `if` was false."
        line.startsWith("else") ->
            "This runs if NONE of the above conditions were true."
        line.startsWith("for ") || line.startsWith("for(") ->
            "A loop that repeats a block of code multiple times."
        line.startsWith("while ") ->
            "A loop that keeps repeating as long as the condition is true."
        line.startsWith("return ") ->
            "Sends a value back from the function and exits it."
        line.startsWith("break") ->
            "Exits the current loop immediately."
        line.startsWith("continue") ->
            "Skips the rest of this loop iteration and goes to the next one."

        // Class
        line.startsWith("class ") ->
            "Defines a class — a blueprint for creating objects."

        // Main function
        line.contains("fun main(") || line.contains("public static void main") || line.contains("int main(") ->
            "This is the main function — the starting point of the program."

        else -> null
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 5. AUTO-FIX
    // ═════════════════════════════════════════════════════════════════════════

    fun tryAutoFix(code: String, errorOutput: String, language: Language): String {
        val error = errorOutput.lowercase()
        // Try each fix strategy in order
        val fixed = when {
            error.contains("expected ';'") || error.contains("missing ';'") ->
                tryFixMissingSemicolon(code, language)
            error.contains("expecting '}'") || error.contains("unexpected end") ->
                tryFixMissingBrace(code)
            error.contains("print ") && language == Language.PYTHON ->
                code.replace(Regex("""^print\s+(.+)$""", RegexOption.MULTILINE), "print($1)")
            else -> ""
        }
        return fixed.ifBlank { code }  // return original if no fix found
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 6. AUTO-FIX HELPERS (private)
    // ═════════════════════════════════════════════════════════════════════════

    private fun tryFixMissingSemicolon(code: String, lang: Language): String {
        if (lang !in listOf(Language.JAVA, Language.C, Language.CPP, Language.CSHARP, Language.JAVASCRIPT)) return ""
        return code.lines().joinToString("\n") { line ->
            val trimmed = line.trimEnd()
            if (trimmed.isNotBlank()
                && !trimmed.endsWith(";") && !trimmed.endsWith("{")
                && !trimmed.endsWith("}") && !trimmed.startsWith("//")
                && !trimmed.startsWith("*") && !trimmed.startsWith("#")) {
                "$trimmed;"
            } else line
        }
    }

    private fun tryFixMissingBrace(code: String): String {
        val opens = code.count { it == '{' }
        val closes = code.count { it == '}' }
        val missing = opens - closes
        return if (missing > 0) code + "\n" + "}".repeat(missing) else ""
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 6. REAL-TIME INLINE ERROR DETECTION
    // Detects errors as the student types (before running)
    // Returns list of line numbers with issues for red underline effect
    // ═════════════════════════════════════════════════════════════════════════

    fun detectInlineErrors(code: String, language: Language): List<InlineError> {
        val errors = mutableListOf<InlineError>()
        val lines = code.lines()

        for ((idx, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("//") || line.startsWith("#")) continue

            when (language) {
                Language.PYTHON -> {
                    // Missing colon after control structures
                    if (Regex("""^(if|elif|else|for|while|def|class|with|try|except|finally)\b.*[^:]$""")
                            .matches(line) && !line.endsWith("\\"))
                        errors += InlineError(idx, "Missing `:` at end of line", InlineErrorType.ERROR)
                    // print without parens
                    if (Regex("""^print\s+[^(]""").matches(line))
                        errors += InlineError(idx, "Use print() with parentheses", InlineErrorType.ERROR)
                }
                Language.KOTLIN -> {
                    // !! force unwrap
                    if (line.contains("!!"))
                        errors += InlineError(idx, "!! can crash if null — consider ?.  or ?:", InlineErrorType.WARNING)
                    // TODO
                    if (line.contains("TODO()"))
                        errors += InlineError(idx, "TODO() will throw an exception at runtime", InlineErrorType.WARNING)
                }
                Language.JAVA -> {
                    // == on string literals
                    if (line.contains("== \"") || line.contains("\" =="))
                        errors += InlineError(idx, "Use .equals() to compare Strings, not ==", InlineErrorType.ERROR)
                }
                Language.JAVASCRIPT -> {
                    // == vs ===
                    if (Regex("""[^=!<>]==[^=]""").containsMatchIn(line))
                        errors += InlineError(idx, "Use === (strict equality) instead of ==", InlineErrorType.WARNING)
                    // var usage
                    if (line.trimStart().startsWith("var "))
                        errors += InlineError(idx, "Prefer const or let instead of var", InlineErrorType.INFO)
                }
                Language.CPP, Language.C -> {
                    // gets() unsafe
                    if (line.contains("gets("))
                        errors += InlineError(idx, "gets() is unsafe — use fgets() instead", InlineErrorType.ERROR)
                }
                else -> {}
            }
        }

        // Brace matching for all C-style languages
        if (language in listOf(Language.KOTLIN, Language.JAVA, Language.JAVASCRIPT, Language.CPP, Language.C, Language.CSHARP)) {
            var depth = 0
            for ((idx, rawLine) in lines.withIndex()) {
                for (ch in rawLine) {
                    if (ch == '{') depth++
                    if (ch == '}') depth--
                    if (depth < 0) {
                        errors += InlineError(idx, "Extra closing `}` — no matching opening `{`", InlineErrorType.ERROR)
                        depth = 0
                    }
                }
            }
            if (depth > 0)
                errors += InlineError(lines.size - 1, "Missing $depth closing `}`", InlineErrorType.ERROR)
        }

        return errors
    }
}

data class InlineError(
    val line: Int,           // 0-based line index
    val message: String,
    val type: InlineErrorType
)

enum class InlineErrorType { ERROR, WARNING, INFO }
