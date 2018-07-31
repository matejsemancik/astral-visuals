
import processing.core.PApplet
import sketches.polygonal.PolygonalSketch
import sketches.test.TestSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
    val sketch = PolygonalSketch()
//    val sketch = TestSketch()
    PApplet.runSketch(processingArgs, sketch)
}