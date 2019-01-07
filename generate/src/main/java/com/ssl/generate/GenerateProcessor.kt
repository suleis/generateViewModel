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
    var type: Int = 0//当前注解类型
    var mClassNameEntity: ClassNameEntity? = null//class信息
    lateinit var  javaPoetClassName:String//生成的类名
    override fun getSupportedAnnotations(): Array<Class<out Annotation>> {//注册注解
        return arrayOf(LiveData::class.java, ViewModel::class.java, BaseViewModel::class.java)
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        var classEntity = entityHandler.handlerElement(p1!!, this).getClassEntityMap()//取注解分类(class,field)

        try {
            mClassNameEntity = TypeUtil.getBaseViewModelClassName(classEntity as MutableMap<String, ClassEntity>)//判断是否有BaseViewModel

            mClassNameEntity?.run {
                classEntity.remove(this.mElement.simpleName.toString())//存在BaseViewModel的类不被创建
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

    /**
     * ClassEntity 当前所注释过的类信息
     * ClassNameEntity 当前自定义BaseViewModel超类信息
     */
    private fun testJavaPoet(value: ClassEntity, mClassName: ClassNameEntity?): JavaFile? {

        var typeName: TypeMirror? = null
        val typeElement = value.typeElement.get() ?: return null

        TypeUtil.useAndroidX = useAndroidX//判断是否是AndroidX ViewModel 在不同系统版本包名不一样
        javaPoetClassName=typeElement.simpleName.toString().upperCase() + "ViewModel"
        val build = TypeSpec.classBuilder(javaPoetClassName)//创建当前的类
                .addModifiers(Modifier.PUBLIC)//修饰符


        val typeParameterSize = mClassName?.run { this.mType.size }//如果当前自定义BaseViewModel 存在泛型将取出类型
        if (typeParameterSize == 1) {
            val typeParameterElement = mClassName.mType[typeParameterSize - 1]
            typeName = typeParameterElement.bounds[0]
        }


        val className = initAnnotationType(typeElement)
        if (className == null) {
            val fields = value.fieids

            for (fieldEntity in fields.values) {
                var valueTypeName = initAnnotationType(fieldEntity.typeElement)
                if (valueTypeName == null)
                valueTypeName = ClassName.get(fieldEntity.typeMirror)
                brewLiveData(fieldEntity.fieldName, valueTypeName!!, build, typeElement)
            }

        } else {
            brewLiveData(value.classSimpleName.toString(), className, build, typeElement, typeName)
        }

        val getThis =MethodSpec.methodBuilder("get"+javaPoetClassName.upperCase()).addModifiers(Modifier.PUBLIC).returns( ClassName.get(value.classPackage, javaPoetClassName.upperCase())).addStatement("return this").build()
        build.addMethod(getThis)
        return JavaFile.builder(value.classPackage, build.build()).build()


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
        val strUpperCase = fieldName.upperCase()//当前函数、属性名
        val mutableLiveData = ClassName.get(if (useAndroidX) "androidx.lifecycle" else "android.arch.lifecycle", "MutableLiveData")
        val typeName = ParameterizedTypeName.get(mutableLiveData, valueTypeName)//将当前类类型注入到泛型中

        val className_type = getAnnotationType(typeElement, typeParameterName)
        val field = FieldSpec.builder(typeName, "m${strUpperCase}",//创建属性
                Modifier.PRIVATE).initializer("new \$T()", mutableLiveData).build()
        var parameterizedTypeName: ParameterizedTypeName

        try {
//当前自定义BaseViewModel 并且继承ViewModel 设置当前类的超类，如ViewModel注解的superClass与定义的BaseViewModel
// 是继承关系将superClass定义的泛型注入到自定义BaseViewModel   
            if (className_type != null && mClassNameEntity != null) {
                parameterizedTypeName = ParameterizedTypeName.get(mClassNameEntity!!.mClassName, className_type)
                builder.superclass(parameterizedTypeName)
            }

            if (mClassNameEntity == null) {
                builder.superclass(TypeUtil.getClassNameViewModel()!!)
            } else {
                builder.superclass(mClassNameEntity!!.mClassName)
            }
        } catch (e: Exception) {

        }

        val getLiveData = MethodSpec.methodBuilder("get" + strUpperCase + "LiveData")
                .returns(typeName).addModifiers(Modifier.PUBLIC).addStatement("return this.m\$L", strUpperCase).build()
        val getValue = MethodSpec.methodBuilder("get${strUpperCase}Value").addModifiers(Modifier.PUBLIC)
                .returns(valueTypeName).addStatement("return this.m\$L.getValue()", strUpperCase).build()
        val setValue = MethodSpec.methodBuilder("set" + strUpperCase + "Value").addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addStatement("this.m\$L.setValue(m\$L)", strUpperCase, strUpperCase).addParameter(valueTypeName,  "m"+strUpperCase).build()
        val postValue = MethodSpec.methodBuilder("post" + strUpperCase + "Value").addModifiers(Modifier.PUBLIC).returns(TypeName.VOID).addStatement("this.m\$L.postValue(m\$L)", strUpperCase, strUpperCase).addParameter(valueTypeName,  "m"+strUpperCase).build()
        builder.addField(field)
        builder.addMethod(postValue)
        builder.addMethod(getLiveData)
        builder.addMethod(setValue)
        builder.addMethod(getValue)



    }


}
