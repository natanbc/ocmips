#pragma once

#include "rt/common.h"

#define FRAMEBUFFER_SYNC_BITBLT 1
#define FRAMEBUFFER_SYNC_CLEAR  2

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    /** Sync word. Writing one of the FRAMEBUFFER_SYNC_* constants to it syncs the framebuffer. */
    int sync;
    /** Width of the VRAM buffer. Read only. */
    int width;
    /** Height of the VRAM buffer. Read only. */
    int height;
    /** VRAM of the framebuffer. Indexes are `y * width + x`. Each value is the RGB color of
     *  the respetive pixel. */
    int vram[];
} framebuffer_t;

/**
 * Maps a framebuffer into memory.
 *
 * @param addr Where to map the framebuffer.
 * @param gpu  Address of the gpu.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_map_framebuffer(volatile framebuffer_t* addr, address_t* gpu);

/**
 * Synchronizes a framebuffer to the screen it's attached to.
 *
 * This is equivalent to `fb->sync = FRAMEBUGGER_SYNC_BITBLT;`.
 *
 * @param fb Framebuffer to synchronize.
 */
void rt_sync_framebuffer(volatile framebuffer_t* fb);

/**
 * Clears a framebuffer.
 *
 * This is equivalent to `fb->sync = FRAMEBUFFER_SYNC_CLEAR;`.
 *
 * @param fb Framebuffer to clear.
 */
void rt_clear_framebuffer(volatile framebuffer_t* fb);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
class framebuffer {
private:
    volatile framebuffer_t* fb;
public:
    /**
     * Maps a framebuffer at the given address, and automatically
     * unmaps it when the destructor is called.
     *
     * After calling this, the result of the mapping operation can be
     * checked with the `operator bool()` method of this class.
     *
     * @param addr Where to map the framebuffer.
     * @param gpu  Address of the gpu.
     */
    framebuffer(int addr, address_t* gpu) {
        fb = (volatile framebuffer_t*)addr;
        if(rt_map_framebuffer(fb, gpu) != 0) fb = nullptr;
    }

    ~framebuffer() {
        rt_unmap(fb);
    }

    /**
     * Returns whether or not this mapping was created successfully.
     */
    inline operator bool() const {
        return fb != nullptr;
    }

    /**
     * Synchronizes this framebuffer with the screen.
     */
    inline void sync() {
        rt_sync_framebuffer(fb);
    }

    /**
     * Clears this framebuffer.
     */
    inline void clear() {
        rt_clear_framebuffer(fb);
    }

    /**
     * Allows reading and writing the framebuffer's fields directly.
     */
    inline volatile framebuffer_t* operator->() { return fb; }

    /**
     * Allows reading and writing a specific pixel.
     *
     * @param x X coordinate of the pixel, must be less than the width.
     * @param y Y coordinate of the pixel, must be less than the height.
     *
     * @return A reference to the pixel at the given coordinate.
     */
    inline volatile int& operator() (unsigned int x, unsigned int y) {
        return fb->vram[y * fb->width + x];
    }

    /**
     * Reads a pixel at the given coordinate.
     *
     * @param x X coordinate of the pixel, must be less than the width.
     * @param y Y coordinate of the pixel, must be less than the height.
     *
     * @return The color of the pixel at the given coordinate.
     */
    inline int operator() (unsigned int x, unsigned int y) const {
        return fb->vram[y * fb->width + x];
    }
};
#endif
