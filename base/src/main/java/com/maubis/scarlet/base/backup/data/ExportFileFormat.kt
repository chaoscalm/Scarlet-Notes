package com.maubis.scarlet.base.backup.data

class ExportFileFormat(
    val version: Int,
    val notes: List<ExportedNote>,
    val tags: List<ExportedTag>,
    val folders: List<ExportedFolder>)