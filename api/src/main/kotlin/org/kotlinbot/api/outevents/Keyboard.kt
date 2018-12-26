package org.kotlinbot.api.outevents

import java.util.*

data class Keyboard(
    val buttons: Array<Array<out Button>> = emptyArray(),
    val oneTime: Boolean = false,
    val inline: Boolean = false,
    val optional: Boolean = false
) {
    override fun toString(): String {
        return "Keyboard(buttons=${buttons.map { Arrays.toString(it) }.joinToString(
            ",",
            "[",
            "]"
        )}, oneTime=$oneTime, inline=$inline, optional=$optional)"
    }

    operator fun plus(anotherKeyboard: Keyboard): Keyboard {
        assert(this.inline == anotherKeyboard.inline, { "cant merge inline with noninline keyboard" })
        return Keyboard(
            this.buttons + anotherKeyboard.buttons,
            this.oneTime || anotherKeyboard.oneTime,
            this.inline || anotherKeyboard.inline,
            this.optional || anotherKeyboard.optional
        )
    }

    companion object {
        fun btn(label: String) = TextButton(label)
        fun cbBtn(label: String, data: String) = CallbackButton(label, data)
        fun gpsBtn(label: String) = LocationButton(label)
        fun urlBtn(label: String, url: String) = OpenUrlButton(label, url)

        fun vertiacal(vararg buttons: Button): Keyboard {
            return Keyboard(buttons.map { arrayOf(it) }.toTypedArray())
        }

        fun horizontal(vararg buttons: Button): Keyboard {
            return Keyboard(arrayOf(buttons))
        }

        fun of(vararg rows: Array<out Button>): Keyboard {
            return Keyboard(rows.map { it }.toTypedArray())
        }
    }

}

sealed class Button {
    abstract val label: String
}

data class TextButton(override val label: String) : Button()
data class CallbackButton(override val label: String, val data: String) : Button()
data class LocationButton(override val label: String) : Button()
data class OpenUrlButton(override val label: String, val url: String) : Button()

val HIDE_KEYBOARD = Keyboard()


fun String.toKeyboard(oneTime: Boolean = false, optional: Boolean = false): Keyboard {
    return Keyboard(
        buttons = prepareStringToKeys(),
        oneTime = oneTime,
        optional = optional
    )
}

fun String.toKeyboardInline(spliter: String? = null, optional: Boolean = false): Keyboard {
    return Keyboard(
        buttons = prepareStringToKeys(spliter),
        oneTime = false,
        inline = true,
        optional = optional
    )
}

private fun String.prepareStringToKeys(splitChar: String? = null): Array<Array<out Button>> {
    return this.split("\n")
        .map { row ->
            row.split("|").map { label ->
                if (splitChar != null) {
                    val (data, title) = label.split(splitChar)
                    CallbackButton(title, data)
                } else {
                    TextButton(label)
                }


            }.toTypedArray()
        }
        .toTypedArray()
}


