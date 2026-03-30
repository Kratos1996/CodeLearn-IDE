package com.codelearn.ide.model

import kotlinx.serialization.Serializable

// ─── Quiz Models ──────────────────────────────────────────────────────────────
@Serializable
data class QuizProblem(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    val category: String,
    val description: String,
    val examples: List<QuizExample>,
    val constraints: List<String>,
    val starterCode: Map<String, String>, // language -> starter code
    val testCases: List<TestCase>,
    val hints: List<String>,
    val solutionExplanation: String,
    val tags: List<String> = emptyList(),
    val timeLimit: Int = 2000, // ms
    val memoryLimit: Int = 256  // MB
)

@Serializable
data class QuizExample(
    val input: String,
    val output: String,
    val explanation: String = ""
)

@Serializable
data class TestCase(
    val input: String,
    val expectedOutput: String,
    val isHidden: Boolean = false
)

@Serializable
enum class Difficulty(val label: String, val color: Long) {
    EASY("Easy", 0xFF00B8A9),
    MEDIUM("Medium", 0xFFF8AC14),
    HARD("Hard", 0xFFEF4444)
}

data class QuizSubmission(
    val problemId: String,
    val language: Language,
    val code: String,
    val result: SubmissionResult,
    val timestamp: Long = System.currentTimeMillis()
)

data class SubmissionResult(
    val status: SubmissionStatus,
    val passedTests: Int,
    val totalTests: Int,
    val runtime: Long = 0,
    val memory: Long = 0,
    val errorMessage: String = ""
)

enum class SubmissionStatus {
    ACCEPTED, WRONG_ANSWER, TIME_LIMIT, RUNTIME_ERROR, COMPILE_ERROR, PENDING
}

// ─── Quiz Repository ──────────────────────────────────────────────────────────
object QuizRepository {
    fun getProblems(): List<QuizProblem> = listOf(
        QuizProblem(
            id = "two-sum",
            title = "Two Sum",
            difficulty = Difficulty.EASY,
            category = "Arrays",
            description = """
Given an array of integers `nums` and an integer `target`, return **indices** of the two numbers such that they add up to `target`.

You may assume that each input would have **exactly one solution**, and you may not use the same element twice.

You can return the answer in any order.
            """.trimIndent(),
            examples = listOf(
                QuizExample("nums = [2,7,11,15], target = 9", "[0,1]", "nums[0] + nums[1] = 2 + 7 = 9"),
                QuizExample("nums = [3,2,4], target = 6", "[1,2]"),
                QuizExample("nums = [3,3], target = 6", "[0,1]")
            ),
            constraints = listOf("2 <= nums.length <= 10⁴", "-10⁹ <= nums[i] <= 10⁹", "Only one valid answer exists"),
            starterCode = mapOf(
                "kt" to "fun twoSum(nums: IntArray, target: Int): IntArray {\n    // Your solution here\n    return intArrayOf()\n}",
                "java" to "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Your solution here\n        return new int[]{};\n    }\n}",
                "py" to "def two_sum(nums, target):\n    # Your solution here\n    pass",
                "js" to "function twoSum(nums, target) {\n    // Your solution here\n}",
                "cs" to "public class Solution {\n    public int[] TwoSum(int[] nums, int target) {\n        // Your solution here\n        return new int[]{};\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    vector<int> twoSum(vector<int>& nums, int target) {\n        // Your solution here\n        return {};\n    }\n};",
                "c" to "int* twoSum(int* nums, int numsSize, int target, int* returnSize) {\n    // Your solution here\n    *returnSize = 2;\n    int* result = (int*)malloc(2 * sizeof(int));\n    return result;\n}",
                "rb" to "def two_sum(nums, target)\n  # Your solution here\nend",
                "dart" to "List<int> twoSum(List<int> nums, int target) {\n  // Your solution here\n  return [];\n}",
                "vb" to "Public Function TwoSum(nums As Integer(), target As Integer) As Integer()\n    ' Your solution here\n    Return New Integer() {}\nEnd Function"
            ),
            testCases = listOf(
                TestCase("[2,7,11,15]\n9", "[0,1]"),
                TestCase("[3,2,4]\n6", "[1,2]"),
                TestCase("[3,3]\n6", "[0,1]"),
                TestCase("[1,2,3,4,5]\n9", "[3,4]", isHidden = true)
            ),
            hints = listOf(
                "Think about using a HashMap to store previously seen numbers",
                "For each number, check if (target - number) exists in your map",
                "Time complexity: O(n), Space complexity: O(n)"
            ),
            solutionExplanation = "Use a hash map to store each element's index. For each element, check if target - element exists in the map.",
            tags = listOf("Array", "Hash Table")
        ),
        QuizProblem(
            id = "palindrome-number",
            title = "Palindrome Number",
            difficulty = Difficulty.EASY,
            category = "Math",
            description = """
Given an integer `x`, return `true` if `x` is a palindrome, and `false` otherwise.

An integer is a **palindrome** when it reads the same forward and backward.
            """.trimIndent(),
            examples = listOf(
                QuizExample("x = 121", "true", "121 reads as 121 from left to right and from right to left."),
                QuizExample("x = -121", "false", "From left to right, it reads -121. From right to left, it reads 121-. Therefore it is not a palindrome."),
                QuizExample("x = 10", "false", "Reads 01 from right to left. Therefore it is not a palindrome.")
            ),
            constraints = listOf("-2³¹ <= x <= 2³¹ - 1"),
            starterCode = mapOf(
                "kt" to "fun isPalindrome(x: Int): Boolean {\n    // Your solution here\n    return false\n}",
                "java" to "class Solution {\n    public boolean isPalindrome(int x) {\n        // Your solution here\n        return false;\n    }\n}",
                "py" to "def is_palindrome(x):\n    # Your solution here\n    pass",
                "js" to "function isPalindrome(x) {\n    // Your solution here\n}",
                "cs" to "public class Solution {\n    public bool IsPalindrome(int x) {\n        // Your solution here\n        return false;\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    bool isPalindrome(int x) {\n        // Your solution here\n        return false;\n    }\n};",
                "c" to "bool isPalindrome(int x) {\n    // Your solution here\n    return false;\n}",
                "rb" to "def is_palindrome(x)\n  # Your solution here\n  false\nend",
                "dart" to "bool isPalindrome(int x) {\n  // Your solution here\n  return false;\n}",
                "vb" to "Public Function IsPalindrome(x As Integer) As Boolean\n    ' Your solution here\n    Return False\nEnd Function"
            ),
            testCases = listOf(
                TestCase("121", "true"),
                TestCase("-121", "false"),
                TestCase("10", "false"),
                TestCase("0", "true", isHidden = true),
                TestCase("1221", "true", isHidden = true)
            ),
            hints = listOf(
                "Negative numbers are never palindromes",
                "Convert the number to string and check if it equals its reverse",
                "Or reverse only half the number mathematically"
            ),
            solutionExplanation = "Convert to string, then compare with its reverse. Handle negative numbers and trailing zeros edge cases.",
            tags = listOf("Math", "Two Pointers")
        ),
        QuizProblem(
            id = "reverse-linked-list",
            title = "Reverse Linked List",
            difficulty = Difficulty.EASY,
            category = "Linked List",
            description = """
Given the `head` of a singly linked list, reverse the list, and return the reversed list.
            """.trimIndent(),
            examples = listOf(
                QuizExample("head = [1,2,3,4,5]", "[5,4,3,2,1]"),
                QuizExample("head = [1,2]", "[2,1]"),
                QuizExample("head = []", "[]")
            ),
            constraints = listOf("The number of nodes in the list is in [0, 5000]", "-5000 <= Node.val <= 5000"),
            starterCode = mapOf(
                "kt" to "class ListNode(var `val`: Int) {\n    var next: ListNode? = null\n}\n\nfun reverseList(head: ListNode?): ListNode? {\n    // Your solution here\n    return null\n}",
                "java" to "class Solution {\n    public ListNode reverseList(ListNode head) {\n        // Your solution here\n        return null;\n    }\n}",
                "cs" to "public class Solution {\n    public ListNode ReverseList(ListNode head) {\n        // Your solution here\n        return null;\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    ListNode* reverseList(ListNode* head) {\n        // Your solution here\n        return nullptr;\n    }\n};",
                "c" to "struct ListNode* reverseList(struct ListNode* head) {\n    // Your solution here\n    return NULL;\n}",
                "rb" to "def reverse_list(head)\n  # Your solution here\nend",
                "dart" to "ListNode? reverseList(ListNode? head) {\n  // Your solution here\n  return null;\n}",
                "vb" to "Public Function ReverseList(head As ListNode) As ListNode\n    ' Your solution here\n    Return Nothing\nEnd Function"
            ),
            testCases = listOf(
                TestCase("[1,2,3,4,5]", "[5,4,3,2,1]"),
                TestCase("[1,2]", "[2,1]"),
                TestCase("[]", "[]")
            ),
            hints = listOf(
                "Use three pointers: prev, curr, and next",
                "Iteratively reverse each pointer",
                "Recursive solution: reverse the rest first, then fix the head"
            ),
            solutionExplanation = "Use iterative approach with prev=null, curr=head. For each node, save next, point curr.next to prev, advance both pointers.",
            tags = listOf("Linked List", "Recursion")
        ),
        QuizProblem(
            id = "valid-parentheses",
            title = "Valid Parentheses",
            difficulty = Difficulty.EASY,
            category = "Stack",
            description = """
Given a string `s` containing just the characters `'('`, `')'`, `'{'`, `'}'`, `'['` and `']'`, determine if the input string is valid.

An input string is valid if:
1. Open brackets must be closed by the same type of brackets.
2. Open brackets must be closed in the correct order.
3. Every close bracket has a corresponding open bracket of the same type.
            """.trimIndent(),
            examples = listOf(
                QuizExample("s = \"()\"", "true"),
                QuizExample("s = \"()[]{}\"", "true"),
                QuizExample("s = \"(]\"", "false")
            ),
            constraints = listOf("1 <= s.length <= 10⁴", "s consists of parentheses only '()[]{}'"),
            starterCode = mapOf(
                "kt" to "fun isValid(s: String): Boolean {\n    // Your solution here\n    return false\n}",
                "java" to "class Solution {\n    public boolean isValid(String s) {\n        // Your solution here\n        return false;\n    }\n}",
                "py" to "def is_valid(s):\n    # Your solution here\n    pass",
                "js" to "function isValid(s) {\n    // Your solution here\n}",
                "cs" to "public class Solution {\n    public bool IsValid(string s) {\n        // Your solution here\n        return false;\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    bool isValid(string s) {\n        // Your solution here\n        return false;\n    }\n};",
                "c" to "bool isValid(char* s) {\n    // Your solution here\n    return false;\n}",
                "rb" to "def is_valid(s)\n  # Your solution here\n  false\nend",
                "dart" to "bool isValid(String s) {\n  // Your solution here\n  return false;\n}",
                "vb" to "Public Function IsValid(s As String) As Boolean\n    ' Your solution here\n    Return False\nEnd Function"
            ),
            testCases = listOf(
                TestCase("\"()\"", "true"),
                TestCase("\"()[]{}\"", "true"),
                TestCase("\"(]\"", "false"),
                TestCase("\"{[]}\"", "true", isHidden = true)
            ),
            hints = listOf(
                "Use a Stack data structure",
                "Push opening brackets onto the stack",
                "For closing brackets, check if the top of stack matches"
            ),
            solutionExplanation = "Use a stack. Push opening brackets. For closing brackets, check if stack top is the matching opener. Stack should be empty at end.",
            tags = listOf("String", "Stack")
        ),
        QuizProblem(
            id = "binary-search",
            title = "Binary Search",
            difficulty = Difficulty.EASY,
            category = "Binary Search",
            description = """
Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then return its index. Otherwise, return `-1`.

You must write an algorithm with `O(log n)` runtime complexity.
            """.trimIndent(),
            examples = listOf(
                QuizExample("nums = [-1,0,3,5,9,12], target = 9", "4", "9 exists in nums and its index is 4"),
                QuizExample("nums = [-1,0,3,5,9,12], target = 2", "-1", "2 does not exist in nums so return -1")
            ),
            constraints = listOf("1 <= nums.length <= 10⁴", "-10⁴ < nums[i], target < 10⁴", "All integers in nums are unique"),
            starterCode = mapOf(
                "kt" to "fun search(nums: IntArray, target: Int): Int {\n    // Your solution here\n    return -1\n}",
                "java" to "class Solution {\n    public int search(int[] nums, int target) {\n        // Your solution here\n        return -1;\n    }\n}",
                "py" to "def search(nums, target):\n    # Your solution here\n    return -1",
                "js" to "function search(nums, target) {\n    // Your solution here\n    return -1;\n}",
                "cs" to "public class Solution {\n    public int Search(int[] nums, int target) {\n        // Your solution here\n        return -1;\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    int search(vector<int>& nums, int target) {\n        // Your solution here\n        return -1;\n    }\n};",
                "c" to "int search(int* nums, int numsSize, int target) {\n    // Your solution here\n    return -1;\n}",
                "rb" to "def search(nums, target)\n  # Your solution here\n  -1\nend",
                "dart" to "int search(List<int> nums, int target) {\n  // Your solution here\n  return -1;\n}",
                "vb" to "Public Function Search(nums As Integer(), target As Integer) As Integer\n    ' Your solution here\n    Return -1\nEnd Function"
            ),
            testCases = listOf(
                TestCase("[-1,0,3,5,9,12]\n9", "4"),
                TestCase("[-1,0,3,5,9,12]\n2", "-1"),
                TestCase("[5]\n5", "0", isHidden = true)
            ),
            hints = listOf(
                "Use left and right pointers",
                "Calculate mid = (left + right) / 2",
                "Adjust left or right based on comparison with target"
            ),
            solutionExplanation = "Classic binary search: left=0, right=n-1. While left<=right, compute mid. If nums[mid]==target return mid, else adjust bounds.",
            tags = listOf("Array", "Binary Search")
        ),
        QuizProblem(
            id = "maximum-subarray",
            title = "Maximum Subarray",
            difficulty = Difficulty.MEDIUM,
            category = "Dynamic Programming",
            description = """
Given an integer array `nums`, find the **subarray** with the largest sum, and return its sum.
            """.trimIndent(),
            examples = listOf(
                QuizExample("nums = [-2,1,-3,4,-1,2,1,-5,4]", "6", "The subarray [4,-1,2,1] has the largest sum 6."),
                QuizExample("nums = [1]", "1"),
                QuizExample("nums = [5,4,-1,7,8]", "23")
            ),
            constraints = listOf("1 <= nums.length <= 10⁵", "-10⁴ <= nums[i] <= 10⁴"),
            starterCode = mapOf(
                "kt" to "fun maxSubArray(nums: IntArray): Int {\n    // Your solution here\n    return 0\n}",
                "java" to "class Solution {\n    public int maxSubArray(int[] nums) {\n        // Your solution here\n        return 0;\n    }\n}",
                "py" to "def max_sub_array(nums):\n    # Your solution here\n    return 0",
                "js" to "function maxSubArray(nums) {\n    // Your solution here\n    return 0;\n}",
                "cs" to "public class Solution {\n    public int MaxSubArray(int[] nums) {\n        // Your solution here\n        return 0;\n    }\n}",
                "cpp" to "class Solution {\npublic:\n    int maxSubArray(vector<int>& nums) {\n        // Your solution here\n        return 0;\n    }\n};",
                "c" to "int maxSubArray(int* nums, int numsSize) {\n    // Your solution here\n    return 0;\n}",
                "rb" to "def max_sub_array(nums)\n  # Your solution here\n  0\nend",
                "dart" to "int maxSubArray(List<int> nums) {\n  // Your solution here\n  return 0;\n}",
                "vb" to "Public Function MaxSubArray(nums As Integer()) As Integer\n    ' Your solution here\n    Return 0\nEnd Function"
            ),
            testCases = listOf(
                TestCase("[-2,1,-3,4,-1,2,1,-5,4]", "6"),
                TestCase("[1]", "1"),
                TestCase("[5,4,-1,7,8]", "23")
            ),
            hints = listOf(
                "Think about Kadane's Algorithm",
                "At each position, decide: extend current subarray OR start new one",
                "currentMax = max(nums[i], currentMax + nums[i])"
            ),
            solutionExplanation = "Kadane's Algorithm: Track current sum and max sum. For each element, current = max(element, current+element). Update max if current > max.",
            tags = listOf("Array", "DP", "Divide and Conquer")
        ),
        QuizProblem(
            id = "lru-cache",
            title = "LRU Cache",
            difficulty = Difficulty.HARD,
            category = "Design",
            description = """
Design a data structure that follows the constraints of a **Least Recently Used (LRU) cache**.

Implement the `LRUCache` class:
- `LRUCache(int capacity)` Initialize the LRU cache with positive size `capacity`.
- `int get(int key)` Return the value of the `key` if the key exists, otherwise return `-1`.
- `void put(int key, int value)` Update the value of the `key` if the key exists. Otherwise, add the key-value pair to the cache. If the number of keys exceeds the `capacity` from this operation, evict the least recently used key.

The functions `get` and `put` must each run in `O(1)` average time complexity.
            """.trimIndent(),
            examples = listOf(
                QuizExample(
                    "LRUCache lRUCache = new LRUCache(2);\nlRUCache.put(1, 1);\nlRUCache.put(2, 2);\nlRUCache.get(1);\nlRUCache.put(3, 3);\nlRUCache.get(2);\nlRUCache.put(4, 4);\nlRUCache.get(1);\nlRUCache.get(3);\nlRUCache.get(4);",
                    "[null,null,null,1,null,-1,null,-1,3,4]"
                )
            ),
            constraints = listOf("1 <= capacity <= 3000", "0 <= key <= 10⁴", "0 <= value <= 10⁵"),
            starterCode = mapOf(
                "kt" to "class LRUCache(capacity: Int) {\n    fun get(key: Int): Int {\n        // Your solution\n        return -1\n    }\n    fun put(key: Int, value: Int) {\n        // Your solution\n    }\n}",
                "java" to "class LRUCache {\n    public LRUCache(int capacity) {}\n    public int get(int key) { return -1; }\n    public void put(int key, int value) {}\n}",
                "cs" to "public class LRUCache {\n    public LRUCache(int capacity) {}\n    public int Get(int key) { return -1; }\n    public void Put(int key, int value) {}\n}",
                "cpp" to "class LRUCache {\npublic:\n    LRUCache(int capacity) {}\n    int get(int key) { return -1; }\n    void put(int key, int value) {}\n};",
                "c" to "// Implement LRU Cache in C\nstruct LRUCache* lRUCacheCreate(int capacity) { return NULL; }\nint lRUCacheGet(struct LRUCache* obj, int key) { return -1; }\nvoid lRUCachePut(struct LRUCache* obj, int key, int value) {}",
                "rb" to "class LRUCache\n  def initialize(capacity)\n  end\n  def get(key)\n    -1\n  end\n  def put(key, value)\n  end\nend",
                "dart" to "class LRUCache {\n  LRUCache(int capacity) {}\n  int get(int key) => -1;\n  void put(int key, int value) {}\n}",
                "vb" to "Public Class LRUCache\n    Public Sub New(capacity As Integer)\n    End Sub\n    Public Function [Get](key As Integer) As Integer\n        Return -1\n    End Function\n    Public Sub Put(key As Integer, value As Integer)\n    End Sub\nEnd Class"
            ),
            testCases = listOf(
                TestCase("capacity=2\nops=[[put,1,1],[put,2,2],[get,1],[put,3,3],[get,2],[put,4,4],[get,1],[get,3],[get,4]]", "[null,null,1,null,-1,null,-1,3,4]")
            ),
            hints = listOf(
                "Use a combination of HashMap and Doubly Linked List",
                "HashMap gives O(1) lookup",
                "Doubly Linked List maintains order for O(1) insertion/deletion"
            ),
            solutionExplanation = "Combine HashMap (key→node) with doubly linked list (order). On get: move to head. On put: add to head, if over capacity remove from tail.",
            tags = listOf("Hash Table", "Linked List", "Design", "Doubly-Linked List")
        )
    )
}
