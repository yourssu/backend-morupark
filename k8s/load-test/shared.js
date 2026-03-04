import http from "k6/http";
import { check } from "k6";

export function reserveSeat(baseUrl, seatCount) {
    const seatId = Math.floor(Math.random() * seatCount) + 1;

    const payload = JSON.stringify({
        eventId: 1,
        seatId: seatId
    });

    const params = {
        headers: {
            "Content-Type": "application/json"
        },
        timeout: "5s"
    };

    const res = http.post(`${baseUrl}/tickets/reserve`, payload, params);

    check(res, {
        "status is 200 or 409": (r) => r.status === 200 || r.status === 409,
    });

    return res;
}
