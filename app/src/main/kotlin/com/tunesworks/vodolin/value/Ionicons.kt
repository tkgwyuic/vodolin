package com.tunesworks.vodolin.value

enum class Ionicons(val icon: String) {
    LIGHT_BULB(""),
    HEART(""),
    HEART_BROKEN(""),
    STAR(""),
    ALERT(""),
    BUG(""),
    PAW(""),
    MUSIC(""),
    FLASK(""),

    PERSON(""),
    CLOCK(""),
    BRIEFCASE(""),
    DOCUMENT(""),

    CART(""),

    WALK(""),
    BICYCLE(""),
    CAR(""),
    BUS(""),
    BOAT(""),
    TRAIN(""),
    PLANE(""),

    CALL(""),
    MAIL(""),
    CHAT(""),

    FORK(""),
    COFFEE(""),
    PINT(""),
    WINEGLASS(""),

    SOCIAL_ANDROID(""),
    SOCIAL_APPLE(""),
    SOCIAL_OCTCAT(""),
    SOCIAL_SKYPE(""),
    SOCIAL_TWITTER("");

    companion object {
        val DEFAULT = LIGHT_BULB
    }
}