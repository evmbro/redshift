package com.aifactory.imagine

import android.graphics.Bitmap
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ModelHelper(private val tfLite: Interpreter) {
    private val postProcessor = TensorProcessor
        .Builder()
        .add(NormalizeOp(0f, 1/255f))
        .add(CastOp(DataType.UINT8))
        .build()
    private val preProcessor = ImageProcessor
        .Builder()
        .add(CastOp(DataType.FLOAT32))
        .add(NormalizeOp(0f, 255f))
        .build()

    fun process(image: Bitmap): Bitmap? {
        val tensorImage = preProcessor.process(TensorImage.fromBitmap(image))
        val outputImage = TensorBuffer.createFrom(tensorImage.tensorBuffer, DataType.FLOAT32)
        tfLite.resizeInput(0, arrayOf(1, image.width, image.height, 3).toIntArray())
        tfLite.run(tensorImage.buffer, outputImage.buffer)

        val processed = postProcessor.process(outputImage)
        val tensorImageProcessed = TensorImage(DataType.UINT8)
        tensorImageProcessed.load(processed)
        return tensorImageProcessed.bitmap
    }

}
