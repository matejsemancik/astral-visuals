
import processing.core.PApplet
import sketches.polygonal.PolygonalSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("PolygonalSketch")
    val sketch = PolygonalSketch()
    PApplet.runSketch(processingArgs, sketch)
}