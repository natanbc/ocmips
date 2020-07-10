#pragma once

typedef int address_t[4];

#define MEMORY_MAP(_T,_N,_A) \
    volatile _T* _N = (volatile _T*) (_A)

void rt_sleep(int ticks);

void rt_shutdown(int reboot);

int rt_unmap(volatile void* addr);

void rt_bsod(address_t* gpu, const char* msg);