import com.example.framereality.ModelImage

data class ModelItem(
    val id: String = "",
    val uid: String = "",
    val title: String = "",
    val city: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val timestamp: Long = 0L,
    var isFavourite: Boolean = false,
    // Now each key in 'images' maps to a ModelImage object
    var Images: Map<String, ModelImage>? = null
)
