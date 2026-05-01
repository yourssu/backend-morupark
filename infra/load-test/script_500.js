import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 설정 (500명 규모로 확장)
export const options = {
    stages: [
        { duration: '1m', target: 200 },  // 1분 동안 200명까지 ramp-up
        { duration: '2m', target: 500 },  // 다음 2분 동안 500명까지 도달
        { duration: '5m', target: 500 },  // 5분 동안 500명 유지 (피크 부하)
        { duration: '1m', target: 0 },    // 1분 동안 서서히 종료
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],   // 500명 규모이므로 에러율 허용치를 5%로 살짝 완화 (필요시 조정)
        http_req_duration: ['p(95)<1000'], // 부하가 높으므로 응답 속도 기준을 1s로 완화
    },
};

const BASE_URL = 'http://api.morupark.urssu.com/api';

export default function () {
    // 1. 로그인
    // 500명 규모에서 식별자 중복을 피하기 위해 시간차(Date.now)를 아주 살짝 조합
    const uniqueId = `${__VU}${__ITER}${Date.now() % 1000}`;
    const studentId = `2024${uniqueId}`.slice(0, 10);
    const phoneNumber = `010${uniqueId}`.slice(0, 11);

    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
        studentId: studentId,
        phoneNumber: phoneNumber,
    }), { headers: { 'Content-Type': 'application/json' } });

    const isLoginOk = check(loginRes, {
        'login: status is 200': (r) => r.status === 200,
        'login: has access token': (r) => r.json('accessToken') !== undefined,
    });

    if (!isLoginOk) return;

    const accessToken = loginRes.json('accessToken');

    // 2. 대기열 진입
    const enqueueHeaders = {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
    };

    const queueRes = http.post(`${BASE_URL}/queues`, null, { headers: enqueueHeaders });

    const isQueueOk = check(queueRes, {
        'enqueue: status is 202': (r) => r.status === 202,
        'enqueue: has waiting token': (r) => r.json('waitingToken') !== undefined,
    });

    if (!isQueueOk) return;

    const waitingToken = queueRes.json('waitingToken');

    // 3. 대기열 상태 조회
    // 500명이 동시에 쏘면 Redis 반영 속도보다 빠를 수 있으므로 sleep을 2~3초로 권장
    sleep(3);

    const statusHeaders = {
        'Authorization': `Bearer ${accessToken}`,
        'X-Waiting-Token': waitingToken,
        'Content-Type': 'application/json',
    };

    const statusRes = http.get(`${BASE_URL}/queues/status`, { headers: statusHeaders });

    check(statusRes, {
        'status: status is 200': (r) => r.status === 200,
        'status: has result': (r) => r.json('status') !== undefined,
    });

    // VU 간의 간격을 위해 랜덤 지연
    sleep(Math.random() * 2 + 1);
}