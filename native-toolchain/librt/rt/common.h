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

/**
 * Sleeps for a given amount of minecraft ticks. A minecraft tick
 * is equivalent to 0.05 seconds. Immediately returns if there are
 * queued signals, and returns early if a signal is queued during sleep.
 *
 * Use rt_pull_signal and/or rt_uptime to sleep for a fixed amount of time.
 *
 * @param ticks Number of ticks to sleep
 */
void rt_sleep(int ticks);

/**
 * Shuts down the machine, optionally rebooting it.
 *
 * If `reboot` is anything other than 0, the machine reboots.
 *
 * Never returns.
 *
 * @param reboot Whether or not the machine should reboot.
 */
void rt_shutdown(int reboot);

/**
 * Unmaps a memory map. The given address must be the base address
 * of the memory map.
 *
 * @param addr Address of the memory map to unmap.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_unmap(volatile void* addr);

/**
 * Crashes the machine with the given error message, optionally
 * drawing it to a gpu.
 *
 * This function never returns.
 *
 * @param gpu Address of the gpu to draw the message on. May be null.
 * @param msg Error message.
 */
void rt_bsod(address_t* gpu, const char* msg);

/**
 * Writes a debug message with an argument to the game's console.
 *
 * This function might be made a noop or gated behind a mod configuration
 * in the future.
 *
 * @param msg  Message to print.
 * @param type Type of the argument (see rt/ffi.h).
 * @param val  Bitwise representation of the value (eg a float
 *             type-punned to an int, or a pointer cast to an int,
 *             which works because ints are word-sized).
 */
void rt_dbg(const char* msg, int type, int val);

/**
 * Remaps an address range to be interpreted as if it was another region,
 * such that any reads/writes to [addr, addr + size) are remaped to
 * [target, target + size).
 *
 * @param addr   Base address of the resulting map.
 * @param target Target region of the map.
 * @param size   Size of the map.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_mremap(void* addr, void* target, int size);

/**
 * Returns the size of the installed ram, in bytes.
 */
int rt_ramsize();

/**
 * Returns the uptime of the machine, in seconds.
 */
float rt_uptime();

/**
 * Unmaps a memory map, then jumps to `fn`, with the first two arguments
 * being set to `arg1` and `arg2`. The provided function should never return.
 *
 * This function never returns.
 *
 * @param addr Address to unmap. Unmap failures are ignored.
 * @param fn   Address of the function to jump to.
 * @param arg1 First argument for the function.
 * @param arg2 Second argument for the function.
 */
void rt_unmap_and_jump(volatile void* addr, void* fn, int arg1, int arg2);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
#include "rt/ffi.h"

//C++ overloads of rt_dbg

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
