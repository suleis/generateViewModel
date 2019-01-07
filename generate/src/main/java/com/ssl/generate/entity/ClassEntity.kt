package com.ssl.generate.entity

import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author 苏磊
 * @describe
 */
class ClassEntity : GenerateBaseEntity {
    var className: String = ""//类名
    var classPackage: String = ""//包名
    var simpleName: String = ""
    var classSuperName: String = ""
    var types: Types;
    var classSimpleName: Name
    var classQualifiedName: Name
    var modifier: Set<Modifier>

    var fieids: MutableMap<String, FieldEntity> = HashMap()
    var methods: MutableMap<String, MethodEntity> = HashMap()
    var interfaces: MutableList<String> = ArrayList()
    var typeElement: WeakReference<TypeElement>
    var annotationMirror: List<AnnotationMirror>

    constructor(types: Types, element: Elements, typeElement: TypeElement) {

        classPackage = element.getPackageOf(typeElement).qualifiedName.toString()
        this.types = types;this.typeElement = WeakReference(typeElement)
        modifier = typeElement.modifiers
        classSuperName = typeElement.superclass.toString()
        className = typeElement.toString()
        simpleName = typeElement.simpleName.toString()
        classSimpleName = typeElement.simpleName
        classQualifiedName = typeElement.qualifiedName
        annotationMirror = typeElement.annotationMirrors
        typeElement.interfaces.forEach { interfaces.add(types.asElement(it).toString()) }

    }

    fun addFieid(fieidEntity: FieldEntity) {
        val key = fieidEntity.element.toString()
        if (fieids[key] == null) {
            fieids[key] = fieidEntity
        }
    }

    fun addMethod(methodEntity: MethodEntity) {
        val key = methodEntity.methodElement.toString()
        if (methods[key] == null) {
            methods[key] = methodEntity
        }
    }
}
