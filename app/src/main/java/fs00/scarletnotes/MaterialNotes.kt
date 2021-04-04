package fs00.scarletnotes

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.export.support.ExternalFolderSync

class MaterialNotes : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    ExternalFolderSync.setup(this)
  }
}