package com.example.tierapp.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class LifecycleProperty<T> private constructor(
    private val onDestroy: T.() -> Unit
) : ReadWriteProperty<LifecycleOwner, T>,
    DefaultLifecycleObserver {

    companion object {
        @Suppress("unused")
        fun <T> LifecycleOwner.lifecycleProperty(
            onDestroy: T.() -> Unit = {}
        ): LifecycleProperty<T> = LifecycleProperty(
            onDestroy
        )
    }

    private var _value: T? = null

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): T {
        if (Lifecycle.Event.upFrom(thisRef.lifecycle.currentState) == Lifecycle.Event.ON_STOP)
            throw IllegalStateException("Can not access to ${property.name} after ON_STOP event")
        return _value ?: throw IllegalStateException("${property.name} is null")
    }

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: T) {
        thisRef.lifecycle.addObserver(this)
        this._value = value
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        _value?.onDestroy()
        _value = null
        owner.lifecycle.removeObserver(this)
    }
}