// SPDX-License-Identifier: GPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.gradle

import java.io.Serializable

data class SimpleModule(val group: String, val name: String) : Serializable
