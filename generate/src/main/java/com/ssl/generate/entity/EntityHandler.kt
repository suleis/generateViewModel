package com.ssl.generate.entity

import com.squareup.javapoet.JavaFile

import com.ssl.generate.base.BaseProcessor
import java.util.*
import javax.annotation.processing.Filer

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class EntityHandler(env: ProcessingEnvironment) : GenerateBaseEntity() {
    internal val elements: Elements

    internal val filer: Filer
    private var classEntityMap: MutableMap<String, ClassEntity> = HashMap()

    internal var env: RoundEnvironment? = null

    init {
        this.elements = env.elementUtils
        types = env.typeUtils
        this.filer = env.filer
    }

    fun handlerElement(env: RoundEnvironment, processor: BaseProcessor): EntityHandler {
        this.env = env
        for (support in processor.getSupportedAnnotations()) {
            for (element in env.getElementsAnnotatedWith(support)) {
                if (element.getKind() == ElementKind.FIELD) {
                    handlerField(element as VariableElement)
                }
                if (element.getKind() == ElementKind.METHOD) {
                    handlerMethod(element as ExecutableElement)
                }
                if (element.getKind() == ElementKind.CLASS) {
                    handlerClass(element as TypeElement)
                }
            }
        }
        return this
    }

    private fun handlerClass(element: TypeElement) {

        val classEntity = ClassEntity(types!!, elements, element)
        val className = classEntity.classSimpleName.toString()
        if (classEntityMap[className] == null) {
            classEntityMap[className] = classEntity
        }
    }

    fun getClassEntityMap(): Map<String, ClassEntity> {
        val classEntityMap = HashMap<String, ClassEntity>()
        classEntityMap.clear()
        for ((key, value) in this.classEntityMap) {
            classEntityMap[key] = value
        }
        return classEntityMap
    }


    private fun handlerMethod(element: ExecutableElement) {
        val methodEntity = MethodEntity(element, elements)

        val className = methodEntity.classSimpleName
        if (classEntityMap[className] == null) {
            classEntityMap[className] = ClassEntity(types!!, elements, element.enclosingElement as TypeElement)
        }

        val classEntity = classEntityMap[className]

        classEntity!!.addMethod(methodEntity)
    }


    private fun handlerField(element: VariableElement) {


        val fieldEntity = FieldEntity(element)

        val className = fieldEntity.classSimpleName

        if (classEntityMap[className] == null) {
            classEntityMap[className] = ClassEntity(types!!, elements,
                    element.enclosingElement as TypeElement)
        }

        val classEntity = classEntityMap[className]

        classEntity!!.addFieid(fieldEntity)
    }

    fun generateCode(code: JavaFile) {
        try {
            code.writeTo(filer)
        } catch (e: Exception) {
        }

    }

    companion object {
        private var types: Types? = null
        fun types(): Types? {
            return types
        }
    }
}


