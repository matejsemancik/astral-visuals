package dev.matsem.astral.visuals.sketches.boxes

import org.jbox2d.collision.shapes.ChainShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.*
import processing.core.PApplet
import shiffman.box2d.Box2DProcessing

class Boundary(sketch: PApplet, box2d: Box2DProcessing) {
    val body: Body
    val fixture: Fixture

    init {
        val bd = BodyDef().apply {
            type = BodyType.STATIC
        }

        body = box2d.createBody(bd)
        val shape = ChainShape()
        shape.createLoop(
                arrayOf(
                        Vec2(box2d.coordPixelsToWorld(0f, 0f)),
                        Vec2(box2d.coordPixelsToWorld(sketch.width.toFloat(), 0f)),
                        Vec2(box2d.coordPixelsToWorld(sketch.width.toFloat(), sketch.height.toFloat())),
                        Vec2(box2d.coordPixelsToWorld(0f, sketch.height.toFloat()))
                ),
                4
        )

        val fd = FixtureDef().apply {
            this.shape = shape
            density = 100f
            friction = 10.0f
            restitution = 0.0f
        }

        fixture = body.createFixture(fd)
    }
}