package com.ssl.generate

import com.squareup.javapoet.*
import com.ssl.annotation.BaseViewModel
import com.ssl.annotation.LiveData
import com.ssl.annotation.ViewModel
import com.ssl.generate.base.BaseProcessor
import com.ssl.generate.entity.ClassEntity
import com.ssl.generate.entity.ClassNameEntity
import com.ssl.generate.utils.TypeUtil
import com.ssl.generate.utils.TypeUtil.BASE_VIEWMODEL
import com.ssl.generate.utils.TypeUtil.getAnnotationType
import com.ssl.generate.utils.TypeUtil.useAndroidX
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

@SupportedSourceVersion(SourceVersion.RELEASE_7)
class GenerateProcessor : BaseProcessor() {
    var type: Int = 0;
    var mClassNameEntity: ClassNameEntity? = null
    override fun getSupportedAnnotations(): Array<Class<out Annotation>> {
        return arrayOf(LiveData::class.java, ViewModel::class.java, BaseViewModel::class.java)
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        var classEntity = entityHandler.handlerElement(p1!!, this).getClassEntityMap()

        try {
            mClassNameEntity = TypeUtil.getBaseViewModelClassName(classEntity as MutableMap<String, ClassEntity>)

            mClassNameEntity?.run {
                classEntity.remove(this.mElement.simpleName.toString())
                if (this.tag != BASE_VIEWMODEL) {
                    mClassNameEntity = null
                }
            }
        } catch (e: Exception) {
        }

        for ((_, value) in classEntity) {
            val javaFile = testJavaPoet(value, mClassNameEntity)
            if (javaFile != null) {
                entityHandler.generateCode(javaFile)
            }
        }
        return true
    }

    private fun testJavaPoet(value: ClassEntity, mClassName: ClassNameEntity?): JavaFile? {

        var typeName: TypeMirror? = null
        val typeElement = value.typeElement.get() ?: return null

        TypeUtil.useAndroidX = useAndroidX

        val build = TypeSpec.classBuilder(typeElement.simpleName.toString() + "ViewModel")
                .addModifiers(Modifier.PUBLIC)


        val typeParameterSize = mClassName?.run { this.mType.size }
        if (typeParameterSize == 1) {
            val typeParameterElement = mClassName.mType[typeParameterSize - 1]
            typeName = typeParameterElement.bounds[0]
        }


        val className = initAnnotationType(typeElement)
        if (className == null) {
            val fields = value.fieids

            for (fieldEntity in fields.values) {
                var valueTypeName = initAnnotationType(fieldEntity.typeElement)
                if (valueTypeName == null);
                valueTypeName = ClassName.get(fieldEntity.typeMirror)
                brewLiveData(fieldEntity.fieldName, valueTypeName!!, build, typeElement)
            }

        } else {
            brewLiveData(value.classSimpleName.toString(), className, build, typeElement, typeName)
        }
        val typeSpec = build.build()

        return JavaFile.builder(value.classPackage, typeSpec).build()


    }

    private fun initAnnotationType(typeElement: TypeElement): TypeName? {
        val KotlinLiveData = typeElement.getAnnotation(LiveData::class.java)
        if (KotlinLiveData != null) {
            type = TypeUtil.JAVA_VIEWMODEL_TYPE
            return TypeUtil.getTypeLiveDataClass(KotlinLiveData.attribute, typeElement)
        } else
            if (typeElement.getAnnotation(BaseViewModel::class.java) != null) {
                return null
            }
        return null
    }

    private fun brewLiveData(fieldName: String, valueTypeName: TypeName, builder: TypeSpec.Builder, typeElement: TypeElement, typeParameterName: TypeMirror? = null) {

        val mutableLiveData = ClassName.get(if (useAndroidX) "androidx.lifecycle" else "android.arch.lifecycle", "MutableLiveData")
        val typeName = ParameterizedTypeName.get(mutableLiveData, valueTypeName)

        val className_type = getAnnotationType(typeElement, typeParameterName)
        val field = FieldSpec.builder(typeName, "m$fieldName", Modifier.PRIVATE).initializer("new \$T()", mutableLiveData).build()
        var parameterizedTypeName: ParameterizedTypeName

        try {
            if (className_type != null && mClassNameEntity != null) {
                parameterizedTypeName = ParameterizedTypeName.get(mClassNameEntity!!.mClassName, className_type)
                builder.superclass(parameterizedTypeName)
            }
            if (mClassNameEntity == null) {
                val viewModel = ClassName.get(if (useAndroidX)
                    "androidx.lifecycle"
                else
                    "android.arch.lifecycle", "ViewModel")
                builder.superclass(viewModel)
            } else {
                builder.superclass(mClassNameEntity!!.mClassName)
            }
        } catch (e: Exception) {

        }

//        val className = ClassName.bestGuess(valueTypeName.toString() + "ViewModel")

        val getValue = MethodSpec.methodBuilder("getValue").addModifiers(Modifier.PUBLIC)
                .returns(valueTypeName).addStatement("return this.m\$L.getValue()", fieldName).build()
        val setValue = MethodSpec.methodBuilder("set" + fieldName + "Value").addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addStatement("this.m\$L.setValue(\$L)", fieldName, fieldName).addParameter(valueTypeName, fieldName).build()
        val postValue = MethodSpec.methodBuilder("post" + fieldName + "Value").addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addStatement("this.m\$L.postValue(\$L)", fieldName, fieldName).addParameter(valueTypeName, fieldName).build()
        builder.addField(field)
        builder.addMethod(postValue)
        builder.addMethod(setValue)
        builder.addMethod(getValue)

    }


}
