package pl.edu.agh.sarna.model

interface AsyncResponse {
    fun processFinish(output: Any)
    fun load(output: Any) {}
}