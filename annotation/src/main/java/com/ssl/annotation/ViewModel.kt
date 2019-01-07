package com.ssl.annotation

import com.ssl.annotation.entity.Null
import kotlin.reflect.KClass
annotation class ViewModel(val superClass: KClass<*> = Null::class)
