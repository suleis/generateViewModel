package com.ssl.annotation

import com.ssl.annotation.entity.Null
import kotlin.reflect.KClass
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class ViewModel(val superClass: KClass<*> = Null::class)
