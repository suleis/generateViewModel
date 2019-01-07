package com.ssl.generate.entity


import com.ssl.generate.upperCase
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * @author 苏磊
 * @describe
 */
class FieldEntity(internal var element: VariableElement) : GenerateBaseEntity() {
    internal var typeElement: TypeElement
    internal var classSimpleName: String
    internal var classQualifiedName: String
    internal var fieldName: String
    internal var typeName: String
    internal val typeMirror: TypeMirror

    init {
        this.classSimpleName = element.enclosingElement.simpleName.toString()
        typeElement = element.enclosingElement as TypeElement
        this.classQualifiedName = typeElement.qualifiedName.toString()
        this.fieldName = element.simpleName.toString()
        this.typeMirror = element.asType()
        this.typeName = typeMirror.toString()
    }

    fun getFieldName(): String {
        return fieldName.run { upperCase() }
    }


    fun <T : Annotation> getAnnotation(clazz: Class<T>): T {
        return element.getAnnotation(clazz)
    }


}
