package com.ssl.annotation

import com.ssl.annotation.utils.LiveDataAttributeType
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS )
annotation class LiveData(val attribute: LiveDataAttributeType = LiveDataAttributeType.DEFAULT)
