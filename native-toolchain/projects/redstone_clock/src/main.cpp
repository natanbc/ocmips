#include "rt.h"

//redstone ticks, not minecraft ticks
//redstone tick = 0.1s, minecraft tick = 0.05s
#ifndef DELAY
  #define DELAY 10
#endif
#ifndef DURATION
  #define DURATION 5
#endif

//convert into seconds
const float delay_seconds = DELAY * 0.1;
const float duration_seconds = DURATION * 0.1;
float next_iteration = 0;

void wait_until(float deadline) {
    while(rt_uptime() <= deadline) {
        //empty signal queue (if it isn't empty sleeping does nothing)
        //we don't care about the values so just pass no buffer
        while(rt_pull_signal(NULL, 0) >= 0) {}
        rt_sleep(1);
    }
}

void set_all(method& m, int value) {
    //set all sides (north, south, east, west, up, down)
    for(int i = 0; i < 6; i++) {
        m(2, i, value);
    }
}

int main() {
    static_assert(DURATION < DELAY, "Pulse duration must be less than the delay");
    address_t rs;
    if(rt_find_component("redstone", 0, &rs) != 0) {
        rt_bsod(0, "No redstone component!");
    }
    rt_dbg("Redstone", &rs);
    //setOutput(side: number, value: number)
    method m(0x90000000, &rs, "setOutput", 2);
    m.configure_args(2, TYPE_INTEGER, TYPE_INTEGER);
    next_iteration = rt_uptime();
    while(1) {
        //use a global variable with the timestamp of the next iteration
        //so that the sleep doesn't affect the delay between pulses
        wait_until(next_iteration);
        set_all(m, 15);
        wait_until(next_iteration + duration_seconds);
        set_all(m, 0);
        next_iteration += delay_seconds;
    }
}
