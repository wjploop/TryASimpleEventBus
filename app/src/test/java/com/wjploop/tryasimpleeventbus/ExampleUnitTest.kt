package com.wjploop.tryasimpleeventbus

import android.view.inputmethod.ExtractedText
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMyEventBus() {
        val eventBus = EventBus.getDefault()
//
        eventBus.register(this)
        eventBus.register(StringSubscriber())
        eventBus.post("hello world")
        eventBus.post(ExampleUnitTest())
    }

    fun onEvent(msg: ExampleUnitTest) {
        println("that is ok")
    }

    class StringSubscriber {
        fun onEvent(strEvent: String) {
            println("oh, I received the string message:$strEvent")
        }

    }

    class StringSubscriber2 {
        fun onEvent(strEvent: String) {
            println("oh, I received the string message2:$strEvent")
        }

    }

}
