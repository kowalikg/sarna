package pl.edu.agh.sarna.model

class SubtaskStatus (val _id: Long, val description: String, var value: Any) {
    fun toEmoji() : SubtaskStatus {
        value = String(Character.toChars(if (value as Boolean) 0x2714 else 0x274C))
        return this
    }

}