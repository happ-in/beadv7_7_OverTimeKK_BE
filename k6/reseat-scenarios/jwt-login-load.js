import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';
const ALGO = __ENV.ALGO || 'HS384';
const USER_COUNT = Number(__ENV.USER_COUNT) || 50;
const STAGE_DURATION = __ENV.STAGE_DURATION || '30s';

const loginDuration = new Trend('login_duration', true);

function stage(vus, startTime) {
    return {
        executor: 'constant-vus',
        vus: vus,
        duration: STAGE_DURATION,
        startTime: startTime,
        exec: 'login',
        tags: { vus: String(vus), algo: ALGO, phase: 'login' },
    };
}

export const options = {
    discardResponseBodies: true,
    scenarios: {
        vus_10: stage(10, '0s'),
        vus_50: stage(50, '30s'),
        vus_100: stage(100, '60s'),
        vus_300: stage(300, '90s'),
        vus_500: stage(500, '120s'),
        vus_1000: stage(1000, '150s'),
    },
    thresholds: {
        'http_req_failed': ['rate<0.05'],
    },
};

export function login() {
    const userNum = Math.floor(Math.random() * USER_COUNT) + 1;
    const body = JSON.stringify({
        username: `loadtest${userNum}`,
        password: 'password123!',
    });

    const res = http.post(`${BASE_URL}/api/users/login`, body, {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'POST /api/users/login' },
    });

    loginDuration.add(res.timings.duration);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}