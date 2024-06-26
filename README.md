# MurderCraft
![ex](https://github.com/NoBrain0917/MurderCraft/blob/master/res/example.gif?raw=true)     

자신이 만든 건축물로 머더 미니게임을 즐겨보세요
         
---
<br>
         
## 설치 방법
1. 1.19.4 Paper 버킷 준비
2. [릴리즈](https://github.com/NoBrain0917/MurderCraft/releases/)에서 최신 버전 다운로드
3. 압축풀고 플러그인 폴더 안에 jar파일 2개 집어넣기


- Murder.jar - 메인 파일 (필수)     
- ProtocolLib.jar - 프로토콜 관련 라이브러리 (필수)      
- [Simple Voice Chat Mod](https://modrinth.com/plugin/simple-voice-chat/versions?l=fabric&l=forge), [Simple Voice Chat Plugin](https://modrinth.com/plugin/simple-voice-chat/versions?l=paper) - 인게임 마이크 기능을 추가합니다. 방송인이거나 게임의 재미를 더 높이고 싶다면 추천합니다. (필수아님, 선택)
 
Simple Voice Chat을 사용하신다면 모든 유저가 Simple Voice Chat Mod 설치, 플러그인 폴더 안에 Simple Voice Chat Plugin을 넣어여 합니다.

<br>


## 사용법
1. 먼저 게임을 플레이할 맵을 건축해야 합니다.
2. 맵을 건축했다면 명령어를 통해 등록합니다.
3. 게임 시작시 그 맵에서 스폰할 장소를 명령어를 통해 추가합니다.
4. 적당히 맵을 추가했다면 `/머더 시작`라는 명령어로 게임을 즐길 수 있습니다.

자세한 명령어는 아래를 참고해 주세요.    

<br>

     

## 플레이 방법
- 기본적으로 하이픽셀의 Murder Mystery와 규칙은 같습니다.
- 살인마와 경찰 그리고 시민이라는 역할이 존재합니다.
<br>

- 모든 역할은 여러가지의 고유의 능력있습니다. 일정 확률로 능력을 가질 수 있습니다.
- 능력은 황금 조각을 우클릭해 사용할 수 있으며 능력이 있는데 황금 조각이 없다면 패시브 능력입니다.
<br>

- 살인마는 자신을 제외한 모든 사람을 제거하는 것이 목표입니다.
- 철 칼을 우클릭해 던질 수 있습니다. 다만 칼을 회수하는 동안은 사용하지 못합니다.
<br>
  
- 경찰은 살인마를 제거하는 것이 목표입니다.
- 살인마가 아닌 선량한 시민을 공격한다면 신의 심판으로 자신이 죽게됩니다.
<br>

- 시민은 죽지 않고 끝까지 버티는 것이 목표입니다.
- 제한 시간이 다 될 때까지 버티거나 경찰의 총을 주워 살인마를 공격할 수 있습니다. 

<br>

## 명령어 
- 모든 명령어는 OP가 있어야 가능합니다.

### 시작 / 종료
```
/머더 시작 - 랜덤한 맵에서 게임 시작
/머더 시작 <맵이름> - 지정된 에서 게임 시작
/머더 강제시작 - 게임이 이미 진행 중 이더라도 랜덤한 맵에서 게임 강제 시작
/머더 강제시작 <맵이름> - 게임이 이미 진행 중 이더라도 지정된 맵에서 게임 강제 시작
/머더 강종 - 게임을 무승부로 강제 종료
```

### 맵
```
/머더 맵 추가 <맵이름> - 새로운 맵을 추가합니다.
/머더 맵 스폰 <맵이름> - 게임 시작 시 랜덤 스폰 할 장소에 현재 위치를 추가합니다.
/머더 맵 스폰 <맵이름> <x> <y> <z> - 게임 시작 시 랜덤 스폰 할 장소에 <x> <y> <z>를 추가합니다.
/머더 맵 삭제 <맵이름> - 맵을 삭제합니다.

/머더 맵 스폰 로비 - 로비 스폰 장소중에 현재 위치를 추가합니다.
/머더 맵 스폰 로비 <x> <y> <z> - 로비 스폰 장소중에 <x> <y> <z>를 추가합니다.
/머더 맵 삭제 로비 - 로비 스폰 장소들을 초기화 합니다.
```

### 설정
```
/머더 설정 <설정이름> <값>
```
#### 설정 종류
```
SkillPercentage (기본값 20) - 능력을 가질 수 있을 확률입니다. 백분율로 표시됩니다.
AllowEntityDamageInLobby (기본값 true) - 로비(게임이 시작하기 전 상태)에서 엔티티에게 대미지를 줄 수 있는지에 대한 여부입니다. 맵을 만들때는 true, 맵을 다 만들고 플레이 할 때는 false로 하는걸 추천합니다.
MurdererCount (기본값 1) - 선택하는 살인자의 수 입니다.
DetectiveCount (기본값 1) - 선택하는 경찰의 수 입니다.
TotalTime (기본값 180) - 게임의 진행 시간입니다. 시간이 다 지나면 시민이 승리합니다. 기본 단위는 초(s)입니다.
```

<br>

### 주의
 - `살인마 선택 -> 경찰 선택 -> 나머지 시민`라는 순서를 가지고 있어 플레이어가 5명인데 `MurdererCount`가 5고 `DetectiveCount`가 1이라면 살인마가 5명, 경찰 0명이 됩니다.
 - **Paper 기반으로 만들어졌습니다.** Spigot은 테스트안해봐서 몰?루 아마 안될거에요.

