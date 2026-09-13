# 배포 가이드 (AWS Lightsail)

## 1. 인스턴스 생성
- Lightsail 콘솔 → Create instance
- Platform: **Linux/Unix** → Blueprint: **OS Only → Ubuntu 22.04**
- 리전: 서울 (ap-northeast-2)
- Plan: **$10/월 (2GB RAM / 2vCPU / 60GB SSD)** 권장
  - JVM + MySQL + OCR(Tesseract/OpenCV)을 한 인스턴스에서 같이 띄우기 때문에, $5 플랜(1GB RAM)은 트래픽이 조금만 몰려도 OOM 위험이 큽니다.
- 생성 후 **Networking 탭 → Attach static IP** (무료, 재부팅해도 IP 고정됨)
- **Networking 탭 → Firewall**: 22(SSH), 80, 443만 열어두면 됩니다 (앱 포트 8081은 nginx 뒤에 두고 직접 노출하지 않음 — 아래 5번 참고).

## 2. 서버 초기 설정
Lightsail 콘솔의 브라우저 SSH(또는 다운로드한 키페어로 SSH) 접속 후:

```bash
sudo apt-get update && sudo apt-get install -y ca-certificates curl gnupg git

# swap 추가 (2GB RAM 인스턴스에서 OCR/OpenCV 메모리 스파이크 대비, 필수는 아니지만 강력 권장)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Docker 설치
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo $VERSION_CODENAME) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo usermod -aG docker $USER
# 그룹 반영을 위해 재접속 (exit 후 다시 ssh)
```

## 3. 코드 배포
```bash
git clone https://github.com/nyamtori/BE.git
cd BE
cp .env.example .env
nano .env   # DB_PASSWORD, JWT_SECRET, KAKAO_*, GEMINI_API_KEY, FRONTEND_OAUTH_REDIRECT_URI 채우기
docker compose up -d --build
```

첫 빌드는 Gradle 의존성 + tesseract-ocr apt 설치 때문에 몇 분 걸립니다.

확인:
```bash
docker compose logs -f app     # 부팅 로그 확인
curl http://localhost:8081/v3/api-docs   # 200 응답 확인
```

## 4. Kakao OAuth 설정 갱신
Kakao Developers 콘솔 → 내 애플리케이션 → 카카오 로그인 → Redirect URI에
`https://<도메인 또는 IP>/login/oauth2/code/kakao` 를 추가하세요.
(운영 환경에서는 카카오가 HTTPS 리다이렉트 URI를 권장/요구하므로, 5번의 도메인+HTTPS 설정을 먼저 하는 걸 추천합니다.)

## 5. 도메인 + HTTPS (Nginx + Let's Encrypt)
도메인이 있다면(없다면 Route 53이나 가비아 등에서 구매 후 Lightsail 고정 IP로 A 레코드 연결):

```bash
sudo apt-get install -y nginx certbot python3-certbot-nginx
```

`/etc/nginx/sites-available/nyamtori`:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/nyamtori /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d your-domain.com   # HTTPS 인증서 자동 발급 + 갱신 설정
```

이후 `.env`의 `FRONTEND_OAUTH_REDIRECT_URI`와 Kakao 콘솔의 Redirect URI를 `https://your-domain.com/...`으로 갱신하세요.

## 6. 재배포 (코드 업데이트 시)
```bash
cd BE
git pull
docker compose up -d --build
```

## 비용 요약
- Lightsail $10/월 (2GB) + 고정 IP 무료 + 아웃바운드 트래픽 3TB 포함
- 도메인 (선택): 연 1만~2만원대
- MySQL은 같은 인스턴스의 Docker 컨테이너로 운영 → 추가 비용 없음
