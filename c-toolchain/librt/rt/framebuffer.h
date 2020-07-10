#pragma once

#include "rt/common.h"

typedef struct {
    int sync;
    int width;
    int height;
    int vram[];
} framebuffer_t;

void rt_sync_framebuffer(volatile framebuffer_t* fb);

int rt_map_framebuffer(volatile framebuffer_t* addr, address_t* gpu);
