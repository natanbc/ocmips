#pragma once

#include "rt/common.h"

#define DRIVE_MAP_DEFAULT   0
#define DRIVE_MAP_AUTOWRITE 1
#define DRIVE_MAP_READONLY  2

#define DRIVE_SYNC_DISCARD  0
#define DRIVE_SYNC_WRITE    1

typedef struct {
    int sync;
    int mode;
    int size;
    int sector_size;
    int current_sector;
    int dirty;
    int data[];
} drive_t;

int rt_map_drive(volatile drive_t* addr, address_t* drive, int mode);

void rt_sync_drive(volatile drive_t* drive, int mode);

