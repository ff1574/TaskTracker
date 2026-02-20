package com.better.spark

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform