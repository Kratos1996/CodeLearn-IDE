package com.codelearn.ide

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform