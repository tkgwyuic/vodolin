package com.tunesworks.vodolin.event

import com.tunesworks.vodolin.model.ToDo
import com.tunesworks.vodolin.value.ItemColor

class ToDoEvent {
    data class ChangeAll(val itemColorname: String)
    //data class Change(val uuid: String)
    data class Update(val todo: ToDo)
    data class Delete(val todo: ToDo)
    data class Change(val newTodo: ToDo, val oldTodo: ToDo)
}