package com.maubis.scarlet.base.backup.support

import androidx.core.content.FileProvider

// This subclass is needed to avoid conflicts with FileProvider provided by EasyImage library
class ScarletFileProvider : FileProvider()
