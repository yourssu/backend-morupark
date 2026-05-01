import http from 'k6/http';
import { check, sleep } from 'k6';

// 스트레스 테스트 설정 (극심한 부하 상황 시뮬레이션)
export const options = {
  stages: [
    { duration: '2m', target: 200 },  // 2분 동안 200명까지 점진적 증가
    { duration: '5m', target: 500 },  // 5분 동안 500명까지 대폭 증가 (Stress)
    { duration: '10m', target: 500 }, // 10분 동안 500명 유지 (Stability)
    { duration: '2m', target: 0 },   // 2분 동안 종료
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'], // 5% 미만 에러 허용 (부하 시 어느 정도 에러 감안)
    http_req_duration: ['p(99)<2000'], // 99%의 요청은 2초 이내 처리
  },
};

const BASE_URL = 'http://api.morupark.urssu.com/api';

export default function () {
  const studentId = `STRESS${__VU}${__ITER}`.slice(0, 10);
  const phoneNumber = `010${__VU}${__ITER}`.slice(0, 11);

  const loginPayload = JSON.stringify({
    studentId: studentId,
    phoneNumber: phoneNumber,
  });

  const headers = { 'Content-Type': 'application/json' };

  // 1. 로그인
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, { headers });
  const accessToken = loginRes.json('accessToken');

  if (!accessToken) return;

  const authHeaders = {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
  };

  // 2. 대기열 진입 (연속 시도)
  const queueRes = http.post(`${BASE_URL}/queues`, null, { headers: authHeaders });
  
  check(queueRes, {
    'enqueue: status is 202': (r) => r.status === 202,
  });

  const waitingToken = queueRes.json('waitingToken');
  if (!waitingToken) return;

  // 3. 상태 지속 모니터링 (부하 상황 시뮬레이션)
  for (let i = 0; i < 3; i++) {
    sleep(1);
    const statusRes = http.get(`${BASE_URL}/queues/status?token=${waitingToken}`, { headers: authHeaders });
    check(statusRes, {
      'status check ok': (r) => r.status === 200,
    });
    
    // 성공 시 루프 종료
    if (statusRes.json('status') === 'SUCCESS' || statusRes.json('status') === 'FAILED') {
        break;
    }
  }

  sleep(0.5);
}
