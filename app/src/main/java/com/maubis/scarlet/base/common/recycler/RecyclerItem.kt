package com.maubis.scarlet.base.common.recycler

abstract class RecyclerItem {

  abstract val type: Type

  enum class Type {
    NOTE,
    EMPTY_NOTICE,
    FOLDER
  }
}
