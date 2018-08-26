
import processing.core.PApplet
import sketches.polygonal.PolygonalSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
    val sketch = PolygonalSketch()
//    val sketch = TerrainSketch()
    PApplet.runSketch(processingArgs, sketch)
}