import http from 'k6/http';
import { check, sleep } from 'k6';

// 💡 1000명 / 2000명 테스트 설정
export const options = {
    stages: [
        { duration: '1m', target: 500 },   // [공통] 1분 동안 500명으로 급격히 워밍업

        // 🚀 [수정 포인트] 1000명 테스트 시 아래 두 줄의 target을 1000으로 변경
        { duration: '3m', target: 2000 },  // 3분 동안 2000명까지 미친듯이 트래픽 밀어넣기
        { duration: '5m', target: 2000 },  // 5분 동안 2000명 유지 (여기서 파드와 DB가 비명을 지를 거임)

        { duration: '1m', target: 0 },     // 1분 동안 서서히 종료
    ],
    thresholds: {
        // 극한의 스트레스 테스트이므로 기준을 대폭 완화함 (시스템이 터지는 '시점'을 보기 위함)
        http_req_failed: ['rate<0.1'],     // 에러율 10%까지 허용
        http_req_duration: ['p(95)<3000'], // 95%의 요청이 3초(3000ms) 이내에 들어오면 통과
    },
};

const BASE_URL = 'http://api.morupark.urssu.com/api';

export default function () {
    // 1. 로그인
    // 2000명 규모에서 식별자 충돌(Duplicate)이 나지 않도록 난수 조합 강화
    const uniqueId = `${__VU}${__ITER}${Math.floor(Math.random() * 10000)}`;
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
    // 트래픽 폭주로 Redis 반영이 늦어질 수 있으므로 3~4초 랜덤 대기
    sleep(Math.random() * 2 + 3);

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

    // VU 간격 유지 (Think Time)
    sleep(Math.random() * 2 + 1);
}