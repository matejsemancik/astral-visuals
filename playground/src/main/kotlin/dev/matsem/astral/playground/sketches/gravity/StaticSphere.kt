package dev.matsem.astral.playground.sketches.gravity

import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.Fixture
import org.jbox2d.dynamics.FixtureDef
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

    var accentColor = 0
    var fgColor = 0
    var hasFill = true

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
            if (hasFill) fill(fgColor) else noFill()
            if (hasFill) stroke(accentColor) else stroke(fgColor)
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