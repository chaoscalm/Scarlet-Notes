# Fs00's personal Scarlet Notes fork

A more polished and lightweight fork of [Scarlet Notes](https://github.com/BijoySingh/Scarlet-Notes) that works completely offline.

I've decided to develop this fork because I really like the overall experience of Scarlet, but I've never got used to some design choices and usability issues of the original app, even after using it for a few years.  
Furthermore, Scarlet Notes development stopped a few years ago and there seem to be very little chances that it will ever resume.

All credit for the development of the original Scarlet Notes app goes to its creator [Bijoy Singh](https://github.com/BijoySingh).

## Differences from upstream Scarlet Notes
- Changed app logo (the one from an older Scarlet version is used)
- Removed cloud sync with Google Drive
- Removed several half-broken features (floating bubble, sync to local folder, text-to-speech, automatic trash emptying in background)
- Removed almost all translations (many of them were of low quality, often made with an automatic translation software)
- Made app much smaller compared to the Play Store version (now takes ~4MB once installed)
- Improved note editing experience (more intuitive bottom bar, smarter Markdown -> text block conversions, fixed undo/redo, and more)
- Slightly revamped and reorganized UI
- Leveraged Android built-in file picker for importing/exporting backups more easily
- Fixed lots of bugs
- Under the hood improvements and optimizations

## Compatibility
You can import backups made with the upstream version of Scarlet Notes (including Scarlet Pro) into this fork.  
The reverse is possible and should work just fine, but I can't guarantee that it will always continue to work without issues.

## Development and contributions
Being a project aimed at personal use, don't expect any continued development or frequent releases.  
Requests for new features won't be taken into account, as I'm not willing to add more functionality to the app.

That said, you're welcome to use this fork and report any bugs (I don't guarantee I'll take the time to fix them though) or propose any improvements to existing features.  
I'll gladly accept any PR that fixes some bugs or adds a new translation (please only translate into languages that you know very well!).
