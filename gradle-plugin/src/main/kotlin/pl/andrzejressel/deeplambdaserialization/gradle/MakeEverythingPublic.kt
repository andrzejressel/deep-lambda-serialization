package pl.andrzejressel.deeplambdaserialization.gradle

import proguard.classfile.AccessConstants
import proguard.classfile.Clazz
import proguard.classfile.ProgramClass
import proguard.classfile.ProgramMember
import proguard.classfile.visitor.ClassVisitor
import proguard.classfile.visitor.MemberVisitor

class MakeEverythingPublic: ClassVisitor, MemberVisitor {
    override fun visitProgramClass(programClass: ProgramClass) {
        programClass.u2accessFlags = programClass.u2accessFlags and AccessConstants.PRIVATE.inv()
        programClass.u2accessFlags = programClass.u2accessFlags or AccessConstants.PUBLIC
        programClass.methodsAccept(this)
    }

    override fun visitProgramMember(programClass: ProgramClass, programMember: ProgramMember) {
        programMember.u2accessFlags = programMember.u2accessFlags and AccessConstants.PRIVATE.inv()
        programMember.u2accessFlags = programMember.u2accessFlags or AccessConstants.PUBLIC
    }

    override fun visitAnyClass(clazz: Clazz) {
        throw IllegalStateException("visitAnyClass is unsupported")
    }
}