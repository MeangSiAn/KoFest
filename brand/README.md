# KoFest 로고

`feature/festival/.../common/StatusViews.kt` 의 자주 사각형 + `祭` 를 그대로 자산으로 뽑은 것.

| 파일 | 쓰는 곳 |
|---|---|
| `kofest-logo.svg` | 마스터. 자주 배경 + 흰 `祭`. 벡터 |
| `kofest-logo-1024/512/192.png` | 스토어 등록, 문서, 슬라이드 |
| `kofest-mark.svg` | 배경 없는 자주색 글자 단독. 종이색 위에 얹을 때 |
| `kofest-mark-white.svg` | 배경 없는 흰 글자. 사진·자주 면 위에 얹을 때 |

## 규격

- 배경 `--jaju #6B1E32`, 글자 `#FFFFFF`. **모서리를 둥글리지 않는다** — 화면의 원본이 각진 사각형이다
- 글자 크기는 화면과 같은 비율(21/46 = 캔버스의 45.7%)로 고정. 잉크 bbox 기준 정중앙
- 글리프는 **AppleMyungjo 의 U+796D 를 path 로 변환**한 것이라 폰트 설치와 무관하게 동일하게 렌더된다.
  기획서의 나눔명조가 저장소에 들어오면(`core/designsystem/src/main/res/font/`) 같은 방식으로 다시 뽑는다
- 런처 아이콘(adaptive icon)은 아직 만들지 않았다. 안전영역이 66% 라 이 비율을 그대로 쓰면 글자가 잘린다 — 별도로 짠다
