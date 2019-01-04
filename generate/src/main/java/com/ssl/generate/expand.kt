package com.ssl.generate

fun String.upperCase():String{
    var toCharArray = toCharArray()
    if (toCharArray[0] >= 'a' && toCharArray[0] <= 'z') {
        toCharArray[0] = (toCharArray[0].toInt() - 32).toChar()
    }
    return String(toCharArray)

}