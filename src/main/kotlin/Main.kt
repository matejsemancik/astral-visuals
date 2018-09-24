
import processing.core.PApplet
import sketches.polygonal.PolygonalSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
    val sketch = PolygonalSketch()
//    val sketch = TerrainSketch()
//    val sketch = FibSphereSketch()
    PApplet.runSketch(processingArgs, sketch)
}