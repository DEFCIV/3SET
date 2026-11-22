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
