import { sleep } from "k6";
import { reserveSeat } from "./shared.js";

export const options = {
    scenarios: {
        opening_burst: {
            executor: "ramping-arrival-rate",
            startRate: 350,
            timeUnit: "1s",
            stages: [
                { target: 350, duration: "1s" }, // 오픈 폭발
                { target: 150, duration: "4s" }, // 재시도 폭발
                { target: 50, duration: "5s" },  // 마지막 경쟁
            ],
            preAllocatedVUs: 500,
            maxVUs: 1000,
        },
    },
};

export default function () {
    reserveSeat("http://YOUR_API", 100);
    sleep(Math.random() * 3);
}
