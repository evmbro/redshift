package com.aifactory.imagine

class ModelHelper {
    /** Abstraction object that wraps a prediction output in an easy to parse way */

    private val predictedImage =  arrayOf(FloatArray())

    private val outputBuffer = mapOf(
        0 to FloatArray(1)
    )
    val predictions get() = (0 until OBJECT_COUNT).map {
      // TODO
    }

    fun predict(image: TensorImage): List<ObjectPrediction> {
        tflite.runForMultipleInputsOutputs(arrayOf(image.buffer), outputBuffer)
        return predictions
    }

}