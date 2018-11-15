package pl.edu.agh.sarna.model

import pl.edu.agh.sarna.utils.kotlin.isKitKat4_4

class SubtaskStatus (val description: String, var value: Any, val _id : Long = 0) {
    fun toEmoji() : SubtaskStatus {
        if (isKitKat4_4()){
            val emojiValue = String(Character.toChars(if (value as Boolean) 0x2714 else 0x274C))
            return SubtaskStatus(description, emojiValue, _id)
        }
        return this
    }

}