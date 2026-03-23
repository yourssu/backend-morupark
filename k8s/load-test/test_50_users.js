import { reserveSeat } from "./shared.js";

export const options = {
    scenarios: {
        simultaneous_click: {
            executor: "per-vu-iterations",
            vus: 50,
            iterations: 1,
            maxDuration: "10s",
        },
    },
};

export default function () {
    reserveSeat("http://YOUR_API", 100);
}