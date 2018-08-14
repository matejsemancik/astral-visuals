
import processing.core.PApplet
import sketches.terrain.TerrainSketch

fun main(args: Array<String>) {
    val processingArgs = arrayOf("Sketch")
//    val sketch = PolygonalSketch()
    val sketch = TerrainSketch()
//    val sketch = MidiSketch()
    PApplet.runSketch(processingArgs, sketch)
}