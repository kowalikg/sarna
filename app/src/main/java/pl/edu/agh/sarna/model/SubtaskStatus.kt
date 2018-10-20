package pl.edu.agh.sarna.model

class SubtaskStatus (val description: String, var value: Any, val _id : Long = 0) {
    fun toEmoji() : SubtaskStatus {
        value = String(Character.toChars(if (value as Boolean) 0x2714 else 0x274C))
        return this
    }

}