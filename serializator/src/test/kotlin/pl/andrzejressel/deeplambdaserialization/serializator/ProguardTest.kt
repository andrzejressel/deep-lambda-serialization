package pl.andrzejressel.deeplambdaserialization.serializator

import org.junit.jupiter.api.Test
import proguard.classfile.AccessConstants
import proguard.classfile.ClassConstants
import proguard.classfile.VersionConstants
import proguard.classfile.editor.ClassBuilder

class ProguardTest {

    @Test
    fun test() {
        val programClass =
            ClassBuilder(
                VersionConstants.CLASS_VERSION_11,
                AccessConstants.PUBLIC,
                "EntryPoint",
                ClassConstants.NAME_JAVA_LANG_OBJECT)

                .addMethod(
                    AccessConstants.PUBLIC or  AccessConstants.STATIC,
                    "run",
                    "([Ljava/lang/Object;)Ljava/lang/Object;",
                    50
                ) { code ->

                    val descriptor = "IForgotParentheses);"

                    code
                        .new_("MyClass")
                        .dup()
                        .invokespecial("MyClass", "<init>", "()V")
                        .astore_1()
                        .aload_1()
                        .aload_0()
                        .invokestatic("MyClass", "runObject", descriptor)
                        .areturn()
//
                }
                .programClass;
    }

}