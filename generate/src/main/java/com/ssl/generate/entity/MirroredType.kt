package com.ssl.generate.entity

import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

class MirroredType : GenerateBaseEntity {
    var type: Int = 0
    var element: TypeElement
    var classTypeMirror: DeclaredType
    var qualifiedSuperClassName: String
    var simpleTypeName: String

    constructor(e: MirroredTypeException, type: Int) {
        classTypeMirror = e.typeMirror as DeclaredType

        element = classTypeMirror.asElement() as TypeElement
        qualifiedSuperClassName = element.qualifiedName.toString()
        simpleTypeName = element.simpleName.toString()
        this.type = type

    }


    override fun toString(): String {
        return "MirroredType{" +
                "type=" + type +
                ", element=" + element +
                ", classTypeMirror=" + classTypeMirror +
                ", qualifiedSuperClassName='" + qualifiedSuperClassName + '\''.toString() +
                ", simpleTypeName='" + simpleTypeName + '\''.toString() +
                '}'.toString()
    }
}
