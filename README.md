# 3SET — 이 대화에서 계속 업데이트하는 운동 앱

메인 화면은 캐릭터 / 3세트 체크 / 운동 달력만 유지하고, 운동 5종의 움직이는 자세 가이드를 포함합니다.

## 배포 구조

- Android APK는 **고정된 GitHub Pages 웹 주소**를 WebView로 표시합니다. 웹 화면을 바꾸어도 APK를 다시 설치할 필요가 없습니다.
- 웹 사이트가 꺼졌거나 연결되지 않으면 APK 내부의 **동봉된 오프라인 화면**을 사용합니다.
- 운동 기록 및 알림 설정은 Android `SharedPreferences`에도 저장돼, 온라인 페이지 ↔ 오프라인 페이지 이동 중에도 보존됩니다.
- 독촉 알림은 Android 네이티브 `AlarmManager`로 예약합니다(권한 허용 필요). 앱이 닫혀도 예약은 유지되지만 절전·제조사 정책에 따라 시각이 지연될 수 있습니다.

## 최초 1회 설정 (아직 미완료)

1. GitHub 계정 `DEFCIV`에서 **새 공개 저장소 `3SET`** 를 만듭니다. ChatGPT 연결 GitHub App에도 이 저장소 접근을 허용해야 합니다. 다른 저장소 `our-home-fridge`, `makchan`은 절대 사용하지 않습니다.
2. 이 프로젝트를 저장소 루트에 업로드합니다. 이 대화의 ChatGPT에 저장소 주소를 주면 연결된 GitHub를 통해 추후 코드를 갱신할 수 있습니다.
3. GitHub 저장소 **Settings → Pages → Build and deployment → Source: GitHub Actions** 로 설정합니다.
4. GitHub Actions의 `Deploy website updates` 성공 후 `https://defciv.github.io/3SET/`가 실제 배포된 주소입니다.
5. GitHub Actions의 `Build installable Android APK`에서 생성한 artifact ZIP을 받아 `app-debug.apk`를 갤럭시에 설치합니다. `Actions` artifact가 성공해야 APK를 받는 것이며, 이 소스 ZIP 자체는 APK가 아닙니다.
6. 알림 권한을 허용합니다. 앱의 종 아이콘에서 시간과 알림 여부를 설정합니다.

## 이후 이 채팅에서 업데이트

'3SET 앱에서 캐릭터를 ~~로 바꿔줘'처럼 요청하면 `docs/index.html`, `docs/version.json`, 필요하면 다른 웹 파일만 수정하면 됩니다. `version.json`의 `version`과 HTML 안의 `SITE_BUILD`, `sw.js`의 `CACHE` 값을 새로운 동일 버전으로 바꿔야 합니다. GitHub `main`에 반영되면 Pages가 재배포하고 앱을 새로 열거나 페이지를 갱신할 때 적용됩니다.

**중요:** 채팅에 수정 요청을 적었다는 사실 자체로 자동 배포되지는 않습니다. ChatGPT가 해당 GitHub 저장소에 변경 사항을 커밋하고 Actions 배포에 성공해야 앱에 반영됩니다. 재설치 없는 업데이트는 *웹 화면·이미지·운동 가이드·문구·웹 로직*에 한합니다. Android 권한, 네이티브 알림 동작, 앱 아이콘 등 Android 쪽을 수정하면 APK 재빌드/재설치가 필요할 수 있습니다.

## 테스트 및 주의점

- `docs/`는 배포 웹 파일이며 `app/src/main/assets/`는 연결이 안 될 때 사용하는 동봉 버전입니다.
- 이 버전은 앱 소스 생성 단계로, Android SDK/Gradle이 없는 현재 실행 환경에서 APK 실물 빌드·실기기 테스트는 아직 완료하지 못했습니다.
- APK는 디버그 서명으로 빌드됩니다. 별도 서명/배포용 APK를 만들 땐 안정적인 릴리스 키 관리가 필요합니다.
- 사용자 운동 기록은 이 기기에 저장됩니다. 기기 변경 시 클라우드 동기화는 아직 없습니다.

## 브랜드 이름 확정

- 앱의 대외 표시 이름은 **3SET**. 기존 표정 캐릭터와 화면 구성은 유지.
- 이전 내부 패키지 `com.haennya.fitness`와 로컬 저장 키는 기록 보존을 위해 의도적으로 변경하지 않음.
- 소스가 배포·APK 빌드된 것은 아니며 GitHub 신규 저장소 생성·초기 배포가 먼저 필요.