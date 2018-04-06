package sketches.polygonal

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape
import processing.core.PVector

class Triangloid(private val sketch: PApplet, private val x: Float, private val y: Float) {

    private val vectors = mutableListOf<PVector>()
    private val group = sketch.createShape(PConstants.GROUP)

    companion object {
        const val NUMBER_VECTORS = 4
    }

    init {
        repeat(NUMBER_VECTORS, action = {
            val vector = PVector.random3D().mult(sketch.width / 4.5f)
            vectors.add(vector)
        })

        vectors.add(vectors[0])
        vectors.add(vectors[1])

        for (i in 0 until vectors.size - 2) {
            val polygon = sketch.createShape()
            polygon.beginShape()

            polygon.fill(0f)
            polygon.stroke(0f, 255f, 100f)
            polygon.strokeWeight(5f)

            val currentVector = vectors[i]
            polygon.vertex(currentVector.x, currentVector.y, currentVector.z)

            for (n in 0 + i until 3 + i) {
                if (n != i) {
                    polygon.vertex(vectors[n].x, vectors[n].y, vectors[n].z)
                }
            }
            polygon.endShape(PConstants.CLOSE)

            group.addChild(polygon)

            val box = sketch.createShape(PConstants.BOX, 20f)
            group.addChild(box)
        }
    }

    fun draw() {
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