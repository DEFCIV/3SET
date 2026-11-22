# 3SET — daily 3-set workout companion

3SET tracks three daily workout sets, displays the existing red/yellow/green expressive character, keeps a workout calendar, and includes five illustrated exercise guides.

## Web & Android deployment

- Web source: `docs/index.html`, with version in `docs/version.json`.
- Android wrapper: `app/` (initial APK build workflow will be configured separately).
- Web target: https://defciv.github.io/3SET/ (only after Pages is enabled and deployment succeeds).
- Changes in this chat require a GitHub commit before deployment; they are not deployed simply by being mentioned.
- Original app state keys and package identifiers are preserved.

**Status:** Repository connected and web sources committed. GitHub Pages publication and APK installation are separate steps. The repository is currently private; GitHub Free generally requires a public repository for Pages.

## Future updates

Update web files and bump the same build/version tag in `docs/index.html`, `docs/version.json`, and `docs/sw.js`. After the Pages workflow succeeds, opening the installed wrapper can show new web content without reinstalling the APK. Native permission/reminder changes require rebuilding the APK.

## Safety

Private workout history stays on-device. The web display uses a fixed origin rather than permitting arbitrary web navigation. Android notifications require user permission and can be delayed by battery-saving rules.


## Animated character & home icon (3.1.0)

- A gentle floating idle animation and blink are added to the web mascot. The green completed face is a restrained small smile; previous workout animation exaggerations are reduced.
- Native Android launcher uses two activity aliases: red neutral by default and green smiling only when today's count is exactly 3. Unchecking a set returns the neutral icon.
- An inexact midnight AlarmManager event, app resume, device boot and time change keep the icon current; Android power saving may delay the scheduled change. Galaxy launcher caches/placement may vary.
- Launcher icon switching is a native change, therefore this version requires a newly built APK. Future character movement and guide adjustments can be delivered via web when Pages is enabled.
- Debug APKs from distinct GitHub Actions runners may have different signing keys. Backup workout history before replacing an already installed debug build; never promise a seamless in-place update until signing has been stabilized.


## Launcher mascot status (v3.1.1)

- 0 rounds: red neutral face (`LauncherDefault`)
- 1–2 rounds: yellow thinking face (`LauncherPartial`)
- 3 rounds: green restrained smile (`LauncherDone`)
- State is read from the existing local daily record; transitions include manual corrections and the next-day reset. On Android launchers, alias switching may temporarily rearrange shortcuts or delay icon refresh. This is a native APK change requiring a reinstall/update.
