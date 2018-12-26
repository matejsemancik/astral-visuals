package sketches.boxes

import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import processing.core.PApplet
import processing.core.PConstants
import shiffman.box2d.Box2DProcessing

class Box(private val sketch: PApplet, private val box2d: Box2DProcessing, val x: Float, val y: Float) {
    var size = 25f
    var color = 0
    var strokeWeight = 3f
    var rotationConstant = sketch.random(6f, 10f)

    val body: Body
    val fixture: Fixture

    init {
        val bd = BodyDef().apply {
            position.set(box2d.coordPixelsToWorld(x, y))
            type = BodyType.DYNAMIC
            linearDamping = 0.5f
        }

        body = box2d.createBody(bd)
        val shape = PolygonShape()
        shape.setAsBox(
                box2d.scalarPixelsToWorld(size / 2f),
                box2d.scalarPixelsToWorld(size / 2f)
        )

        val fd = FixtureDef().apply {
            this.shape = shape
            density = 1f
            friction = 1f
            restitution = 0.3f
        }

        fixture = body.createFixture(fd)
    }

    fun applyForce(force: Vec2) {
        body.applyForce(force, body.worldCenter)
    }

    fun attract(x: Float, y: Float, force: Float) {
        val targetVec = box2d.coordPixelsToWorld(x, y)
        val bodyVec = body.worldCenter

        targetVec.subLocal(bodyVec)
        targetVec.normalize()
        targetVec.mulLocal(force)
        body.applyForce(targetVec, bodyVec)
    }

    fun draw() {
        with(sketch) {
            noFill()
            stroke(color)
            strokeWeight(strokeWeight)

            pushMatrix()
            val coords = box2d.getBodyPixelCoord(body)
            val angle = -body.angle
            translate(coords.x, coords.y)
            rotateX(millis() / 1000f * PConstants.TWO_PI / rotationConstant)
            rotateZ(angle)
            sketch.box(size)
            popMatrix()
        }
    }
}