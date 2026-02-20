package com.example.outofroutebuddy.testutils

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

/**
 * Common MockK helpers used across tests.
 */
object MockHelpers {

    /** Create a relaxed mock with optional stubbing block. */
    inline fun <reified T : Any> relaxedMock(noinline block: (T.() -> Unit)? = null): T {
        val m: T = mockk(relaxed = true)
        block?.invoke(m)
        return m
    }

    /** Convenience for stubbing suspend functions returning value. */
    inline fun <reified T> stubSuspend(noinline stub: suspend () -> T, result: T) {
        coEvery { stub() } returns result
    }

    /** Convenience for stubbing functions returning value. */
    inline fun <reified T> stub(noinline stub: () -> T, result: T) {
        every { stub() } returns result
    }
}


