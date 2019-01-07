package com.ssl.annotation

import com.ssl.annotation.entity.Null
import kotlin.reflect.KClass

annotation class BaseViewModel(val bvmTypeClass: KClass<*> = Null::class)
