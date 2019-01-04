package com.ssl.generate.entity

import com.squareup.javapoet.ClassName
import com.ssl.annotation.entity.GenerateBaseEntity

import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement

class ClassNameEntity :GenerateBaseEntity {
    var mClassName: ClassName
    var mElement: TypeElement
    var mMirroredType: MirroredType
    var mType: List<TypeParameterElement>
    var tag = 0


    constructor(element: TypeElement, type: MirroredType, baseViewModel: Int) {
        this.mElement = element
        this.mClassName = ClassName.get(type.element)
        this.mMirroredType = type
        this.mType = type.element.typeParameters
        this.tag = baseViewModel
    }
}
