package com.benyaamin.rakhsh.util

import android.util.Log

class Logger(private val debug: Boolean, private val tag: String) {

    fun info(msg: String, throwable: Throwable? = null) {
        Log.i(tag, msg, throwable)
    }

    fun error(msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
    }

    fun debug(block: () -> String) {
        if (debug) {
            Log.d(tag, block.invoke())
        }
    }

}