package dev.matsem.astral.visuals.sketches.boxes

import dev.matsem.astral.core.tools.extensions.toPVector
import dev.matsem.astral.core.tools.extensions.toVec2
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PConstants.TWO_PI
import shiffman.box2d.Box2DProcessing

class Box(
        private val sketch: PApplet,
        private val box2d: Box2DProcessing,
        val x: Float,
        val y: Float
) {

    companion object {
        const val DEBUG = false
    }

    var accentColor = 0
    var fgColor = 0

    var size = 25f
    var strokeWeight = 3f
    var hasFill = true
    var rotationConstant = sketch.random(6f, 10f)

    val body: Body
    val fixture: Fixture

    init {
        val bd = BodyDef().apply {
            position.set(box2d.coordPixelsToWorld(x, y))
            type = BodyType.DYNAMIC
            linearDamping = 0.01f
        }

        body = box2d.createBody(bd)
        val shape = PolygonShape()
        shape.setAsBox(
                box2d.scalarPixelsToWorld(size / 2f),
                box2d.scalarPixelsToWorld(size / 2f)
        )

        val fd = FixtureDef().apply {
            this.shape = shape
            density = sketch.random(0.2f, 1.2f)
            friction = 1f
            restitution = sketch.random(0.2f, 0.9f)
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

    fun boostOrbit(force: Float) {
        val coords = box2d.getBodyPixelCoord(body).toPVector()
        val accelVec = coords.copy().normalize().rotate(PConstants.PI / 2f).normalize().mult(force)
        val forceVec = coords.copy().add(accelVec)
        val targetVec = box2d.coordPixelsToWorld(forceVec.toVec2())

        body.applyForce(targetVec, body.worldCenter)
    }

    fun draw() {
        with(sketch) {
            if (hasFill) fill(fgColor) else noFill()
            if (hasFill) stroke(accentColor) else stroke(fgColor)
            strokeWeight(strokeWeight)

            // Box itself
            pushMatrix()
            val coords = box2d.getBodyPixelCoord(body)
            val angle = -body.angle
            translate(coords.x, coords.y)
            rotateX(millis() / 1000f * TWO_PI / rotationConstant)
            rotateZ(angle)
            sketch.box(size)
            popMatrix()

            // Debug vectors
            if (DEBUG) {
                val mainVec = coords.toPVector()
                val accelVec = mainVec.copy().normalize().rotate(PConstants.PI / 2f).normalize().mult(50f)
                val forceVec = mainVec.copy().add(accelVec)

                noFill()
                strokeWeight(2f)

                pushMatrix()
                stroke(fgColor)
                line(0f, 0f, mainVec.x, mainVec.y)

                stroke(accentColor)
                line(mainVec.x, mainVec.y, forceVec.x, forceVec.y)
                popMatrix()
            }
        }
    }

    fun destroy() {
        box2d.destroyBody(body)
    }
}