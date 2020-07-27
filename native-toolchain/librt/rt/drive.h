#pragma once

#include "rt/common.h"

#define DRIVE_MAP_DEFAULT   0
#define DRIVE_MAP_AUTOWRITE 1
#define DRIVE_MAP_READONLY  2

#define DRIVE_SYNC_DISCARD  0
#define DRIVE_SYNC_WRITE    1

#ifdef __cplusplus
extern "C" {
#endif

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

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus

class drive {
private:
    volatile drive_t* d;
public:
    drive(int addr, address_t* drv, int mode) {
        d = (volatile drive_t*)addr;
        if(rt_map_drive(d, drv, mode) != 0) d = nullptr;
    }

    ~drive() {
        rt_unmap(d);
    }

    inline operator bool() const {
        return d != nullptr;
    }

    inline void sync(int mode) {
        rt_sync_drive(d, mode);
    }

    inline void flush() {
        sync(DRIVE_SYNC_WRITE);
    }

    inline void reload() {
        sync(DRIVE_SYNC_DISCARD);
    }

    inline volatile drive_t* operator->() { return d; }

    inline volatile uint8_t* data() const {
        return (volatile uint8_t*) &d->data;
    }
};

#endif
