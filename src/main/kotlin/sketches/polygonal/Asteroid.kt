package sketches.polygonal

import ddf.minim.analysis.FFT
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape
import processing.core.PVector

class Asteroid(private val sketch: PApplet, centerAverage: Boolean = false, private val fft: FFT) {

    private val skeletonVectors = mutableListOf<PVector>()
    private val group = sketch.createShape(PConstants.GROUP)

    companion object {
        const val NUMBER_VECTORS = 4
    }

    val xAvg = mutableListOf<Float>()
    val yAvg = mutableListOf<Float>()
    val zAvg = mutableListOf<Float>()

    init {
        repeat(NUMBER_VECTORS, action = {
            val random3D = PVector.random3D().mult(sketch.width / 3.5f)

            if (centerAverage) {
                xAvg.add(random3D.x)
                yAvg.add(random3D.y)
                zAvg.add(random3D.z)
            }

            skeletonVectors.add(random3D)
        })

        val shapeVectors = skeletonVectors.toMutableList()
        if (centerAverage) {
            val midpoint = PVector(xAvg.average().toFloat(), yAvg.average().toFloat(), zAvg.average().toFloat())

            repeat(NUMBER_VECTORS, action = { i ->
                shapeVectors[i] = shapeVectors[i].sub(midpoint)
            })
        }

        shapeVectors.add(shapeVectors[0])
        shapeVectors.add(shapeVectors[1])

        for (i in 0 until shapeVectors.size - 2) {
            val polygon = sketch.createShape()
            polygon.beginShape()

            polygon.fill(0f)
            polygon.stroke(0f, 255f, 100f)
            polygon.strokeWeight(5f)

            val currentVector = shapeVectors[i]
            polygon.vertex(currentVector.x, currentVector.y, currentVector.z)

            for (n in 0 + i until 3 + i) {
                if (n != i) {
                    polygon.vertex(shapeVectors[n].x, shapeVectors[n].y, shapeVectors[n].z)
                }
            }
            polygon.endShape(PConstants.CLOSE)

            group.addChild(polygon)
        }
    }

    fun draw() {
        // modify vectors
        val shapeVectors = skeletonVectors
                .map { pVector -> pVector.copy() }
                .toMutableList()

        for (i in 0 until shapeVectors.size) {
            // scale by FFT
            shapeVectors[i].add(shapeVectors[i].copy().mult(PApplet.map(fft.getAvg(i), 0f, 300f, 0f, 0.5f)))
        }

        shapeVectors.apply {
            this.add(this[0])
            this.add(this[1])
        }

        // render

        for (i in 0 until shapeVectors.size - 2) {
            val polygon = sketch.createShape()
            polygon.beginShape()

            polygon.fill(0f)
            polygon.stroke(0f, 255f, 100f)
            polygon.strokeWeight(5f)

            val currentVector = shapeVectors[i]
            polygon.vertex(currentVector.x, currentVector.y, currentVector.z)

            for (n in 0 + i until 3 + i) {
                if (n != i) {
                    polygon.vertex(shapeVectors[n].x, shapeVectors[n].y, shapeVectors[n].z)
                }
            }
            polygon.endShape(PConstants.CLOSE)

            group.removeChild(0)
            group.addChild(polygon)
        }

        sketch.shape(group)
    }

    fun wiggle() {
        // Wiggle wiggle wiggle
        for (shape in group.children) {
            for (i in 0 until shape.vertexCount) {
                val offsetVector = PVector.random3D().mult(0.8f)
                val originalVector = shape.getVertex(i)
                shape.setVertex(i, originalVector.add(offsetVector))
            }
        }
    }

    fun getShape(): PShape = group
}