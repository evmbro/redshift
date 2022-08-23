package com.aifactory.imagine

import android.graphics.Bitmap
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ModelHelper(private val tfLite: Interpreter) {

    fun process(image: Bitmap): Bitmap? {
        val preProcessor = ImageProcessor
                .Builder()
                .add(NormalizeOp(0f, 1f))
                .build()
        val tensorImage = preProcessor.process(TensorImage.fromBitmap(image))
        val outputImage = TensorBuffer.createDynamic(DataType.FLOAT32)
        tfLite.run(tensorImage.buffer, outputImage.buffer)
        val postProcessor = TensorProcessor
            .Builder()
            .add(NormalizeOp(0f, 255f))
            .build()
        val processed = postProcessor.process(outputImage)
        val tensorImageProcessed = TensorImage(DataType.FLOAT32)
        tensorImageProcessed.load(processed)
        return tensorImageProcessed.bitmap
    }

}
