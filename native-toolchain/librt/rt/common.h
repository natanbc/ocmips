#pragma once

#include "stddef.h"

typedef struct { int words[4]; } address_t;

#define MEMORY_MAP(_T,_N,_A) \
    volatile _T* _N = (volatile _T*) (_A)

#define RT_ASSERT_OK(cond) \
    do { if((cond)!=0) { rt_bsod(0, "Call " #cond " failed"); } } while(0)

#ifdef __cplusplus
extern "C" {
#endif

void rt_sleep(int ticks);

void rt_shutdown(int reboot);

int rt_unmap(volatile void* addr);

void rt_bsod(address_t* gpu, const char* msg);

void rt_dbg(const char* msg, int type, int val);

int rt_mremap(void* addr, void* target, int size);

int rt_ramsize();

float rt_uptime();

void rt_unmap_and_jump(volatile void* addr, void* fn, int arg1, int arg2);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
#include "rt/ffi.h"

template<typename T>
inline void rt_dbg(const char* msg, T val);

template<>
inline void rt_dbg(const char* msg, const int i) {
    rt_dbg(msg, TYPE_INTEGER, i);
}

template<>
inline void rt_dbg(const char* msg, const float f) {
    rt_dbg(msg, TYPE_FLOAT, *((int*)&f));
}

template<>
inline void rt_dbg(const char* msg, const char* str) {
    rt_dbg(msg, TYPE_STRING, (int)str);
}

template<>
inline void rt_dbg(const char* msg, const address_t addr) {
    static_assert(sizeof(address_t) == 16, "Wrong address_t size");
    rt_dbg(msg, TYPE_ADDRESS, (int)&addr);
}

template<>
inline void rt_dbg(const char* msg, address_t* addr) {
    rt_dbg(msg, TYPE_ADDRESS, (int)addr);
}

template<>
inline void rt_dbg(const char* msg, const bool b) {
    rt_dbg(msg, TYPE_BOOLEAN, b);
}

template<>
inline void rt_dbg(const char* msg, const byte_array_t* array) {
    rt_dbg(msg, TYPE_BYTE_ARRAY, (int)array);
}
#endif
