// SPDX-License-Identifier: LGPL-3.0-or-later
package pl.andrzejressel.deeplambdaserialization.aws.handler

import com.amazonaws.services.lambda.runtime.Context
import java.io.InputStream
import java.io.OutputStream

data class LambdaIOContext(
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val context: Context
)
