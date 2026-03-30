package com.codelearn.ide

class AndroidPlatform : Platform {
    override val name: String = "Desktop ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = AndroidPlatform()