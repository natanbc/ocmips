#pragma once

#include "rt/common.h"

#define EEPROM_SYNC_DISCARD      0
#define EEPROM_SYNC_WRITE        1

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    int sync;
    int dirty;
    int size;
    int data_size;
    int __internal[];
} eeprom_t;

void rt_sync_eeprom(volatile eeprom_t* eeprom, int mode);

int rt_map_eeprom(volatile eeprom_t* addr, address_t* eeprom);

volatile int* rt_get_eeprom_content(volatile eeprom_t* eeprom);

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
    eeprom(int addr, address_t* rom) {
        e = (volatile eeprom_t*)addr;
        if(rt_map_eeprom(e, rom) != 0) e = nullptr;
    }

    ~eeprom() {
        rt_unmap(e);
    }

    inline operator bool() const {
        return e != nullptr;
    }

    inline void sync(int mode) {
        rt_sync_eeprom(e, mode);
    }

    inline void flush() {
        sync(EEPROM_SYNC_WRITE);
    }

    inline void reload() {
        sync(EEPROM_SYNC_DISCARD);
    }

    inline volatile eeprom_t* operator->() { return e; }

    inline volatile uint8_t* content() const {
        return (volatile uint8_t*)rt_get_eeprom_content(e);
    }

    inline volatile uint8_t* data() const {
        return (volatile uint8_t*)rt_get_eeprom_data(e);
    }
};

#endif
