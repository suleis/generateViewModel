package com.ssl.generate.entity

import com.ssl.annotation.entity.GenerateBaseEntity
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * Created by weilu on 2017/12/14.
 */

class MethodEntity(val methodElement: ExecutableElement , elementUtils: Elements): GenerateBaseEntity() {
    internal val packageName: String
    internal val returnType: String
    internal val parameterElements: List<VariableElement>
    internal val typeParameterElements: List<TypeParameterElement>
    internal val isVarArgs: Boolean
    internal val methodName: String
    internal val exceptionTypes: List<TypeMirror>
    internal val classSimpleName: String
    internal val classQualifiedName: String

    init {
        this.returnType = methodElement.returnType.toString()
        this.parameterElements = methodElement.parameters
        this.isVarArgs = methodElement.isVarArgs
        this.methodName = methodElement.simpleName.toString()
        this.exceptionTypes = methodElement.thrownTypes
        this.typeParameterElements = methodElement.typeParameters
        this.classSimpleName = methodElement.enclosingElement.simpleName.toString()
        this.classQualifiedName = (methodElement.enclosingElement as TypeElement).qualifiedName.toString()
        this.packageName = elementUtils.getPackageOf(methodElement).qualifiedName.toString()
    }

    fun <T : Annotation> getAnnotation(clazz: Class<T>): T {
        return methodElement.getAnnotation(clazz)
    }


}
