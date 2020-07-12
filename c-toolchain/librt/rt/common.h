#pragma once

typedef int address_t[4];

#define MEMORY_MAP(_T,_N,_A) \
    volatile _T* _N = (volatile _T*) (_A)

#define RT_ASSERT_OK(cond) \
    do { if((cond)!=0) { rt_bsod(0, "Call " #cond " failed"); } } while(0)

void rt_sleep(int ticks);

void rt_shutdown(int reboot);

int rt_unmap(volatile void* addr);

void rt_bsod(address_t* gpu, const char* msg);

void rt_dbg(const char* msg, int type, int val);

