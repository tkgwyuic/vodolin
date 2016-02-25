package com.tunesworks.vodolin.event

import com.tunesworks.vodolin.value.ItemColor

data class ItemSelectionChangeEvent(val selectedItemCount: Int, val itemColor : ItemColor)