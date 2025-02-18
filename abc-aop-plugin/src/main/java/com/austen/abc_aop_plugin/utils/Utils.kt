package com.austen.abc_aop_plugin.utils

import java.io.File

object Utils {

    fun String.adapterOSPath(): String {
        return replace('/', File.separatorChar)
    }
}