package com.tunesworks.vodolin.event

class ToDoEvent {
    data class ChangeAll(val itemColorname: String)
    data class Change(val uuid: String)
}