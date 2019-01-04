package com.ssl.generate.utils

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.ssl.annotation.BaseViewModel
import com.ssl.annotation.ViewModel
import com.ssl.annotation.utils.LiveDataAttributeType
import com.ssl.generate.entity.ClassEntity
import com.ssl.generate.entity.ClassNameEntity
import com.ssl.generate.entity.EntityHandler
import com.ssl.generate.entity.MirroredType
import java.util.ArrayList
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.NullType
import javax.lang.model.type.TypeMirror

object TypeUtil {
    var STRING_TYPE: ClassName = ClassName.get(String::class.java)
    var ARRAY_LIST_TYPE = ClassName.get(ArrayList::class.java)
    var SET_TYPE = ClassName.get(Set::class.java)
    var MAP_TYPE = ClassName.get(Map::class.java)
    val JAVA_VIEWMODEL_TYPE = 1
    val BASE_VIEWMODEL = 2
    val NULL_TYPE = 4;
    var useAndroidX: Boolean = false

    fun arrayListType(genericType: ClassName): ParameterizedTypeName {
        return ParameterizedTypeName.get(ARRAY_LIST_TYPE, genericType)
    }

    fun setType(genericType: ClassName): ParameterizedTypeName {
        return ParameterizedTypeName.get(SET_TYPE, genericType)
    }

    fun mapType(V: ClassName): ParameterizedTypeName {
        return ParameterizedTypeName.get(MAP_TYPE, STRING_TYPE, V)
    }

    @Throws(Exception::class)
    fun getBaseViewModelClassName(typeElements: MutableMap<String, ClassEntity>): ClassNameEntity? {
        var baseViewModelType: MirroredType?
        for (entry in typeElements.entries) {
            val typeElement = entry.value.typeElement.get()
            baseViewModelType = isNullAnnotationType(typeElement!!, BASE_VIEWMODEL)
            if (baseViewModelType != null) {
                return ClassNameEntity(typeElement, baseViewModelType, baseViewModelType.type)
            }
        }


        return null
    }


    fun getAnnotationType(typeElement: TypeElement, typeParameterName: TypeMirror?): ClassName? {
        val mirroredType = isNullAnnotationType(typeElement, JAVA_VIEWMODEL_TYPE)
        val defaultClass = "com.ssl.annotation.entity.Null"

        return if (mirroredType != null && defaultClass != mirroredType.element.toString()
                && EntityHandler.types()!!.isSubtype(mirroredType.classTypeMirror, typeParameterName)) {
            ClassName.get(mirroredType.element)
        } else null

    }

    private fun isNullAnnotationType(typeElement: TypeElement, type: Int): MirroredType? {
        try {
            when (type) {
                JAVA_VIEWMODEL_TYPE -> typeElement.getAnnotation(ViewModel::class.java).superClass
                BASE_VIEWMODEL -> typeElement.getAnnotation(BaseViewModel::class.java).bvmTypeClass
            }
        } catch (e: MirroredTypeException) {
            val mirroredType = MirroredType(e, type)
            return if (type == BASE_VIEWMODEL) {
                if (isExtendsViewModel(mirroredType.element)) {
                    mirroredType.type = BASE_VIEWMODEL
                    return mirroredType
                } else {
                    mirroredType.type = NULL_TYPE
                    return mirroredType
                }
            } else mirroredType

        } catch (e: NullPointerException) {
            return null
        }

        return null
    }

    fun getTypeLiveDataClass(type: LiveDataAttributeType, value: TypeElement): TypeName? {
        val className = ClassName.get(value)
        when (type) {
            LiveDataAttributeType.DEFAULT -> return className
            LiveDataAttributeType.ARR_LIST -> return arrayListType(className)
            LiveDataAttributeType.SET -> return setType(className)
            LiveDataAttributeType.MAP -> return mapType(className)
            else -> return null

        }
    }

    private fun isExtendsViewModel(element: TypeElement): Boolean {
        val superclass = element.superclass.toString()
        return "android.arch.lifecycle.ViewModel" == superclass || "androidx.lifecycle.ViewModel" == superclass
    }
}