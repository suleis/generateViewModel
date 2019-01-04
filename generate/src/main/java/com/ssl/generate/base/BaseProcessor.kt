package com.ssl.generate.base

import com.ssl.generate.entity.EntityHandler
import java.util.LinkedHashSet
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

abstract class BaseProcessor : AbstractProcessor() {
    protected lateinit var entityHandler: EntityHandler
    protected var isAndroidX = false
    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean = false
    override fun init(env: ProcessingEnvironment?) {
        super.init(env)
        entityHandler = EntityHandler(env!!)
        isAndroidX = hasAndroidX(env.elementUtils)

    }

    abstract fun getSupportedAnnotations(): Array<Class<out Annotation>>
    private fun hasAndroidX(elementUtils: Elements): Boolean {
        val annotationsPresent = elementUtils.getTypeElement("androidx.annotation.NonNull") != null
        val corePresent = elementUtils.getTypeElement("androidx.core.content.ContextCompat") != null
        return annotationsPresent && corePresent
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types = LinkedHashSet<String>()
        val typeStrings = getSupportedAnnotations()
        for (type in typeStrings) {
            types.add(type.canonicalName)
        }
        return types
    }
}