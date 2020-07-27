#pragma once

#include "rt/common.h"

/**
 * Maps the drive for reading and writing, but writes are dropped
 * when the currently cached sector changes (due to a read or write to
 * another sector) if they haven't been explicitly flushed.
 */
#define DRIVE_MAP_DEFAULT   0
/**
 * Maps the drive for reading and writing, automatically flushing
 * writes when the currently cached sector changes.
 */
#define DRIVE_MAP_AUTOWRITE 1
/**
 * Maps the drive for reading. Attempting to write to the drive
 * triggers an exception. Flushing writes does nothing.
 */
#define DRIVE_MAP_READONLY  2

/**
 * Drops the currently cached sector along with any changes done to it.
 *
 * This can be used to reload the current sector from disk.
 */
#define DRIVE_SYNC_DISCARD  0
/**
 * Flushes any changes made to the current sector back to disk. If the
 * drive is not dirty, this does nothing.
 */
#define DRIVE_SYNC_WRITE    1

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    /** Sync word. Writing one of the DRIVE_SYNC_* constants to it syncs the drive. */
    int sync;
    /** Mode the drive was mapped in, one of DRIVE_MAP_*. Read only */
    int mode;
    /** Size of the drive in bytes. Read only */
    int size;
    /** Sector size of the drive in bytes. Read only */
    int sector_size;
    /** Current sector in cache. Read only */
    int current_sector;
    /** Whether or not the currently cached sector has been modified. Read only */
    int dirty;
    /** Data of the drive. At least `size` bytes long. Read only if mapped with DRIVE_MAP_READONLY. */
    int data[];
} drive_t;

/**
 * Maps a drive into memory.
 *
 * @param addr  Where to map the drive.
 * @param drive Address of the drive to map.
 * @param mode  Mode to map the drive. One of DRIVE_MAP_*.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_map_drive(volatile drive_t* addr, address_t* drive, int mode);

/**
 * Synchronizes a drive with a given mode.
 *
 * This is equivalent to `drive->sync = mode;`.
 *
 * @param drive Drive to sync.
 * @param mode  Mode to sync. One of DRIVE_SYNC_*.
 */
void rt_sync_drive(volatile drive_t* drive, int mode);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus

class drive {
private:
    volatile drive_t* d;
public:
    /**
     * Maps a drive map at a given address, and automatically
     * unmaps it when the destructor is called.
     *
     * After calling this, the result of the mapping operation can be
     * checked with the `operator bool()` method of this class.
     *
     * @param addr Where to map the drive in.
     * @param drv  Address of the drive.
     * @param mode Mode to map. One of DRIVE_MAP_*.
     */
    drive(int addr, address_t* drv, int mode) {
        d = (volatile drive_t*)addr;
        if(rt_map_drive(d, drv, mode) != 0) d = nullptr;
    }

    ~drive() {
        rt_unmap(d);
    }

    /**
     * Returns whether or not this mapping was created successfully.
     */
    inline operator bool() const {
        return d != nullptr;
    }

    /**
     * Syncs this drive with the given mode.
     *
     * @param mode Mode to sync.
     */
    inline void sync(int mode) {
        rt_sync_drive(d, mode);
    }

    /**
     * Flushes any writes back to the drive.
     */
    inline void flush() {
        sync(DRIVE_SYNC_WRITE);
    }

    /**
     * Discards all writes and reloads the data on the next access.
     */
    inline void reload() {
        sync(DRIVE_SYNC_DISCARD);
    }

    /**
     * Allows reading and writing to drive's fields directly.
     */
    inline volatile drive_t* operator->() { return d; }

    /**
     * Returns a pointer to the drive's data.
     */
    inline volatile uint8_t* data() const {
        return (volatile uint8_t*) &d->data;
    }
};

#endif

