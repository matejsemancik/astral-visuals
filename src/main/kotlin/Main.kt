
import processing.core.PApplet
import sketches.polygonal.PolygonalSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
    val sketch = PolygonalSketch()
    PApplet.runSketch(processingArgs, sketch)
}