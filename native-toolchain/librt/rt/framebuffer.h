#pragma once

#include "rt/common.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    int sync;
    int width;
    int height;
    int vram[];
} framebuffer_t;

void rt_sync_framebuffer(volatile framebuffer_t* fb);

void rt_clear_framebuffer(volatile framebuffer_t* fb);

int rt_map_framebuffer(volatile framebuffer_t* addr, address_t* gpu);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
class framebuffer {
private:
    volatile framebuffer_t* fb;
public:
    framebuffer(int addr, address_t* gpu) {
        fb = (volatile framebuffer_t*)addr;
        if(rt_map_framebuffer(fb, gpu) != 0) fb = nullptr;
    }

    ~framebuffer() {
        rt_unmap(fb);
    }

    inline operator bool() const {
        return fb != nullptr;
    }

    inline void sync() {
        rt_sync_framebuffer(fb);
    }

    inline void clear() {
        rt_clear_framebuffer(fb);
    }

    inline volatile framebuffer_t* operator->() { return fb; }

    inline volatile int& operator() (unsigned int x, unsigned int y) {
        return fb->vram[y * fb->width + x];
    }

    inline int operator() (unsigned int x, unsigned int y) const {
        return fb->vram[y * fb->width + x];
    }
};
#endif
