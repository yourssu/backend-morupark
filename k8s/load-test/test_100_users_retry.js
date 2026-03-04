import { sleep } from "k6";
import { reserveSeat } from "./shared.js";

export const options = {
    scenarios: {
        burst_and_retry: {
            executor: "ramping-arrival-rate",
            startRate: 80,
            timeUnit: "1s",
            stages: [
                { target: 80, duration: "1s" }, // 오픈 순간
                { target: 20, duration: "4s" }, // 재시도
            ],
            preAllocatedVUs: 100,
            maxVUs: 200,
        },
    },
};

export default function () {
    reserveSeat("http://YOUR_API", 100);
    sleep(Math.random() * 2);
}
