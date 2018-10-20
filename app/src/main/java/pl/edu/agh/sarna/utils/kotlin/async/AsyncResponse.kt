package pl.edu.agh.sarna.utils.kotlin.async

interface AsyncResponse {
    fun processFinish(output: Any)
    fun load(output: Any) {}
}