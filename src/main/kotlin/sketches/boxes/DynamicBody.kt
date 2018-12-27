package sketches.boxes

interface DynamicBody {

    var color: Int

    fun attract(x: Float, y: Float, force: Float)

    fun draw()
}