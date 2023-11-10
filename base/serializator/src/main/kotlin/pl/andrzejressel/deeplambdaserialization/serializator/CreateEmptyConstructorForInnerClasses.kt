// SPDX-License-Identifier: GPL-2.0-or-later
package pl.andrzejressel.deeplambdaserialization.serializator

import proguard.classfile.Clazz
import proguard.classfile.visitor.ClassVisitor

class CreateEmptyConstructorForInnerClasses : ClassVisitor {
  override fun visitAnyClass(clazz: Clazz?) {}
}
