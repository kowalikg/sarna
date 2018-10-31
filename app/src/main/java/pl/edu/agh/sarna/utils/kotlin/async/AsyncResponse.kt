package pl.edu.agh.sarna.utils.kotlin.async

interface AsyncResponse {
    fun processFinish(output: Any)
    fun onFirstFinished(output: Any) {}
    fun onSecondFinished(output: Any) {}
}