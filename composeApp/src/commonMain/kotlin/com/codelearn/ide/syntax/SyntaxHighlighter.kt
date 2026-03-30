package com.codelearn.ide.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.codelearn.ide.model.Language

// ─── Token Types ──────────────────────────────────────────────────────────────
enum class TokenType {
    KEYWORD, STRING, NUMBER, COMMENT, OPERATOR,
    FUNCTION, TYPE, ANNOTATION, NORMAL, BRACKET
}

data class SyntaxToken(
    val start: Int,
    val end: Int,
    val type: TokenType
)

// ─── Theme Colors (Dracula-inspired dark theme) ───────────────────────────────
object SyntaxColors {
    val keyword    = Color(0xFFFF79C6)  // pink
    val string     = Color(0xFFF1FA8C)  // yellow
    val number     = Color(0xFFBD93F9)  // purple
    val comment    = Color(0xFF6272A4)  // blue-gray
    val operator   = Color(0xFFFF79C6)  // pink
    val function   = Color(0xFF50FA7B)  // green
    val type       = Color(0xFF8BE9FD)  // cyan
    val annotation = Color(0xFFFFB86C)  // orange
    val normal     = Color(0xFFF8F8F2)  // white
    val bracket    = Color(0xFFFFB86C)  // orange
}

// ─── Language Keywords ────────────────────────────────────────────────────────
private val KOTLIN_KEYWORDS = setOf(
    "fun", "val", "var", "class", "object", "interface", "enum", "sealed",
    "data", "abstract", "open", "override", "private", "protected", "public",
    "internal", "companion", "return", "if", "else", "when", "for", "while",
    "do", "try", "catch", "finally", "throw", "in", "is", "as", "by", "null",
    "true", "false", "this", "super", "import", "package", "typealias",
    "suspend", "coroutine", "inline", "reified", "crossinline", "noinline",
    "lateinit", "lazy", "by", "init", "constructor", "it", "field", "get", "set"
)

private val JAVA_KEYWORDS = setOf(
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
    "class", "const", "continue", "default", "do", "double", "else", "enum",
    "extends", "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native", "new",
    "package", "private", "protected", "public", "return", "short", "static",
    "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
    "transient", "try", "void", "volatile", "while", "true", "false", "null"
)

private val CSHARP_KEYWORDS = setOf(
    "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char",
    "checked", "class", "const", "continue", "decimal", "default", "delegate",
    "do", "double", "else", "enum", "event", "explicit", "extern", "false",
    "finally", "fixed", "float", "for", "foreach", "goto", "if", "implicit",
    "in", "int", "interface", "internal", "is", "lock", "long", "namespace",
    "new", "null", "object", "operator", "out", "override", "params", "private",
    "protected", "public", "readonly", "ref", "return", "sbyte", "sealed",
    "short", "sizeof", "stackalloc", "static", "string", "struct", "switch",
    "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked",
    "unsafe", "ushort", "using", "virtual", "void", "volatile", "while", "var",
    "async", "await", "dynamic", "nameof", "when", "yield"
)

private val CPP_KEYWORDS = setOf(
    "auto", "break", "case", "char", "const", "continue", "default", "do",
    "double", "else", "enum", "extern", "float", "for", "goto", "if", "inline",
    "int", "long", "register", "return", "short", "signed", "sizeof", "static",
    "struct", "switch", "typedef", "union", "unsigned", "void", "volatile",
    "while", "class", "namespace", "template", "typename", "virtual", "override",
    "nullptr", "true", "false", "new", "delete", "public", "private", "protected",
    "this", "throw", "try", "catch", "using", "include", "define", "ifdef",
    "endif", "pragma", "vector", "string", "cout", "cin", "endl"
)

private val C_KEYWORDS = setOf(
    "auto", "break", "case", "char", "const", "continue", "default", "do",
    "double", "else", "enum", "extern", "float", "for", "goto", "if", "inline",
    "int", "long", "register", "return", "short", "signed", "sizeof", "static",
    "struct", "switch", "typedef", "union", "unsigned", "void", "volatile",
    "while", "NULL", "include", "define", "ifdef", "endif", "printf", "scanf"
)

private val RUBY_KEYWORDS = setOf(
    "alias", "and", "begin", "break", "case", "class", "def", "defined?",
    "do", "else", "elsif", "end", "ensure", "false", "for", "if", "in",
    "module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return",
    "self", "super", "then", "true", "undef", "unless", "until", "when", "while",
    "yield", "puts", "print", "require", "attr_accessor", "attr_reader", "attr_writer"
)

private val DART_KEYWORDS = setOf(
    "abstract", "as", "assert", "async", "await", "break", "case", "catch",
    "class", "const", "continue", "covariant", "default", "deferred", "do",
    "dynamic", "else", "enum", "export", "extends", "extension", "external",
    "factory", "false", "final", "finally", "for", "Function", "get", "hide",
    "if", "implements", "import", "in", "interface", "is", "late", "library",
    "mixin", "new", "null", "on", "operator", "part", "required", "rethrow",
    "return", "set", "show", "static", "super", "switch", "sync", "this",
    "throw", "true", "try", "typedef", "var", "void", "while", "with", "yield",
    "print", "List", "Map", "Set", "String", "int", "double", "bool"
)

private val VB_KEYWORDS = setOf(
    "AddHandler", "AddressOf", "Alias", "And", "AndAlso", "As", "Boolean",
    "ByRef", "Byte", "ByVal", "Call", "Case", "Catch", "CBool", "CByte",
    "CChar", "CDate", "CDec", "CDbl", "Char", "CInt", "Class", "CLng",
    "CObj", "Const", "Continue", "CSByte", "CShort", "CSng", "CStr", "CType",
    "CUInt", "CULng", "CUShort", "Date", "Decimal", "Declare", "Default",
    "Delegate", "Dim", "DirectCast", "Do", "Double", "Each", "Else", "ElseIf",
    "End", "EndIf", "Enum", "Erase", "Error", "Event", "Exit", "False",
    "Finally", "For", "Friend", "Function", "Get", "GetType", "GoTo", "Handles",
    "If", "Implements", "Imports", "In", "Inherits", "Integer", "Interface",
    "Is", "IsNot", "Let", "Lib", "Like", "Long", "Loop", "Me", "Mod",
    "Module", "MustInherit", "MustOverride", "MyBase", "MyClass", "Namespace",
    "Narrowing", "New", "Next", "Not", "Nothing", "NotInheritable",
    "NotOverridable", "Object", "Of", "On", "Operator", "Option", "Optional",
    "Or", "OrElse", "Out", "Overloads", "Overridable", "Overrides", "ParamArray",
    "Partial", "Private", "Property", "Protected", "Public", "RaiseEvent",
    "ReadOnly", "ReDim", "RemoveHandler", "Resume", "Return", "SByte", "Select",
    "Set", "Shadows", "Shared", "Short", "Single", "Static", "Step", "Stop",
    "String", "Structure", "Sub", "SyncLock", "Then", "Throw", "To", "True",
    "Try", "TryCast", "TypeOf", "UInteger", "ULong", "UShort", "Using",
    "Variant", "Wend", "When", "While", "Widening", "With", "WithEvents",
    "WriteOnly", "Xor"
)

    val PYTHON_KEYWORDS = setOf(
        "False", "None", "True", "and", "as", "assert", "async", "await",
        "break", "class", "continue", "def", "del", "elif", "else", "except",
        "finally", "for", "from", "global", "if", "import", "in", "is",
        "lambda", "nonlocal", "not", "or", "pass", "raise", "return",
        "try", "while", "with", "yield",
        "int", "str", "float", "bool", "list", "dict", "set", "tuple",
        "len", "range", "print", "input", "type", "isinstance", "enumerate",
        "zip", "map", "filter", "sorted", "reversed", "sum", "min", "max",
        "open", "super", "self", "__init__", "__str__", "__repr__"
    )

    val JAVASCRIPT_KEYWORDS = setOf(
        "var", "let", "const", "function", "return", "if", "else", "for",
        "while", "do", "switch", "case", "break", "continue", "new", "delete",
        "typeof", "instanceof", "in", "of", "this", "class", "extends",
        "import", "export", "default", "from", "async", "await", "try",
        "catch", "finally", "throw", "true", "false", "null", "undefined",
        "console", "log", "error", "warn", "Math", "Array", "Object",
        "String", "Number", "Boolean", "Promise", "JSON", "parseInt",
        "parseFloat", "isNaN", "setTimeout", "setInterval", "fetch",
        "map", "filter", "reduce", "forEach", "find", "some", "every",
        "push", "pop", "shift", "unshift", "slice", "splice", "join",
        "split", "trim", "includes", "indexOf", "toString", "valueOf"
    )

fun getKeywordsForLanguage(language: Language): Set<String> = when (language) {
    Language.KOTLIN     -> KOTLIN_KEYWORDS
    Language.JAVA       -> JAVA_KEYWORDS
    Language.PYTHON     -> PYTHON_KEYWORDS
    Language.JAVASCRIPT -> JAVASCRIPT_KEYWORDS
    Language.CSHARP     -> CSHARP_KEYWORDS
    Language.CPP        -> CPP_KEYWORDS
    Language.C          -> C_KEYWORDS
    Language.RUBY       -> RUBY_KEYWORDS
    Language.DART       -> DART_KEYWORDS
    Language.VB         -> VB_KEYWORDS
}

// ─── Syntax Highlighter ───────────────────────────────────────────────────────
object SyntaxHighlighter {

    fun highlight(code: String, language: Language): AnnotatedString {
        val tokens = tokenize(code, language)
        return buildAnnotatedString {
            append(code)
            for (token in tokens) {
                val color = when (token.type) {
                    TokenType.KEYWORD    -> SyntaxColors.keyword
                    TokenType.STRING     -> SyntaxColors.string
                    TokenType.NUMBER     -> SyntaxColors.number
                    TokenType.COMMENT    -> SyntaxColors.comment
                    TokenType.OPERATOR   -> SyntaxColors.operator
                    TokenType.FUNCTION   -> SyntaxColors.function
                    TokenType.TYPE       -> SyntaxColors.type
                    TokenType.ANNOTATION -> SyntaxColors.annotation
                    TokenType.BRACKET    -> SyntaxColors.bracket
                    TokenType.NORMAL     -> SyntaxColors.normal
                }
                val weight = if (token.type == TokenType.KEYWORD) FontWeight.Bold else FontWeight.Normal
                val style = if (token.type == TokenType.COMMENT) FontStyle.Italic else FontStyle.Normal
                addStyle(
                    SpanStyle(color = color, fontWeight = weight, fontStyle = style),
                    token.start, token.end
                )
            }
        }
    }

    private fun tokenize(code: String, language: Language): List<SyntaxToken> {
        val tokens = mutableListOf<SyntaxToken>()
        val keywords = getKeywordsForLanguage(language)
        var i = 0

        while (i < code.length) {
            when {
                // Single-line comment
                code.startsWith("//", i) -> {
                    val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                    tokens.add(SyntaxToken(i, end, TokenType.COMMENT))
                    i = end
                }
                // Hash comment (Ruby)
                code[i] == '#' && (language == Language.RUBY || language == Language.PYTHON) -> {
                    val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                    tokens.add(SyntaxToken(i, end, TokenType.COMMENT))
                    i = end
                }
                // Multi-line comment /* */
                code.startsWith("/*", i) -> {
                    val end = (code.indexOf("*/", i + 2) + 2).let { if (it == 1) code.length else it }
                    tokens.add(SyntaxToken(i, end, TokenType.COMMENT))
                    i = end
                }
                // Single quoted string
                code[i] == '\'' -> {
                    var j = i + 1
                    while (j < code.length && code[j] != '\'' && code[j] != '\n') {
                        if (code[j] == '\\') j++
                        j++
                    }
                    tokens.add(SyntaxToken(i, minOf(j + 1, code.length), TokenType.STRING))
                    i = minOf(j + 1, code.length)
                }
                // Double quoted string
                code[i] == '"' -> {
                    var j = i + 1
                    while (j < code.length && code[j] != '"' && code[j] != '\n') {
                        if (code[j] == '\\') j++
                        j++
                    }
                    tokens.add(SyntaxToken(i, minOf(j + 1, code.length), TokenType.STRING))
                    i = minOf(j + 1, code.length)
                }
                // Triple quoted string (Kotlin)
                code.startsWith("\"\"\"", i) -> {
                    val end = code.indexOf("\"\"\"", i + 3).let {
                        if (it == -1) code.length else it + 3
                    }
                    tokens.add(SyntaxToken(i, end, TokenType.STRING))
                    i = end
                }
                // Annotation
                code[i] == '@' && (language == Language.KOTLIN || language == Language.JAVA || language == Language.CSHARP) -> {
                    var j = i + 1
                    while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                    tokens.add(SyntaxToken(i, j, TokenType.ANNOTATION))
                    i = j
                }
                // Number
                code[i].isDigit() || (code[i] == '-' && i + 1 < code.length && code[i + 1].isDigit() && (i == 0 || !code[i-1].isLetterOrDigit())) -> {
                    var j = i + 1
                    while (j < code.length && (code[j].isDigit() || code[j] == '.' || code[j] == 'L' || code[j] == 'f' || code[j] == 'x')) j++
                    if (j > i + 1 || code[i].isDigit()) {
                        tokens.add(SyntaxToken(i, j, TokenType.NUMBER))
                        i = j
                    } else i++
                }
                // Identifier or keyword
                code[i].isLetter() || code[i] == '_' -> {
                    var j = i + 1
                    while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                    val word = code.substring(i, j)
                    val type = when {
                        keywords.contains(word) -> TokenType.KEYWORD
                        word[0].isUpperCase() -> TokenType.TYPE
                        j < code.length && code[j] == '(' -> TokenType.FUNCTION
                        else -> TokenType.NORMAL
                    }
                    if (type != TokenType.NORMAL) {
                        tokens.add(SyntaxToken(i, j, type))
                    }
                    i = j
                }
                // Brackets
                code[i] in "()[]{}".toSet() -> {
                    tokens.add(SyntaxToken(i, i + 1, TokenType.BRACKET))
                    i++
                }
                // Operators
                code[i] in "+-*/%=<>!&|^~".toSet() -> {
                    tokens.add(SyntaxToken(i, i + 1, TokenType.OPERATOR))
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }
}
