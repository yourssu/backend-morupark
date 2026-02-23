import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'k6/x/random';

// 10000명의 가상 유저가 1분 동안 테스트를 수행합니다.
export const options = {
  vus: 10000,
  duration: '1m',
  thresholds: {
    http_req_failed: ['rate<0.01'], // http 에러는 1% 미만
    http_req_duration: ['p(95)<500'], // 95%의 요청은 500ms 이내에 처리
  },
};

// 테스트에 사용할 기본 URL
const BASE_URL = 'http://localhost:8080/api'; // 로컬 Ingress 또는 포트포워딩 주소에 맞게 수정하세요.

// 테스트 시나리오
export default function () {
  // 1. 사용자 등록 및 토큰 발급
  // 각 가상 유저마다 고유한 platformName을 생성하여 다른 사용자인 것처럼 시뮬레이션
  const platformName = `test-user-${__VU}-${__ITER}`;
  const registerPayload = JSON.stringify({
    platformName: platformName,
  });

  const registerParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const registerRes = http.post(`${BASE_URL}/auth/token`, registerPayload, registerParams);

  check(registerRes, {
    'register: status is 201': (r) => r.status === 201,
    'register: has access token': (r) => r.json('accessToken') !== null,
  });

  // 응답에서 accessToken 추출
  const accessToken = registerRes.json('accessToken');

  // 토큰 발급 실패 시 테스트 중단
  if (!accessToken) {
    console.error(`[VU: ${__VU}, Iter: ${__ITER}] Failed to get access token. Response: ${registerRes.body}`);
    return;
  }

  // 2. 대기열 진입 요청
  const queueParams = {
    headers: {
      'Authorization': `Bearer ${accessToken}`,
    },
  };

  const queueRes = http.post(`${BASE_URL}/queues`, null, queueParams);

  check(queueRes, {
    'enqueue: status is 202': (r) => r.status === 202,
  });

  // 각 가상 유저는 요청 사이에 1초간 대기합니다.
  sleep(1);
}
