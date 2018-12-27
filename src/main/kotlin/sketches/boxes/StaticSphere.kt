package sketches.boxes

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.*
import processing.core.PApplet
import processing.core.PConstants
import shiffman.box2d.Box2DProcessing

class StaticSphere(private val sketch: PApplet, private val box2d: Box2DProcessing, val x: Float, val y: Float) {
    // Init with maximum theoretical radius possible,
    // bodies start to tunnel through when changing shape's radius above this value at runtime
    var radius = sketch.width.toFloat()
        set(value) {
            field = value
            fixture.shape.radius = box2d.scalarPixelsToWorld(radius)
        }

    var color = 0

    val body: Body
    val shape: CircleShape
    val fixture: Fixture

    init {
        val bd = BodyDef().apply {
            position.set(box2d.coordPixelsToWorld(x, y))
            type = BodyType.STATIC
        }

        body = box2d.createBody(bd)
        shape = CircleShape()
        shape.radius = box2d.scalarPixelsToWorld(radius)

        val fd = FixtureDef().apply {
            this.shape = this@StaticSphere.shape
            density = 20f
            friction = 10f
            restitution = 1.0f
        }

        fixture = body.createFixture(fd)
    }

    fun draw() {
        with(sketch) {
            noFill()
            stroke(color)
            strokeWeight(4f)

            pushMatrix()
            val coords = box2d.getBodyPixelCoord(body)
            translate(coords.x, coords.y)
            rotateX(millis() / 1000f * PConstants.TWO_PI / 12f)
            rotateY(millis() / 1000f * PConstants.TWO_PI / 24f)
            rotateZ(millis() / 1000f * PConstants.TWO_PI / 32f)

            sphere(radius * 0.8f)
            popMatrix()
        }
    }
}