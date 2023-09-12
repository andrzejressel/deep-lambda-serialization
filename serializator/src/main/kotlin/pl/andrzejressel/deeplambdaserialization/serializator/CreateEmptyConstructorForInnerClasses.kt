package pl.andrzejressel.deeplambdaserialization.serializator

import proguard.classfile.Clazz
import proguard.classfile.visitor.ClassVisitor

class CreateEmptyConstructorForInnerClasses: ClassVisitor {
    override fun visitAnyClass(clazz: Clazz?) {

    }
}