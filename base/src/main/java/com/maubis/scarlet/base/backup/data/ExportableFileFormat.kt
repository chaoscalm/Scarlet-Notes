package com.maubis.scarlet.base.backup.data

class ExportableFileFormat(
  val version: Int,
  val notes: List<ExportableNote>,
  val tags: List<ExportableTag>,
  val folders: List<ExportableFolder>?)