package com.wjploop.tryasimpleeventbus

import android.util.Log
import java.lang.IllegalStateException
import java.lang.reflect.Method

class EventBus {

    init {
        subscriptionsByEventType = HashMap()
        typesBySubscriber = HashMap()
    }

    fun register(subscriber: Any, methodName: String = "onEvent") {
        val subscriberMethods = findSubscriberMethods(subscriber.javaClass, methodName)
        subscriberMethods.forEach { method ->
            val eventType = method.parameterTypes[0]!!  //取第一参数作为一个事件
            subscribe(subscriber, method, eventType)
        }
    }

    fun post(event: Any) {
        val clazz = event.javaClass
        val subscriptions = subscriptionsByEventType[clazz]
        if (subscriptions.isNullOrEmpty()) {
            println("No subscriptions registered for the event:${event.javaClass}")
        } else {
            subscriptions.forEach {
                postToSubscription(it, event)
            }
        }
    }

    private fun postToSubscription(subscription: Subscription, event: Any) {
        subscription.method.invoke(subscription.subscriber, event)
    }


    private fun subscribe(subscriber: Any, subscribeMethod: Method, eventType: Class<*>) {
        //判断该事件是否已经有注册有
        //一个事件，对应着一堆观察者列表
        var subscriptions = subscriptionsByEventType[eventType]
        if (subscriptions == null) {
            subscriptions = ArrayList()
            subscriptionsByEventType[eventType] = subscriptions
        } else {
            //有可能已经
            //检查该观察是否已经注册到检查该事件的列表中
            if (subscriptions.map { it.subscriber }.contains(subscriber)) {
                throw IllegalStateException("Subscriber ${subscriber.javaClass} already registered to event $eventType")
            }
        }
        subscribeMethod.isAccessible = true
        subscriptions.add(Subscription(subscriber, subscribeMethod))


        //更新 typesBySubscriber
        var subscribeEvents = typesBySubscriber[subscriber]
        if (subscribeEvents == null) {
            subscribeEvents = ArrayList()
            typesBySubscriber[subscriber] = subscribeEvents
        }
        subscribeEvents.add(eventType)

    }

    //获取目标类中的的onEvent（Event），根据Event来确定不同的事件
    private fun findSubscriberMethods(subscriberClass: Class<Any>, methodName: String): List<Method> {
        return ArrayList<Method>().apply {
            val key = subscriberClass.name + "." + methodName
            var clazz: Class<Any>? = subscriberClass
            while (clazz != null) {
                clazz.declaredMethods.filter {
                    it.name == methodName && it.parameterTypes.size == 1
                }.let {
                    this.addAll(it)
                }
                clazz = clazz.superclass as Class<Any>?
            }
        }.also {
            if (it.isEmpty()) {
                //订阅者没有onEvent方法，不合理
                throw RuntimeException("Subscriber $subscriberClass has no methods called $methodName")
            }
        }
    }


    companion object {
        var mInstance: EventBus? = null

        val methodCache = HashMap<String, List<Method>>()
        val postQueuePool = ArrayList<ArrayList<Subscription>>()

        //一种事件所涵盖的订阅关系
        lateinit var subscriptionsByEventType: HashMap<Class<*>, ArrayList<Subscription>>

        //一个订阅者所关注的事件列表
        lateinit var typesBySubscriber: HashMap<Any, ArrayList<Class<*>>>

        fun getDefault(): EventBus {
            synchronized(this) {
                return mInstance ?: EventBus().also {
                    mInstance = it
                }
            }
        }
    }

    data class Subscription(val subscriber: Any, val method: Method) {
        override fun equals(other: Any?): Boolean {
            return if (other is Subscription) {
                subscriber == other.subscriber && method == other.method
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return subscriber.hashCode() + method.hashCode()

        }
    }


}