package pl.edu.agh.sarna.root.tools

fun execCommand(command: String) {
        val process = Runtime.getRuntime().exec("su")
        val out = process.outputStream

        out.write(command.toByteArray())
        out.flush()
        out.close()
}
