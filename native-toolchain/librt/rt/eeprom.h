#pragma once

#include "rt/common.h"

/**
 * Drops the currently cached data, reloading from the eeprom
 * on the next read/write.
 *
 * This can be used to reload changes from the eeprom.
 */
#define EEPROM_SYNC_DISCARD      0
/**
 * Flushes any writes back to the eeprom. If the eeprom isn't dirty,
 * this does nothing.
 */
#define EEPROM_SYNC_WRITE        1

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    /** Sync word. Writing one of the EEPROM_SYNC_* constants to it syncs the eeprom. */
    int sync;
    /** Whether or not the eeprom is dirty. */
    int dirty;
    /** Size of the eeprom's contents in bytes. */
    int size;
    /** Size of the eeprom's data storage in bytes. */
    int data_size;
    /** Pointer to the eeprom's data. Contains `size` bytes mapping to the eeprom's content,
     *  followed by `data_size` bytes mapping to the eeprom's data storage.
     */
    int __internal[];
} eeprom_t;

/**
 * Maps an eeprom into memory.
 * 
 * @param addr   Where to map the eeprom.
 * @param eeprom Address of the eeprom.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_map_eeprom(volatile eeprom_t* addr, address_t* eeprom);

/**
 * Synchronizes an eeprom with the given mode.
 *
 * This is equivalent to `eeprom->sync = mode;`.
 *
 * @param eeprom EEPROM to sync.
 * @param mode   Mode to sync. One of EEPROM_SYNC_*.
 */
void rt_sync_eeprom(volatile eeprom_t* eeprom, int mode);

/**
 * Returns a pointer to the eeprom's content.
 */
volatile int* rt_get_eeprom_content(volatile eeprom_t* eeprom);

/**
 * Returns a pointer to the eeprom's data storage.
 */
volatile int* rt_get_eeprom_data(volatile eeprom_t* eeprom);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus

#include "stddef.h"

class eeprom {
private:
    volatile eeprom_t* e;
public:
    /**
     * Maps an eeprom at a given address, and automatically
     * unmaps it when the destructor is called.
     *
     * After calling this, the result of the mapping operation can be
     * checked with the `operator bool()` method of this class.
     *
     * @param addr Where to map the eeprom.
     * @param rom  Address of the eeprom.
     */
    eeprom(int addr, address_t* rom) {
        e = (volatile eeprom_t*)addr;
        if(rt_map_eeprom(e, rom) != 0) e = nullptr;
    }

    ~eeprom() {
        rt_unmap(e);
    }

    /**
     * Returns whether or not this mapping was created successfully
     */
    inline operator bool() const {
        return e != nullptr;
    }

    /**
     * Syncs this eeprom with the given mode.
     *
     * @param mode Mode to sync.
     */
    inline void sync(int mode) {
        rt_sync_eeprom(e, mode);
    }

    /**
     * Flushes any writes back to the eeprom.
     */
    inline void flush() {
        sync(EEPROM_SYNC_WRITE);
    }

    /**
     * Discards all writes and reloads the eeprom on the next access.
     */
    inline void reload() {
        sync(EEPROM_SYNC_DISCARD);
    }

    /**
     * Allows reading and writing to eeprom's fields directly.
     */
    inline volatile eeprom_t* operator->() { return e; }

    /**
     * Returns a pointer to the eeprom's contents.
     */
    inline volatile uint8_t* content() const {
        return (volatile uint8_t*)rt_get_eeprom_content(e);
    }

    /**
     * Returns a pointer to the eeprom's data storage.
     */
    inline volatile uint8_t* data() const {
        return (volatile uint8_t*)rt_get_eeprom_data(e);
    }
};

#endif
